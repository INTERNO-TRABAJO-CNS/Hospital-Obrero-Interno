import type { AgendaService } from '../agendaService'
import type { AgendaDia, FichaAgenda } from '~/models/agenda'
import { hoyISO } from '~/models/fechas'

/** Sin backend: {@code porFecha} manda; si la fecha no esta, devuelve una agenda de ejemplo para hoy. */
export class FakeAgendaService implements AgendaService {
  porFecha: Record<string, FichaAgenda[]> = {}
  llamadas: (string | undefined)[] = []

  async agenda(fecha?: string): Promise<AgendaDia> {
    this.llamadas.push(fecha)
    const dia = fecha ?? hoyISO()
    const fichas = this.porFecha[dia] ?? (dia === hoyISO() && Object.keys(this.porFecha).length === 0 ? EJEMPLO : [])
    return { fecha: dia, cantidad: fichas.length, fichas: [...fichas] }
  }
}

const EJEMPLO: FichaAgenda[] = [
  { citaId: 1, numeroFicha: 1, hora: '08:00:00', nombrePaciente: 'Ana Quispe', estado: 'ATENDIDA', pacienteId: 101 },
  { citaId: 2, numeroFicha: 2, hora: '08:40:00', nombrePaciente: 'Juan Choque', estado: 'ASISTENCIA_CONFIRMADA',
    pacienteId: 100 },
  { citaId: 3, numeroFicha: 3, hora: '09:20:00', nombrePaciente: 'Lucía Flores', estado: 'CANCELADA', pacienteId: 102 },
  { citaId: 4, numeroFicha: 4, hora: '10:00:00', nombrePaciente: 'Mario Condori', estado: 'PROGRAMADA', pacienteId: 103 },
]
