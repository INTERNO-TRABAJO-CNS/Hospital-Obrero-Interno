import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { AgendaDia, FichaAgenda } from '~/models/agenda'
import { hoyISO } from '~/models/fechas'
import type { MedicoActual } from '~/models/medico'
import type { AgendaService } from '~/services/agendaService'
import { mensajeDeError } from '~/services/apiClient'

/** CU-13 en la web: agenda del dia (hoy por defecto) y selector de otras fechas. */
export const useAgendaStore = defineStore('agenda', () => {
  let servicio: AgendaService | null = null
  /** Para probar con otra fecha de "hoy". */
  let hoy: () => string = () => hoyISO()

  const agenda = ref<AgendaDia | null>(null)
  const cargando = ref(false)
  const error = ref<string | null>(null)

  const esHoy = computed(() => agenda.value?.fecha === hoy())
  const titulo = computed(() => {
    const n = agenda.value?.cantidad ?? 0
    const citas = n === 1 ? '1 cita' : `${n} citas`
    return esHoy.value ? `${citas} hoy` : `${citas} este día`
  })

  function usar(s: AgendaService, reloj?: () => string) {
    servicio = s
    if (reloj) hoy = reloj
  }

  async function cargar(fecha?: string) {
    if (!servicio) throw new Error('El store de agenda no tiene servicio')
    cargando.value = true
    error.value = null
    try {
      agenda.value = await servicio.agenda(fecha || undefined)
    } catch (e) {
      error.value = mensajeDeError(e)
    } finally {
      cargando.value = false
    }
  }

  /** «Atender»: solo cuando enfermeria ya confirmo la asistencia. */
  function puedeAtender(f: FichaAgenda): boolean {
    return f.estado === 'ASISTENCIA_CONFIRMADA'
  }

  /** «Ticket interno» (T100-27): medico que deriva, cita de hoy, con asistencia confirmada o atendida. */
  function puedeTicketInterno(f: FichaAgenda, medico: MedicoActual | null): boolean {
    return !!medico?.puedeDerivar && esHoy.value
      && (f.estado === 'ASISTENCIA_CONFIRMADA' || f.estado === 'ATENDIDA')
  }

  return { agenda, cargando, error, esHoy, titulo, usar, cargar, puedeAtender, puedeTicketInterno }
})
