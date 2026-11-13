import { ApiError } from '../apiClient'
import type { MedicoService } from '../medicoService'
import type { MedicoActual, Sesion } from '~/models/medico'
import type { Especialidad } from '~/models/especialidad'

/** Cuentas sin backend (NUXT_PUBLIC_USAR_FAKES=true): una por cada tipo de pantalla del Figma. */
const CUENTAS: Record<string, MedicoActual> = {
  medico: { medicoId: 7, nombre: 'Carlos Ramos', especialidad: 'CONSULTORIO_FAMILIAR', puedeDerivar: true,
    operaEnQuirofano: false },
  pediatra: { medicoId: 8, nombre: 'Lucía Quispe', especialidad: 'PEDIATRIA', puedeDerivar: true,
    operaEnQuirofano: false },
  odontologo: { medicoId: 9, nombre: 'Jorge Mamani', especialidad: 'ODONTOLOGIA', puedeDerivar: false,
    operaEnQuirofano: false },
  especialista: { medicoId: 21, nombre: 'Raúl Vargas', especialidad: 'GASTROENTEROLOGIA', puedeDerivar: false,
    operaEnQuirofano: true },
}

const CLAVE = 'medico123'

/** Sin backend: login con «medico», «pediatra», «odontologo» o «especialista» y la clave medico123. */
export class FakeMedicoService implements MedicoService {
  medico: MedicoActual = { ...CUENTAS.medico }
  cerradas = 0

  async yo(): Promise<MedicoActual> {
    return { ...this.medico }
  }

  async login(usuario: string, clave: string): Promise<Sesion> {
    const cuenta = CUENTAS[usuario.toLowerCase()]
    if (!cuenta || clave !== CLAVE) {
      throw new ApiError('CREDENCIALES_INVALIDAS', 'Usuario o contraseña incorrectos', 401)
    }
    this.medico = { ...cuenta }
    return {
      usuarioId: cuenta.medicoId + 100,
      rol: 'MEDICO',
      personaId: cuenta.medicoId,
      nombre: cuenta.nombre,
      token: `token-falso-${usuario}`,
      venceEn: new Date(Date.now() + 8 * 3600_000).toISOString(),
    }
  }

  async logout(): Promise<void> {
    this.cerradas++
  }

  /** Para las pruebas: cambia el tipo de medico. */
  usarEspecialidad(especialidad: Especialidad, operaEnQuirofano = false) {
    this.medico = { ...this.medico, especialidad, operaEnQuirofano,
      puedeDerivar: especialidad === 'CONSULTORIO_FAMILIAR' || especialidad === 'PEDIATRIA' }
  }
}
