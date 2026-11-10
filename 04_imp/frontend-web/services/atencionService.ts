import type { Api } from './apiClient'
import type { DatosAtencion, RegistroAtencion } from '~/models/atencion'

export interface AtencionService {
  /** GET /api/v1/citas/{id}/atencion */
  datos(citaId: number): Promise<DatosAtencion>
  /** POST /api/v1/citas/{id}/atencion: guarda la receta y finaliza la cita (ATENDIDA). */
  registrar(citaId: number, medicacion: string): Promise<RegistroAtencion>
}

export class AtencionServiceApi implements AtencionService {
  constructor(private api: Api) {}

  datos(citaId: number): Promise<DatosAtencion> {
    return this.api.get<DatosAtencion>(`/api/v1/citas/${citaId}/atencion`)
  }

  registrar(citaId: number, medicacion: string): Promise<RegistroAtencion> {
    return this.api.post<RegistroAtencion>(`/api/v1/citas/${citaId}/atencion`, { medicacion })
  }
}
