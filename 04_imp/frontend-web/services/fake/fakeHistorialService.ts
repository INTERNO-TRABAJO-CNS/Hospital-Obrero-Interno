import { ApiError } from '../apiClient'
import type { HistorialService } from '../historialService'
import type { HistorialPaciente } from '~/models/historial'

/** Sin backend: {@code porPaciente} manda; un paciente que no esta responde PACIENTE_FUERA_DE_AGENDA. */
export class FakeHistorialService implements HistorialService {
  porPaciente: Record<number, HistorialPaciente> = {
    100: {
      pacienteId: 100,
      nombrePaciente: 'Juan Choque',
      citasPasadas: [
        { citaId: 11, fecha: '2026-10-28', hora: '09:00:00', especialidad: 'CONSULTORIO_FAMILIAR', medico: 'Rosa Mamani',
          numeroFicha: 3, estado: 'ATENDIDA' },
        { citaId: 10, fecha: '2026-09-15', hora: '10:30:00', especialidad: 'ODONTOLOGIA', medico: 'Luis Choque',
          numeroFicha: 1, estado: 'CANCELADA' },
      ],
      atenciones: [
        { fecha: '2026-10-28', hora: '09:00:00', especialidad: 'CONSULTORIO_FAMILIAR', medico: 'Rosa Mamani',
          recetas: [{ recetaId: 1, citaId: 11, pacienteId: 100, medicacion: 'Paracetamol 500 mg cada 8 h por 3 días',
            fechaHora: '2026-10-28T09:35:00', numeroFicha: 3 }] },
      ],
    },
  }

  async historial(pacienteId: number): Promise<HistorialPaciente> {
    const h = this.porPaciente[pacienteId]
    if (!h) {
      throw new ApiError('PACIENTE_FUERA_DE_AGENDA',
        'Solo puede ver el historial de pacientes que tienen cita en su agenda', 403)
    }
    return structuredClone(h)
  }
}
