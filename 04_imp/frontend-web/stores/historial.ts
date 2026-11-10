import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { HistorialPaciente } from '~/models/historial'
import type { HistorialService } from '~/services/historialService'
import { ApiError, mensajeDeError } from '~/services/apiClient'

/** Un evento de la linea de tiempo: una cita pasada con sus recetas (si fue atendida). */
export interface EventoHistorial {
  clave: string
  fecha: string
  hora: string
  especialidad: string
  medico: string
  estado: string | null
  recetas: string[]
}

/** CU-14 en la web: historial del paciente (solo lectura). */
export const useHistorialStore = defineStore('historial', () => {
  let servicio: HistorialService | null = null

  const historial = ref<HistorialPaciente | null>(null)
  const cargando = ref(false)
  const error = ref<string | null>(null)
  const sinPermiso = ref(false)

  /** Une citas pasadas y atenciones en una sola linea de tiempo, de la mas reciente a la mas antigua. */
  const lineaDeTiempo = computed<EventoHistorial[]>(() => {
    const h = historial.value
    if (!h) return []
    const eventos = new Map<string, EventoHistorial>()
    for (const c of h.citasPasadas) {
      eventos.set(`cita-${c.citaId}`, {
        clave: `cita-${c.citaId}`, fecha: c.fecha, hora: c.hora, especialidad: c.especialidad, medico: c.medico,
        estado: c.estado, recetas: [],
      })
    }
    for (const a of h.atenciones) {
      const citaId = a.recetas[0]?.citaId
      const clave = citaId !== undefined ? `cita-${citaId}` : `atencion-${a.fecha}-${a.hora}`
      const existente = eventos.get(clave)
      const recetas = a.recetas.map(r => r.medicacion)
      if (existente) existente.recetas.push(...recetas)
      else eventos.set(clave, { clave, fecha: a.fecha, hora: a.hora, especialidad: a.especialidad, medico: a.medico,
        estado: 'ATENDIDA', recetas })
    }
    return [...eventos.values()].sort((x, y) => (y.fecha + y.hora).localeCompare(x.fecha + x.hora))
  })

  const totalRecetas = computed(() => historial.value?.atenciones.reduce((n, a) => n + a.recetas.length, 0) ?? 0)

  function usar(s: HistorialService) {
    servicio = s
  }

  async function cargar(pacienteId: number) {
    if (!servicio) throw new Error('El store de historial no tiene servicio')
    cargando.value = true
    error.value = null
    sinPermiso.value = false
    historial.value = null
    try {
      historial.value = await servicio.historial(pacienteId)
    } catch (e) {
      sinPermiso.value = e instanceof ApiError && e.status === 403
      error.value = mensajeDeError(e)
    } finally {
      cargando.value = false
    }
  }

  return { historial, cargando, error, sinPermiso, lineaDeTiempo, totalRecetas, usar, cargar }
})
