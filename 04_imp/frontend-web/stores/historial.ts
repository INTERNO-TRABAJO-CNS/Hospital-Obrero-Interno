import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { NOMBRE_ESTADO } from '~/models/agenda'
import type { Cirugia } from '~/models/cirugia'
import { nombreEspecialidad } from '~/models/especialidad'
import type { FichaInterna } from '~/models/fichaInterna'
import { fechaMes } from '~/models/fechas'
import type { HistorialPaciente } from '~/models/historial'
import type { CirugiasService } from '~/services/cirugiasService'
import type { FichasInternasService } from '~/services/fichasInternasService'
import type { HistorialService } from '~/services/historialService'
import { ApiError, mensajeDeError } from '~/services/apiClient'

export type TipoEvento = 'ATENCION' | 'CITA' | 'FICHA_INTERNA' | 'CIRUGIA'
export type FiltroHistorial = 'TODO' | 'ATENCIONES' | 'FICHAS' | 'CIRUGIAS'

/** Un evento de la linea de tiempo (Figma «HISTORIAL PACIENTE»). */
export interface EventoHistorial {
  clave: string
  tipo: TipoEvento
  fecha: string
  hora: string
  /** «Consultorio Familiar · Carlos Ramos» */
  titulo: string
  especialidad: string
  medico: string
  estado: string | null
  recetas: string[]
  /** Lineas de detalle bajo el titulo. */
  lineas: string[]
}

/** CU-14 en la web: historial del paciente (solo lectura) con atenciones, fichas internas y cirugias. */
export const useHistorialStore = defineStore('historial', () => {
  let servicio: HistorialService | null = null
  let fichasSrv: FichasInternasService | null = null
  let cirugiasSrv: CirugiasService | null = null

  const historial = ref<HistorialPaciente | null>(null)
  const fichas = ref<FichaInterna[]>([])
  const cirugias = ref<Cirugia[]>([])
  const filtro = ref<FiltroHistorial>('TODO')
  const cargando = ref(false)
  const error = ref<string | null>(null)
  const sinPermiso = ref(false)

  const lineaDeTiempo = computed<EventoHistorial[]>(() => {
    const h = historial.value
    if (!h) return []
    const eventos = new Map<string, EventoHistorial>()
    for (const c of h.citasPasadas) {
      eventos.set(`cita-${c.citaId}`, {
        clave: `cita-${c.citaId}`, tipo: c.estado === 'ATENDIDA' ? 'ATENCION' : 'CITA', fecha: c.fecha, hora: c.hora,
        titulo: `${nombreEspecialidad(c.especialidad)} · ${c.medico}`, especialidad: c.especialidad, medico: c.medico,
        estado: c.estado, recetas: [], lineas: [],
      })
    }
    for (const a of h.atenciones) {
      const citaId = a.recetas[0]?.citaId
      const clave = citaId !== undefined ? `cita-${citaId}` : `atencion-${a.fecha}-${a.hora}`
      const recetas = a.recetas.map(r => r.medicacion)
      const existente = eventos.get(clave)
      if (existente) {
        existente.tipo = 'ATENCION'
        existente.recetas.push(...recetas)
      } else {
        eventos.set(clave, { clave, tipo: 'ATENCION', fecha: a.fecha, hora: a.hora,
          titulo: `${nombreEspecialidad(a.especialidad)} · ${a.medico}`, especialidad: a.especialidad,
          medico: a.medico, estado: 'ATENDIDA', recetas, lineas: [] })
      }
    }
    for (const e of eventos.values()) {
      e.lineas = e.recetas.length ? e.recetas.map(r => `Receta: ${r}`)
        : e.estado ? [`Cita ${(NOMBRE_ESTADO[e.estado as keyof typeof NOMBRE_ESTADO] ?? e.estado).toLowerCase()}`] : []
    }
    for (const f of fichas.value) {
      const fecha = f.llegada.slice(0, 10)
      const lineas = [
        f.estado === 'EN_ESPERA' ? 'En espera: sin fecha todavía'
          : `Asignada: ${f.fecha ? fechaMes(f.fecha, false) : ''} · ${f.hora ?? ''} · ${f.medico ?? ''} · Consultorio ${f.consultorio ?? ''}`,
        `Motivo: ${f.motivo}`,
        `Gravedad: ${f.gravedad} (solo la ven el médico general y el pediatra)`,
      ]
      if (f.observaciones) lineas.push(`Observaciones: ${f.observaciones}`)
      eventos.set(`ficha-${f.ticket}`, { clave: `ficha-${f.ticket}`, tipo: 'FICHA_INTERNA', fecha, hora: f.llegada.slice(11, 16),
        titulo: `${nombreEspecialidad(f.especialidad)} · derivada por ${f.derivadoPor}`, especialidad: f.especialidad,
        medico: f.medico ?? '', estado: f.estado, recetas: [], lineas })
    }
    for (const c of cirugias.value) {
      eventos.set(`cirugia-${c.numero}`, { clave: `cirugia-${c.numero}`, tipo: 'CIRUGIA', fecha: c.fecha, hora: c.inicio,
        titulo: `${nombreEspecialidad(c.especialidad)} · ${c.cirujano}`, especialidad: c.especialidad, medico: c.cirujano,
        estado: c.estado, recetas: [],
        lineas: [`Quirófano ${c.quirofano} · ${c.inicio} – ${c.fin} · limpieza hasta ${c.limpiezaHasta}`] })
    }
    return [...eventos.values()].sort((x, y) => (y.fecha + y.hora).localeCompare(x.fecha + x.hora))
  })

  const eventosFiltrados = computed(() => {
    const tipos: Record<FiltroHistorial, TipoEvento[]> = {
      TODO: ['ATENCION', 'CITA', 'FICHA_INTERNA', 'CIRUGIA'],
      ATENCIONES: ['ATENCION'],
      FICHAS: ['FICHA_INTERNA'],
      CIRUGIAS: ['CIRUGIA'],
    }
    return lineaDeTiempo.value.filter(e => tipos[filtro.value].includes(e.tipo))
  })

  const totalRecetas = computed(() => historial.value?.atenciones.reduce((n, a) => n + a.recetas.length, 0) ?? 0)

  function usar(s: HistorialService, f?: FichasInternasService, c?: CirugiasService) {
    servicio = s
    if (f) fichasSrv = f
    if (c) cirugiasSrv = c
  }

  async function cargar(pacienteId: number) {
    if (!servicio) throw new Error('El store de historial no tiene servicio')
    cargando.value = true
    error.value = null
    sinPermiso.value = false
    historial.value = null
    fichas.value = []
    cirugias.value = []
    filtro.value = 'TODO'
    try {
      historial.value = await servicio.historial(pacienteId)
      fichas.value = (await fichasSrv?.delPaciente(pacienteId)) ?? []
      cirugias.value = (await cirugiasSrv?.delPaciente(pacienteId)) ?? []
    } catch (e) {
      sinPermiso.value = e instanceof ApiError && e.status === 403
      error.value = mensajeDeError(e)
    } finally {
      cargando.value = false
    }
  }

  return { historial, fichas, cirugias, filtro, cargando, error, sinPermiso, lineaDeTiempo, eventosFiltrados,
    totalRecetas, usar, cargar }
})
