import { ApiError } from '../apiClient'
import type { HistorialService } from '../historialService'
import type { HistorialPaciente, PacienteDeAgenda } from '~/models/historial'

const receta = (recetaId: number, citaId: number, pacienteId: number, medicacion: string, fechaHora: string) =>
  ({ recetaId, citaId, pacienteId, medicacion, fechaHora, numeroFicha: 1 })

/** Sin backend: {@code porPaciente} manda; un paciente que no esta responde PACIENTE_FUERA_DE_AGENDA. */
export class FakeHistorialService implements HistorialService {
  porPaciente: Record<number, HistorialPaciente> = {
    102: {
      pacienteId: 102, nombrePaciente: 'Elena Copa', numeroAfiliado: '4587-12', titular: true, edad: 34,
      citasPasadas: [
        { citaId: 31, fecha: '2026-10-20', hora: '10:00:00', especialidad: 'ODONTOLOGIA', medico: 'Jorge Mamani',
          numeroFicha: 2, estado: 'ATENDIDA' },
        { citaId: 30, fecha: '2026-09-03', hora: '08:40:00', especialidad: 'CONSULTORIO_FAMILIAR',
          medico: 'Carlos Ramos', numeroFicha: 1, estado: 'ATENDIDA' },
      ],
      atenciones: [
        { fecha: '2026-10-20', hora: '10:00:00', especialidad: 'ODONTOLOGIA', medico: 'Jorge Mamani',
          recetas: [receta(2, 31, 102, 'Ibuprofeno 400 mg cada 8 h por 3 días.', '2026-10-20T10:40:00')] },
        { fecha: '2026-09-03', hora: '08:40:00', especialidad: 'CONSULTORIO_FAMILIAR', medico: 'Carlos Ramos',
          recetas: [receta(1, 30, 102, 'Paracetamol 500 mg si hay fiebre.', '2026-09-03T09:10:00')] },
      ],
    },
    100: {
      pacienteId: 100, nombrePaciente: 'Marcos Condori', numeroAfiliado: '3987-03', titular: false, edad: 47,
      citasPasadas: [
        { citaId: 40, fecha: '2026-10-14', hora: '09:30:00', especialidad: 'CONSULTORIO_FAMILIAR',
          medico: 'Carlos Ramos', numeroFicha: 3, estado: 'ATENDIDA' },
        { citaId: 41, fecha: '2026-08-02', hora: '11:00:00', especialidad: 'ODONTOLOGIA', medico: 'Jorge Mamani',
          numeroFicha: 5, estado: 'CANCELADA' },
      ],
      atenciones: [
        { fecha: '2026-10-14', hora: '09:30:00', especialidad: 'CONSULTORIO_FAMILIAR', medico: 'Carlos Ramos',
          recetas: [receta(3, 40, 100, 'Losartán 50 mg cada 24 h por 30 días.', '2026-10-14T10:05:00')] },
      ],
    },
    101: {
      pacienteId: 101, nombrePaciente: 'Ana Mamani', numeroAfiliado: '2290-15', titular: true, edad: 41,
      citasPasadas: [], atenciones: [],
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

  async buscar(numeroAfiliado: string): Promise<PacienteDeAgenda[]> {
    return Object.values(this.porPaciente)
      .filter(h => h.numeroAfiliado === numeroAfiliado.trim())
      .map(h => ({ pacienteId: h.pacienteId, nombre: h.nombrePaciente, numeroAfiliado: h.numeroAfiliado ?? '',
        titular: !!h.titular, edad: h.edad ?? 0 }))
  }
}
