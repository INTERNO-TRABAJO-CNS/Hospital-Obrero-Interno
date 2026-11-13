import type { EstadoCita } from './agenda'
import type { CategoriaEspecialidad, Especialidad } from './especialidad'

/** GET /api/v1/citas/{id}/atencion: datos autocompletados. */
export interface DatosAtencion {
  citaId: number
  pacienteId: number
  nombrePaciente: string
  numeroAfiliado: string
  numeroFicha: number
  fecha: string
  hora: string
  /** Especialidad de la cita: basica (General, Odontologia, Pediatria) o interna. */
  consulta: Especialidad
  categoriaConsulta: CategoriaEspecialidad
  consultorio: number
  medico: string
  puedeDerivar: boolean
  estado?: EstadoCita
  /** null si el backend no conoce al paciente. */
  edad?: number | null
  titular?: boolean | null
  /** «Consultorio 1»; null si el backend no lo conoce (se muestra el numero). */
  nombreConsultorio?: string | null
}

export type ResultadoAtencion =
  | 'REGISTRADA'
  | 'RECETA_INVALIDA'
  | 'NO_ES_DE_SU_AGENDA'
  | 'ESTADO_NO_PERMITE'
  | 'NO_ENCONTRADA'

/** POST /api/v1/citas/{id}/atencion */
export interface RegistroAtencion {
  resultado: ResultadoAtencion
  mensaje: string
}

export const MAX_RECETA = 2000
