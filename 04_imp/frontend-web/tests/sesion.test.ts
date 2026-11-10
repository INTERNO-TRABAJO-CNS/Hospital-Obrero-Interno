import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import type { MedicoActual } from '~/models/medico'
import { FakeMedicoService } from '~/services/fake/fakeMedicoService'
import { menuPara, useSesionStore } from '~/stores/sesion'

const medico = (m: Partial<MedicoActual>): MedicoActual => ({
  medicoId: 1, nombre: 'X', especialidad: 'CONSULTORIO_FAMILIAR', puedeDerivar: true, operaEnQuirofano: false, ...m,
})

const textos = (m: MedicoActual) => menuPara(m).map(o => o.texto)

describe('menu segun el tipo de medico', () => {
  it('Medico General incluye «Ticket interno»', () => {
    expect(textos(medico({}))).toContain('Ticket interno')
  })

  it('Pediatria incluye «Ticket interno»', () => {
    expect(textos(medico({ especialidad: 'PEDIATRIA' }))).toContain('Ticket interno')
  })

  it('Odontologia no incluye «Ticket interno»', () => {
    expect(textos(medico({ especialidad: 'ODONTOLOGIA', puedeDerivar: false }))).toEqual(['Agenda'])
  })

  it('Especialista ve «Cirugías» solo si opera', () => {
    expect(textos(medico({ especialidad: 'DERMATOLOGIA', puedeDerivar: false, operaEnQuirofano: true })))
      .toContain('Cirugías')
    expect(textos(medico({ especialidad: 'INFECTOLOGIA', puedeDerivar: false }))).not.toContain('Cirugías')
  })
})

describe('store de sesion', () => {
  let fake: FakeMedicoService

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    fake = new FakeMedicoService()
  })

  it('entrar con credenciales carga el medico', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)

    expect(await sesion.entrar('medico', 'medico123')).toBe(true)
    expect(sesion.esMedico).toBe(true)
    expect(sesion.medico?.nombre).toBe('Rosa Mamani')
  })

  it('credenciales invalidas muestran el mensaje del backend', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)

    expect(await sesion.entrar('medico', 'mala')).toBe(false)
    expect(sesion.usuario).toBeNull()
    expect(sesion.error).toBe('Usuario o contraseña incorrectos')
  })

  it('la sesion se guarda y se restaura; salir la borra', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)
    await sesion.entrarTemporal(7)

    setActivePinia(createPinia())
    const otra = useSesionStore()
    otra.restaurar()
    expect(otra.usuario?.personaId).toBe(7)

    otra.salir()
    otra.restaurar()
    expect(otra.usuario).toBeNull()
  })
})
