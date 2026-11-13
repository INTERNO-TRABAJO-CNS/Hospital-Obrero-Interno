import type { Api } from './apiClient'
import type { HistorialPaciente, PacienteDeAgenda } from '~/models/historial'

export interface HistorialService {
  /** GET /api/v1/medico/pacientes/{pacienteId}/historial */
  historial(pacienteId: number): Promise<HistorialPaciente>
  /** GET /api/v1/medico/pacientes?afiliado= : solo pacientes de su agenda. */
  buscar(numeroAfiliado: string): Promise<PacienteDeAgenda[]>
}

export class HistorialServiceApi implements HistorialService {
  constructor(private api: Api) {}

  historial(pacienteId: number): Promise<HistorialPaciente> {
    return this.api.get<HistorialPaciente>(`/api/v1/medico/pacientes/${pacienteId}/historial`)
  }

  buscar(numeroAfiliado: string): Promise<PacienteDeAgenda[]> {
    return this.api.get<PacienteDeAgenda[]>(`/api/v1/medico/pacientes?afiliado=${encodeURIComponent(numeroAfiliado)}`)
  }
}
