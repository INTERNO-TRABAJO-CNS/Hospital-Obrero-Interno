import { ApiError } from '../apiClient'
import type { MedicoService } from '../medicoService'
import type { MedicoActual, Sesion } from '~/models/medico'

/** Sin backend: un medico general que puede derivar. Cambia {@code medico} en las pruebas. */
export class FakeMedicoService implements MedicoService {
  medico: MedicoActual = {
    medicoId: 7,
    nombre: 'Rosa Mamani',
    especialidad: 'CONSULTORIO_FAMILIAR',
    puedeDerivar: true,
    operaEnQuirofano: false,
  }

  async yo(): Promise<MedicoActual> {
    return { ...this.medico }
  }

  async login(usuario: string, clave: string): Promise<Sesion> {
    if (usuario === 'medico' && clave === 'medico123') {
      return { usuarioId: 1, rol: 'MEDICO', personaId: this.medico.medicoId, nombre: this.medico.nombre }
    }
    throw new ApiError('CREDENCIALES_INVALIDAS', 'Usuario o contraseña incorrectos', 401)
  }
}
