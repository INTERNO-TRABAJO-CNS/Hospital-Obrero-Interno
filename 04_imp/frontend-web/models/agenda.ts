export type EstadoCita = 'PROGRAMADA' | 'ASISTENCIA_CONFIRMADA' | 'ATENDIDA' | 'CANCELADA'

export const NOMBRE_ESTADO: Record<EstadoCita, string> = {
  PROGRAMADA: 'Programada',
  ASISTENCIA_CONFIRMADA: 'Asistencia confirmada',
  ATENDIDA: 'Atendida',
  CANCELADA: 'Cancelada',
}

/** Una fila de GET /api/v1/medico/agenda */
export interface FichaAgenda {
  citaId: number
  numeroFicha: number
  hora: string // HH:mm:ss
  nombrePaciente: string
  estado: EstadoCita
  pacienteId: number
}

export interface AgendaDia {
  fecha: string // YYYY-MM-DD
  cantidad: number
  fichas: FichaAgenda[]
}
