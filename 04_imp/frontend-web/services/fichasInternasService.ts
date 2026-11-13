import type { Especialidad } from '~/models/especialidad'
import type {
  CitaOrigen, FichaInterna, PacienteBuscado, ResultadoFicha, SolicitudFicha, TablaFicha,
} from '~/models/fichaInterna'
import type { MedicoActual } from '~/models/medico'

/**
 * CU-21..CU-23 (fichas internas). PENDIENTE T100-20/21/22: todavia no hay endpoints en el backend, por eso la
 * web usa siempre FakeFichasInternasService (datos del Figma). Cuando existan, se agrega FichasInternasServiceApi
 * con esta misma interfaz y solo cambia proveedor.ts.
 */
export interface FichasInternasService {
  /** «Generar»: valida, asigna (o deja EN_ESPERA) o rechaza con su codigo. */
  generar(solicitud: SolicitudFicha, medico: MedicoActual, cita: CitaOrigen | null): Promise<ResultadoFicha>
  /** Ficha generada desde esa cita (una sola derivacion por cita). */
  deCita(citaId: number): Promise<FichaInterna | null>
  porTicket(ticket: number): Promise<FichaInterna | null>
  delPaciente(pacienteId: number): Promise<FichaInterna[]>
  /** Cola de prioridad de una especialidad (solo lectura), ya ordenada. */
  cola(especialidad: Especialidad, tabla: TablaFicha): Promise<FichaInterna[]>
  /** Urgencia sin cita: buscar al paciente por N° de afiliado. */
  buscarPaciente(numeroAfiliado: string): Promise<PacienteBuscado | null>
  /** Agenda del especialista: sus fichas de ese dia, ordenadas por hora. */
  agendaEspecialista(medico: MedicoActual, fecha: string): Promise<FichaInterna[]>
  marcarAtendida(ticket: number, observaciones: string): Promise<FichaInterna>
  noSePresento(ticket: number): Promise<FichaInterna>
}
