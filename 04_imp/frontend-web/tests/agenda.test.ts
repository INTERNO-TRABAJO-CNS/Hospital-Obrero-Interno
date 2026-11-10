import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import type { FichaAgenda } from '~/models/agenda'
import type { MedicoActual } from '~/models/medico'
import { FakeAgendaService } from '~/services/fake/fakeAgendaService'
import { useAgendaStore } from '~/stores/agenda'

const HOY = '2026-11-10'

const ficha = (citaId: number, estado: FichaAgenda['estado']): FichaAgenda => ({
  citaId, numeroFicha: citaId, hora: `0${citaId}:00:00`, nombrePaciente: `P${citaId}`, estado, pacienteId: 100 + citaId,
})

const general: MedicoActual = {
  medicoId: 7, nombre: 'Rosa', especialidad: 'CONSULTORIO_FAMILIAR', puedeDerivar: true, operaEnQuirofano: false,
}
const odontologo: MedicoActual = { ...general, especialidad: 'ODONTOLOGIA', puedeDerivar: false }

describe('agenda del medico', () => {
  let fake: FakeAgendaService

  beforeEach(() => {
    setActivePinia(createPinia())
    fake = new FakeAgendaService()
    fake.porFecha[HOY] = [ficha(1, 'ATENDIDA'), ficha(2, 'ASISTENCIA_CONFIRMADA'), ficha(3, 'CANCELADA')]
  })

  it('contadorCitasHoy', async () => {
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)

    await agenda.cargar(HOY)

    expect(agenda.titulo).toBe('3 citas hoy')
  })

  it('otra fecha cambia el encabezado y pide esa fecha', async () => {
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)

    await agenda.cargar('2026-11-12')

    expect(fake.llamadas).toEqual(['2026-11-12'])
    expect(agenda.titulo).toBe('0 citas este día')
  })

  it('canceladaMarcada', async () => {
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)
    await agenda.cargar(HOY)

    expect(agenda.agenda?.fichas.find(f => f.citaId === 3)?.estado).toBe('CANCELADA')
  })

  it('«Atender» solo con asistencia confirmada', async () => {
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)
    await agenda.cargar(HOY)

    const [atendida, confirmada, cancelada] = agenda.agenda!.fichas
    expect(agenda.puedeAtender(confirmada)).toBe(true)
    expect(agenda.puedeAtender(atendida)).toBe(false)
    expect(agenda.puedeAtender(cancelada)).toBe(false)
  })

  it('ticketInternoSoloSiPuedeDerivar', async () => {
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)
    await agenda.cargar(HOY)

    const [atendida, confirmada, cancelada] = agenda.agenda!.fichas
    expect(agenda.puedeTicketInterno(confirmada, general)).toBe(true)
    expect(agenda.puedeTicketInterno(atendida, general)).toBe(true)
    expect(agenda.puedeTicketInterno(cancelada, general)).toBe(false)
    expect(agenda.puedeTicketInterno(confirmada, odontologo)).toBe(false)
  })

  it('ticket interno solo para citas de hoy', async () => {
    fake.porFecha['2026-11-09'] = [ficha(4, 'ATENDIDA')]
    const agenda = useAgendaStore()
    agenda.usar(fake, () => HOY)
    await agenda.cargar('2026-11-09')

    expect(agenda.puedeTicketInterno(agenda.agenda!.fichas[0], general)).toBe(false)
  })
})
