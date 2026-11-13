import type { Especialidad } from './especialidad'
import type { Gravedad } from './fichaInterna'

/** CU-24: cirugias en quirofano. Los quirofanos no se comparten entre especialidades. */
export type EstadoCirugia = 'PROGRAMADA' | 'REALIZADA' | 'CANCELADA'

export interface Quirofano {
  codigo: string
  especialidad: Especialidad
  /** Limpieza despues de cada cirugia (30 a 45 min). */
  limpiezaMin: number
}

export interface Cirugia {
  numero: number
  fichaTicket: number
  pacienteId: number
  nombrePaciente: string
  numeroAfiliado: string
  edad: number
  especialidad: Especialidad
  quirofano: string
  cirujano: string
  fecha: string
  inicio: string
  fin: string
  limpiezaHasta: string
  duracionMin: number
  urgente: boolean
  gravedad: Gravedad
  estado: EstadoCirugia
}

export interface DatosCirugia {
  fichaTicket: number
  duracionMin: number
  gravedad: Gravedad
  urgente: boolean
}

/** Lo que calcula «Proponer horario» (no guarda nada). */
export interface PropuestaCirugia {
  quirofano: string
  especialidad: Especialidad
  fecha: string
  inicio: string
  fin: string
  limpiezaHasta: string
  limpiezaMin: number
  /** Cirugia no urgente que una urgencia desplazaria, con su nuevo horario. */
  desplaza: (Cirugia & { nuevaFecha: string; nuevoInicio: string }) | null
}

/** Rechazos posibles (Figma «CIRUGIA CHOQUE DE HORARIO»). */
export const RECHAZOS_CIRUGIA: { codigo: string; descripcion: string }[] = [
  { codigo: 'DURACION_FUERA_DE_RANGO', descripcion: 'La duración no está en el rango de cirugía de tu especialidad '
      + '(Gastroenterología 30–120 min, Hematología 30–120, Dermatología 15–120).' },
  { codigo: 'ESPECIALIDAD_NO_OPERA', descripcion: 'Infectología, Medicina del Trabajo y Terapia Intensiva no operan '
      + 'en quirófano.' },
  { codigo: 'FICHA_NO_ATENDIDA', descripcion: 'Solo el especialista que atendió la ficha puede programar la cirugía.' },
]

/** Horario de los quirofanos (PV-19): 07:00 a 19:00; las urgencias pueden operarse fuera de horario. */
export const QUIROFANO_ABRE = '07:00'
export const QUIROFANO_CIERRA = '19:00'
