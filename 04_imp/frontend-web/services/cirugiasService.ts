import type { Cirugia, DatosCirugia, PropuestaCirugia } from '~/models/cirugia'
import type { MedicoActual } from '~/models/medico'

/**
 * CU-24 (cirugias). PENDIENTE T100-23: todavia no hay endpoints en el backend, por eso la web usa siempre
 * FakeCirugiasService (datos del Figma). La regla de cero choques la garantizara la BD con EXCLUDE.
 */
export interface CirugiasService {
  /** «Proponer horario»: calcula el primer hueco sin choques; no guarda nada. */
  proponer(datos: DatosCirugia, medico: MedicoActual): Promise<PropuestaCirugia>
  /** «Confirmar»: guarda la propuesta; si alguien tomo ese horario lanza CHOQUE_DE_HORARIO. */
  confirmar(datos: DatosCirugia, propuesta: PropuestaCirugia, medico: MedicoActual): Promise<Cirugia>
  /** Cirugias del dia en los quirofanos de la especialidad. */
  delDia(medico: MedicoActual, fecha: string): Promise<Cirugia[]>
  delPaciente(pacienteId: number): Promise<Cirugia[]>
  porNumero(numero: number): Promise<Cirugia | null>
  marcar(numero: number, estado: 'REALIZADA' | 'CANCELADA'): Promise<Cirugia>
}
