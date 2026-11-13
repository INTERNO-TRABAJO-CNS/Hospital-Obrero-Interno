import type { Especialidad } from './especialidad'

/** GET /api/v1/medico/yo */
export interface MedicoActual {
  medicoId: number
  nombre: string
  especialidad: Especialidad
  puedeDerivar: boolean
  operaEnQuirofano: boolean
}

/**
 * POST /api/v1/auth/login. El token viaja en cada peticion como «Authorization: Bearer <token>»
 * (decision PV-11 del 13/11/2026) hasta {@code venceEn}.
 */
export interface Sesion {
  usuarioId: number
  rol: string
  personaId: number
  nombre?: string
  token: string
  venceEn?: string
  numeroAfiliado?: string | null
}

export interface OpcionMenu {
  texto: string
  ruta: string
}
