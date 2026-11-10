import type { Especialidad } from './especialidad'

/** GET /api/v1/medico/yo */
export interface MedicoActual {
  medicoId: number
  nombre: string
  especialidad: Especialidad
  puedeDerivar: boolean
  operaEnQuirofano: boolean
}

/** Usuario de la sesion. Mientras no exista Spring Security viaja en los headers X-Usuario-Id, X-Rol, X-Persona-Id. */
export interface Sesion {
  usuarioId: number
  rol: string
  personaId: number
  nombre?: string
}

export interface OpcionMenu {
  texto: string
  ruta: string
  /** true = la pantalla todavia no existe (otra tarjeta); se muestra deshabilitada. */
  pendiente?: boolean
}
