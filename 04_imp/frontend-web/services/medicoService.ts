import type { Api } from './apiClient'
import type { MedicoActual, Sesion } from '~/models/medico'

export interface MedicoService {
  /** GET /api/v1/medico/yo */
  yo(): Promise<MedicoActual>
  /** POST /api/v1/auth/login: devuelve el token de la sesion. */
  login(usuario: string, clave: string): Promise<Sesion>
  /** POST /api/v1/auth/logout: el token deja de servir. */
  logout(): Promise<void>
}

export class MedicoServiceApi implements MedicoService {
  constructor(private api: Api) {}

  yo(): Promise<MedicoActual> {
    return this.api.get<MedicoActual>('/api/v1/medico/yo')
  }

  login(usuario: string, clave: string): Promise<Sesion> {
    return this.api.post<Sesion>('/api/v1/auth/login', { usuario, clave })
  }

  async logout(): Promise<void> {
    await this.api.post('/api/v1/auth/logout')
  }
}
