import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import type { MedicoActual } from '~/models/medico'
import { ApiError } from '~/services/apiClient'
import { FakeMedicoService } from '~/services/fake/fakeMedicoService'
import { etiquetaDe, menuPara, useSesionStore } from '~/stores/sesion'

const medico = (m: Partial<MedicoActual>): MedicoActual => ({
  medicoId: 1, nombre: 'Carlos Ramos', especialidad: 'CONSULTORIO_FAMILIAR', puedeDerivar: true,
  operaEnQuirofano: false, ...m,
})

const textos = (m: MedicoActual) => menuPara(m).map(o => o.texto)

describe('menu segun el tipo de medico (Figma)', () => {
  it('Medico General: Agenda, Atención, Historial, Ticket interno y Cola de prioridad', () => {
    expect(textos(medico({}))).toEqual(['Agenda', 'Atención', 'Historial', 'Ticket interno', 'Cola de prioridad'])
  })

  it('Pediatria tambien deriva', () => {
    expect(textos(medico({ especialidad: 'PEDIATRIA' }))).toContain('Ticket interno')
  })

  it('Odontologia no deriva', () => {
    expect(textos(medico({ especialidad: 'ODONTOLOGIA', puedeDerivar: false }))).toEqual(['Agenda', 'Atención', 'Historial'])
  })

  it('Especialista: agenda de fichas internas y «Cirugías» solo si opera', () => {
    expect(textos(medico({ especialidad: 'DERMATOLOGIA', puedeDerivar: false, operaEnQuirofano: true })))
      .toEqual(['Agenda de fichas internas', 'Cirugías'])
    expect(textos(medico({ especialidad: 'INFECTOLOGIA', puedeDerivar: false }))).toEqual(['Agenda de fichas internas'])
  })

  it('pildora de la barra superior', () => {
    expect(etiquetaDe(medico({}))).toBe('Médico General · Carlos Ramos')
    expect(etiquetaDe(medico({ nombre: 'Raúl Vargas', especialidad: 'GASTROENTEROLOGIA' })))
      .toBe('Especialista · Raúl Vargas · Gastroenterología')
  })
})

describe('store de sesion (login con token)', () => {
  let fake: FakeMedicoService

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    fake = new FakeMedicoService()
  })

  it('entrar con credenciales guarda el token y carga el medico', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)

    expect(await sesion.entrar('medico', 'medico123')).toBe(true)
    expect(sesion.usuario?.token).toBe('token-falso-medico')
    expect(sesion.medico?.nombre).toBe('Carlos Ramos')
    expect(sesion.inicio).toBe('/medico/agenda')
  })

  it('el especialista empieza en su agenda de fichas internas', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)

    await sesion.entrar('especialista', 'medico123')

    expect(sesion.inicio).toBe('/medico/fichas-internas')
  })

  it('credenciales invalidas muestran el mensaje del backend', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)

    expect(await sesion.entrar('medico', 'mala')).toBe(false)
    expect(sesion.usuario).toBeNull()
    expect(sesion.error).toBe('Usuario o contraseña incorrectos')
  })

  it('la sesion se guarda y se restaura; salir la cierra tambien en el backend', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)
    await sesion.entrar('medico', 'medico123')

    setActivePinia(createPinia())
    const otra = useSesionStore()
    otra.usar(fake)
    otra.restaurar()
    expect(otra.usuario?.personaId).toBe(7)

    await otra.salir()
    expect(fake.cerradas).toBe(1)
    otra.restaurar()
    expect(otra.usuario).toBeNull()
  })

  it('una sesion guardada ya vencida no se usa', () => {
    localStorage.setItem('cns.web.sesion', JSON.stringify({ usuarioId: 1, rol: 'MEDICO', personaId: 7, token: 't',
      venceEn: '2020-01-01T00:00:00Z' }))
    const sesion = useSesionStore()
    sesion.restaurar()

    expect(sesion.autenticado).toBe(false)
  })

  it('si el backend no responde NO se borra la sesion; un 401 si', async () => {
    const sesion = useSesionStore()
    sesion.usar(fake)
    await sesion.entrar('medico', 'medico123')
    fake.yo = async () => { throw new ApiError('SIN_CONEXION', 'No se pudo conectar con el servidor', 0) }

    expect(await sesion.cargarMedico()).toBe('sin-conexion')
    expect(sesion.autenticado).toBe(true)

    fake.yo = async () => { throw new ApiError('NO_AUTENTICADO', 'Inicie sesion para continuar', 401) }
    expect(await sesion.cargarMedico()).toBe('rechazada')
  })
})
