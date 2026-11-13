import type { AgendaService } from '../agendaService'
import type { AgendaDia, FichaAgenda } from '~/models/agenda'
import { hoyISO } from '~/models/fechas'

/**
 * Sin backend: {@code porFecha} manda; si no hay nada cargado, hoy devuelve la agenda del Figma
 * («WEB MEDICO AGENDA»: 6 citas del Dr. Ramos con los 4 estados).
 */
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

export const EJEMPLO: FichaAgenda[] = [
  { citaId: 501, numeroFicha: 1, hora: '08:00:00', nombrePaciente: 'Marcos Condori', estado: 'ATENDIDA', pacienteId: 100 },
  { citaId: 502, numeroFicha: 2, hora: '08:45:00', nombrePaciente: 'Ana Mamani', estado: 'ATENDIDA', pacienteId: 101 },
  { citaId: 503, numeroFicha: 3, hora: '09:30:00', nombrePaciente: 'Elena Copa', estado: 'ASISTENCIA_CONFIRMADA',
    pacienteId: 102 },
  { citaId: 504, numeroFicha: 4, hora: '10:15:00', nombrePaciente: 'Luis Quispe', estado: 'PROGRAMADA', pacienteId: 103 },
  { citaId: 505, numeroFicha: 5, hora: '11:00:00', nombrePaciente: 'Rosa Choque', estado: 'PROGRAMADA', pacienteId: 104 },
  { citaId: 506, numeroFicha: 6, hora: '11:45:00', nombrePaciente: 'Jorge Flores', estado: 'CANCELADA', pacienteId: 105 },
]
