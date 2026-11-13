import type { Especialidad } from './especialidad'

/** CU-21/CU-22: ficha interna (derivacion de Medico General o Pediatria a una especialidad interna). */
export type Gravedad = 'ALTA' | 'MEDIA' | 'BAJA'
export type EstadoFicha = 'EN_ESPERA' | 'ASIGNADA' | 'ATENDIDA' | 'CANCELADA'
/** Las 20 tablas: cada especialidad tiene una tabla GENERAL y una PEDIATRIA. */
export type TablaFicha = 'GENERAL' | 'PEDIATRIA'

export const MAX_MOTIVO = 500
export const EDAD_PEDIATRIA = 10
export const EDAD_ADULTO_MAYOR = 60

export interface FichaInterna {
  /** N° de ticket (1024). */
  ticket: number
  citaOrigenId: number | null
  pacienteId: number
  nombrePaciente: string
  numeroAfiliado: string
  edad: number
  especialidad: Especialidad
  tabla: TablaFicha
  urgencia: boolean
  /** Solo la ve el medico que derivo (general o pediatra): nunca sale en el ticket. */
  gravedad: Gravedad
  motivo: string
  estado: EstadoFicha
  /** YYYY-MM-DD y HH:mm; null mientras esta EN_ESPERA. */
  fecha: string | null
  hora: string | null
  duracionMin: number
  medico: string | null
  consultorio: string | null
  derivadoPor: string
  especialidadOrigen: Especialidad
  /** Momento de la derivacion (ISO): desempata la cola por orden de llegada. */
  llegada: string
  observaciones?: string
}

/** Lo que llena el medico en «Nueva ficha interna». */
export interface SolicitudFicha {
  citaOrigenId: number | null
  pacienteId: number
  nombrePaciente: string
  numeroAfiliado: string
  edad: number
  especialidad: Especialidad
  urgencia: boolean
  gravedad: Gravedad
  motivo: string
}

/** Datos de la cita desde la que se deriva (para validar CITA_NO_VALIDA / CITA_YA_DERIVADA). */
export interface CitaOrigen {
  citaId: number
  numeroFicha: number
  fecha: string
  hora: string
  estado: string
}

export interface PacienteBuscado {
  pacienteId: number
  nombre: string
  numeroAfiliado: string
  edad: number
}

export type ResultadoFicha =
  | { resultado: 'ASIGNADA' | 'EN_ESPERA'; ficha: FichaInterna }
  | { resultado: 'RECHAZADA'; codigo: string; mensaje: string }

/** Rechazos posibles al generar la ficha (Figma «TICKET RECHAZADO»). */
export const RECHAZOS_FICHA: { codigo: string; descripcion: string }[] = [
  { codigo: 'MEDICO_NO_DERIVA', descripcion: 'Solo Médico General y Pediatría pueden derivar.' },
  { codigo: 'CITA_NO_VALIDA', descripcion: 'La cita debe ser tuya, de hoy y estar confirmada o atendida.' },
  { codigo: 'CITA_YA_DERIVADA', descripcion: 'Una sola derivación por cita.' },
  { codigo: 'TERAPIA_SOLO_URGENCIA', descripcion: 'Terapia Intensiva solo con Urgencia.' },
  { codigo: 'EDAD_NO_PEDIATRICA', descripcion: 'Pediatría solo atiende a menores de 10 años.' },
  { codigo: 'PACIENTE_CON_FICHA_ACTIVA', descripcion: 'El paciente ya tiene una ficha interna activa.' },
  { codigo: 'MOTIVO_INVALIDO', descripcion: `El motivo debe tener de 1 a ${MAX_MOTIVO} caracteres.` },
]

const PESO_GRAVEDAD: Record<Gravedad, number> = { ALTA: 0, MEDIA: 1, BAJA: 2 }

/** Regla unica de prioridad: urgencia > gravedad (ALTA > MEDIA > BAJA) > 60 o mas anios > orden de llegada. */
export function compararPrioridad(a: FichaInterna, b: FichaInterna): number {
  if (a.urgencia !== b.urgencia) return a.urgencia ? -1 : 1
  if (a.gravedad !== b.gravedad) return PESO_GRAVEDAD[a.gravedad] - PESO_GRAVEDAD[b.gravedad]
  const mayorA = a.edad >= EDAD_ADULTO_MAYOR
  const mayorB = b.edad >= EDAD_ADULTO_MAYOR
  if (mayorA !== mayorB) return mayorA ? -1 : 1
  return a.llegada.localeCompare(b.llegada)
}

/** Ficha que todavia ocupa al paciente (PV-21: una sola activa a la vez). */
export function fichaActiva(f: FichaInterna): boolean {
  return f.estado === 'ASIGNADA' || f.estado === 'EN_ESPERA'
}
