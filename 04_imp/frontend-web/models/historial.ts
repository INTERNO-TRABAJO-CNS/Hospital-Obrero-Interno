import type { EstadoCita } from './agenda'
import type { Especialidad } from './especialidad'

export interface Receta {
  recetaId: number
  citaId: number
  pacienteId: number
  medicacion: string
  fechaHora: string
  numeroFicha: number
}

export interface Atencion {
  fecha: string
  hora: string
  especialidad: string
  medico: string
  recetas: Receta[]
}

export interface CitaPasada {
  citaId: number
  fecha: string
  hora: string
  especialidad: Especialidad
  medico: string
  numeroFicha: number
  estado: EstadoCita
}

/** GET /api/v1/medico/pacientes/{id}/historial */
export interface HistorialPaciente {
  pacienteId: number
  nombrePaciente: string
  citasPasadas: CitaPasada[]
  atenciones: Atencion[]
}
