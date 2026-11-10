import type { Api } from './apiClient'
import type { HistorialPaciente } from '~/models/historial'

export interface HistorialService {
  /** GET /api/v1/medico/pacientes/{pacienteId}/historial */
  historial(pacienteId: number): Promise<HistorialPaciente>
}

export class HistorialServiceApi implements HistorialService {
  constructor(private api: Api) {}

  historial(pacienteId: number): Promise<HistorialPaciente> {
    return this.api.get<HistorialPaciente>(`/api/v1/medico/pacientes/${pacienteId}/historial`)
  }
}
