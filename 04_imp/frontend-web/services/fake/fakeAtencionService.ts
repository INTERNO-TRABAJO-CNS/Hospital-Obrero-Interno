import { ApiError } from '../apiClient'
import type { AtencionService } from '../atencionService'
import { MAX_RECETA, type DatosAtencion, type RegistroAtencion } from '~/models/atencion'
import { hoyISO } from '~/models/fechas'

/** Sin backend: imita las respuestas de AtencionController. {@code registradas} guarda cada envio. */
export class FakeAtencionService implements AtencionService {
  datosPorCita: Record<number, DatosAtencion> = {
    2: {
      citaId: 2, pacienteId: 100, nombrePaciente: 'Juan Choque', numeroAfiliado: 'AF-10002', numeroFicha: 2,
      fecha: hoyISO(), hora: '08:40:00', consulta: 'CONSULTORIO_FAMILIAR', categoriaConsulta: 'BASICA',
      consultorio: 3, medico: 'Rosa Mamani', puedeDerivar: true,
    },
  }

  registradas: { citaId: number; medicacion: string }[] = []

  async datos(citaId: number): Promise<DatosAtencion> {
    const d = this.datosPorCita[citaId]
    if (!d) throw new ApiError('NO_ENCONTRADA', 'La cita no existe', 404)
    return { ...d }
  }

  async registrar(citaId: number, medicacion: string): Promise<RegistroAtencion> {
    if (!this.datosPorCita[citaId]) throw new ApiError('NO_ENCONTRADA', 'La cita no existe', 404)
    const texto = medicacion.trim()
    if (!texto || texto.length > MAX_RECETA) {
      throw new ApiError('RECETA_INVALIDA', `La receta no puede estar vacía ni pasar de ${MAX_RECETA} caracteres`, 400)
    }
    this.registradas.push({ citaId, medicacion: texto })
    return { resultado: 'REGISTRADA', mensaje: 'Atención registrada' }
  }
}
