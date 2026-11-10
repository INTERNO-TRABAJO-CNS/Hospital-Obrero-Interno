import type { Api } from './apiClient'
import type { AgendaDia } from '~/models/agenda'

export interface AgendaService {
  /** GET /api/v1/medico/agenda?fecha=YYYY-MM-DD (sin fecha = hoy) */
  agenda(fecha?: string): Promise<AgendaDia>
}

export class AgendaServiceApi implements AgendaService {
  constructor(private api: Api) {}

  agenda(fecha?: string): Promise<AgendaDia> {
    const consulta = fecha ? `?fecha=${encodeURIComponent(fecha)}` : ''
    return this.api.get<AgendaDia>(`/api/v1/medico/agenda${consulta}`)
  }
}
