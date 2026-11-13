import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { AgendaDia, FichaAgenda } from '~/models/agenda'
import type { FichaInterna } from '~/models/fichaInterna'
import { fechaTitulo, hoyISO } from '~/models/fechas'
import type { MedicoActual } from '~/models/medico'
import type { AgendaService } from '~/services/agendaService'
import type { FichasInternasService } from '~/services/fichasInternasService'
import { mensajeDeError } from '~/services/apiClient'

/**
 * CU-13 en la web: agenda del dia (hoy por defecto) y selector de otras fechas. La fecha elegida se recuerda
 * mientras dure la sesion: al volver de una atencion o del historial se ve el mismo dia.
 */
export const useAgendaStore = defineStore('agenda', () => {
  let servicio: AgendaService | null = null
  let fichas: FichasInternasService | null = null
  /** Para probar con otra fecha de "hoy". */
  let hoy: () => string = () => hoyISO()

  const agenda = ref<AgendaDia | null>(null)
  const fechaElegida = ref<string | null>(null)
  /** Ficha interna generada desde cada cita (una sola por cita). */
  const derivaciones = ref<Record<number, FichaInterna>>({})
  const cargando = ref(false)
  const error = ref<string | null>(null)
  let pedido = 0

  const esHoy = computed(() => agenda.value?.fecha === hoy())
  const titulo = computed(() => {
    const n = agenda.value?.cantidad ?? 0
    const citas = n === 1 ? '1 cita' : `${n} citas`
    return esHoy.value ? `${citas} hoy` : `${citas} este día`
  })
  /** «Jueves 12 de noviembre · 6 citas hoy» (Figma). */
  const resumen = computed(() => agenda.value ? `${fechaTitulo(agenda.value.fecha)} · ${titulo.value}` : 'Cargando…')

  function usar(s: AgendaService, reloj?: () => string, servicioFichas?: FichasInternasService) {
    servicio = s
    if (reloj) hoy = reloj
    if (servicioFichas) fichas = servicioFichas
  }

  /** Carga la fecha pedida, o la ultima elegida, o hoy. Ignora respuestas viejas si se pidio otra fecha. */
  async function cargar(fecha?: string) {
    if (!servicio) throw new Error('El store de agenda no tiene servicio')
    if (fecha !== undefined) fechaElegida.value = fecha === hoy() ? null : fecha
    const pedida = fecha ?? fechaElegida.value ?? undefined
    const numero = ++pedido
    cargando.value = true
    error.value = null
    try {
      const respuesta = await servicio.agenda(pedida)
      if (numero !== pedido) return
      agenda.value = respuesta
      derivaciones.value = await buscarDerivaciones(respuesta.fichas)
    } catch (e) {
      if (numero === pedido) error.value = mensajeDeError(e)
    } finally {
      if (numero === pedido) cargando.value = false
    }
  }

  function volverAHoy() {
    return cargar(hoy())
  }

  async function buscarDerivaciones(lista: FichaAgenda[]): Promise<Record<number, FichaInterna>> {
    if (!fichas) return {}
    const resultado: Record<number, FichaInterna> = {}
    for (const f of lista) {
      if (f.estado === 'ATENDIDA' || f.estado === 'ASISTENCIA_CONFIRMADA') {
        const ficha = await fichas.deCita(f.citaId)
        if (ficha) resultado[f.citaId] = ficha
      }
    }
    return resultado
  }

  /** «Atender»: solo cuando enfermeria ya confirmo la asistencia. */
  function puedeAtender(f: FichaAgenda): boolean {
    return f.estado === 'ASISTENCIA_CONFIRMADA'
  }

  /** «Ticket interno»: medico que deriva, cita de hoy confirmada o atendida, y todavia sin derivacion. */
  function puedeTicketInterno(f: FichaAgenda, medico: MedicoActual | null): boolean {
    return !!medico?.puedeDerivar && esHoy.value && !derivaciones.value[f.citaId]
      && (f.estado === 'ASISTENCIA_CONFIRMADA' || f.estado === 'ATENDIDA')
  }

  return { agenda, fechaElegida, derivaciones, cargando, error, esHoy, titulo, resumen, usar, cargar, volverAHoy,
    puedeAtender, puedeTicketInterno }
})
