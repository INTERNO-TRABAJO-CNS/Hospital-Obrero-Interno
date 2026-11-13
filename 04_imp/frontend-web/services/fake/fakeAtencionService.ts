import { ApiError } from '../apiClient'
import type { AtencionService } from '../atencionService'
import { MAX_RECETA, type DatosAtencion, type RegistroAtencion } from '~/models/atencion'
import { hoyISO } from '~/models/fechas'

/** Sin backend: imita AtencionController. Solo la cita confirmada del Figma (ficha 3, 09:30) se puede atender. */
export class FakeAtencionService implements AtencionService {
  datosPorCita: Record<number, DatosAtencion> = {
    503: {
      citaId: 503, pacienteId: 102, nombrePaciente: 'Elena Copa', numeroAfiliado: '4587-12', numeroFicha: 3,
      fecha: hoyISO(), hora: '09:30:00', consulta: 'CONSULTORIO_FAMILIAR', categoriaConsulta: 'BASICA',
      consultorio: 4, medico: 'Carlos Ramos', puedeDerivar: true, estado: 'ASISTENCIA_CONFIRMADA', edad: 34,
      titular: true, nombreConsultorio: 'Consultorio 4',
    },
  }

  registradas: { citaId: number; medicacion: string }[] = []

  async datos(citaId: number): Promise<DatosAtencion> {
    const d = this.datosPorCita[citaId]
    if (!d) throw new ApiError('NO_ENCONTRADA', 'La cita no existe', 404)
    if (d.estado && d.estado !== 'ASISTENCIA_CONFIRMADA') {
      throw new ApiError('ESTADO_NO_PERMITE', 'La cita debe tener la asistencia confirmada', 409)
    }
    return { ...d }
  }

  async registrar(citaId: number, medicacion: string): Promise<RegistroAtencion> {
    const d = this.datosPorCita[citaId]
    if (!d) throw new ApiError('NO_ENCONTRADA', 'La cita no existe', 404)
    const texto = medicacion.trim()
    if (!texto || texto.length > MAX_RECETA) {
      throw new ApiError('RECETA_INVALIDA', `La receta no puede estar vacía ni pasar de ${MAX_RECETA} caracteres`, 400)
    }
    this.registradas.push({ citaId, medicacion: texto })
    d.estado = 'ATENDIDA'
    return { resultado: 'REGISTRADA', mensaje: 'Atención registrada' }
  }
}
