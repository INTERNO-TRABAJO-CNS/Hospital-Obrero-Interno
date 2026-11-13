import { describe, expect, it } from 'vitest'
import type { Cirugia } from '~/models/cirugia'
import type { CitaOrigen, SolicitudFicha } from '~/models/fichaInterna'
import type { MedicoActual } from '~/models/medico'
import { FakeCirugiasService } from '~/services/fake/fakeCirugiasService'
import { FakeFichasInternasService } from '~/services/fake/fakeFichasInternasService'

// Lunes 16 de noviembre de 2026, 09:00 (hora local).
const AHORA = () => new Date(2026, 10, 16, 9, 0)
const HOY = '2026-11-16'

const general: MedicoActual = { medicoId: 7, nombre: 'Carlos Ramos', especialidad: 'CONSULTORIO_FAMILIAR',
  puedeDerivar: true, operaEnQuirofano: false }
const pediatra: MedicoActual = { ...general, nombre: 'Lucía Quispe', especialidad: 'PEDIATRIA' }
const odontologo: MedicoActual = { ...general, especialidad: 'ODONTOLOGIA', puedeDerivar: false }
const vargas: MedicoActual = { medicoId: 21, nombre: 'Raúl Vargas', especialidad: 'GASTROENTEROLOGIA',
  puedeDerivar: false, operaEnQuirofano: true }

const cita = (c: Partial<CitaOrigen> = {}): CitaOrigen =>
  ({ citaId: 503, numeroFicha: 3, fecha: HOY, hora: '09:30:00', estado: 'ASISTENCIA_CONFIRMADA', ...c })
const solicitud = (s: Partial<SolicitudFicha> = {}): SolicitudFicha => ({
  citaOrigenId: 503, pacienteId: 102, nombrePaciente: 'Elena Copa', numeroAfiliado: '4587-12', edad: 34,
  especialidad: 'GASTROENTEROLOGIA', urgencia: false, gravedad: 'MEDIA', motivo: 'Dolor abdominal persistente', ...s,
})

describe('fichas internas (reglas del 10/11/2026)', () => {
  it('desde una cita confirmada de hoy: ASIGNADA desde mañana con un gastroenterólogo', async () => {
    const s = new FakeFichasInternasService(AHORA)

    const r = await s.generar(solicitud(), general, cita())

    expect(r.resultado).toBe('ASIGNADA')
    if (r.resultado === 'RECHAZADA') return
    expect(r.ficha.ticket).toBe(1033)
    expect(r.ficha.fecha! > HOY).toBe(true)
    expect(['Raúl Vargas', 'Patricia Arce']).toContain(r.ficha.medico)
    expect(r.ficha.tabla).toBe('GENERAL')
  })

  it('una sola derivación por cita', async () => {
    const s = new FakeFichasInternasService(AHORA)
    await s.generar(solicitud(), general, cita())

    const r = await s.generar(solicitud({ pacienteId: 999 }), general, cita())

    expect(r).toMatchObject({ resultado: 'RECHAZADA', codigo: 'CITA_YA_DERIVADA' })
  })

  it('cada rechazo con su código', async () => {
    const s = new FakeFichasInternasService(AHORA)
    const codigo = async (p: Promise<{ resultado: string; codigo?: string }>) => (await p).codigo

    expect(await codigo(s.generar(solicitud(), odontologo, cita()))).toBe('MEDICO_NO_DERIVA')
    expect(await codigo(s.generar(solicitud({ especialidad: 'TERAPIA_INTENSIVA' }), general, cita())))
      .toBe('TERAPIA_SOLO_URGENCIA')
    expect(await codigo(s.generar(solicitud({ motivo: '  ' }), general, cita()))).toBe('MOTIVO_INVALIDO')
    expect(await codigo(s.generar(solicitud(), general, cita({ fecha: '2026-11-13' })))).toBe('CITA_NO_VALIDA')
    expect(await codigo(s.generar(solicitud(), general, cita({ estado: 'PROGRAMADA' })))).toBe('CITA_NO_VALIDA')
    expect(await codigo(s.generar(solicitud({ edad: 12 }), pediatra, cita()))).toBe('EDAD_NO_PEDIATRICA')
    expect(await codigo(s.generar(solicitud({ pacienteId: 113 }), general, cita()))).toBe('PACIENTE_CON_FICHA_ACTIVA')
  })

  it('sin especialista disponible queda EN_ESPERA', async () => {
    const s = new FakeFichasInternasService(AHORA)

    const r = await s.generar(solicitud({ especialidad: 'ENDOCRINOLOGIA' }), general, cita())

    expect(r.resultado).toBe('EN_ESPERA')
  })

  it('urgencia sin cita: se asigna hoy', async () => {
    const s = new FakeFichasInternasService(AHORA)

    const r = await s.generar(solicitud({ citaOrigenId: null, pacienteId: 103, urgencia: true,
      especialidad: 'TERAPIA_INTENSIVA' }), general, null)

    expect(r.resultado).toBe('ASIGNADA')
    if (r.resultado !== 'RECHAZADA') expect(r.ficha.fecha).toBe(HOY)
  })

  it('cola: urgencia > gravedad > 60 o más > llegada', async () => {
    const s = new FakeFichasInternasService(AHORA)

    const cola = await s.cola('GASTROENTEROLOGIA', 'GENERAL')

    expect(cola.map(f => f.nombrePaciente)).toEqual(['Rosa Choque', 'Juan Mamani', 'Luis Paz', 'Ana Rojas',
      'Teresa Ticona', 'Marco Ríos', 'Jorge Limachi', 'Mario Cruz'])
  })
})

describe('cirugías (cero choques)', () => {
  it('propone el primer hueco desde mañana y confirma', async () => {
    const fichas = new FakeFichasInternasService(AHORA)
    const s = new FakeCirugiasService(fichas, AHORA)

    const p = await s.proponer({ fichaTicket: 1019, duracionMin: 90, gravedad: 'ALTA', urgente: false }, vargas)

    expect(p).toMatchObject({ quirofano: 'G-1', fecha: '2026-11-17', inicio: '07:00', fin: '08:30',
      limpiezaHasta: '09:10', desplaza: null })
    const c = await s.confirmar({ fichaTicket: 1019, duracionMin: 90, gravedad: 'ALTA', urgente: false }, p, vargas)
    expect(c.numero).toBe(58)
  })

  it('una propuesta vieja que ya se ocupó lanza CHOQUE_DE_HORARIO', async () => {
    const fichas = new FakeFichasInternasService(AHORA)
    const s = new FakeCirugiasService(fichas, AHORA)
    const datos = { fichaTicket: 1019, duracionMin: 90, gravedad: 'ALTA' as const, urgente: false }
    const p = await s.proponer(datos, vargas)
    await s.confirmar(datos, p, vargas)

    const error = await s.confirmar(datos, p, vargas).catch(e => e)

    expect(error.codigo).toBe('CHOQUE_DE_HORARIO')
  })

  it('rechaza duración fuera de rango y fichas que no atendió', async () => {
    const fichas = new FakeFichasInternasService(AHORA)
    const s = new FakeCirugiasService(fichas, AHORA)

    expect((await s.proponer({ fichaTicket: 1019, duracionMin: 200, gravedad: 'ALTA', urgente: false }, vargas)
      .catch(e => e)).codigo).toBe('DURACION_FUERA_DE_RANGO')
    expect((await s.proponer({ fichaTicket: 1024, duracionMin: 60, gravedad: 'ALTA', urgente: false }, vargas)
      .catch(e => e)).codigo).toBe('FICHA_NO_ATENDIDA')
  })

  it('una urgencia sin hueco hoy desplaza a la primera cirugía no urgente que no empezó', async () => {
    const fichas = new FakeFichasInternasService(AHORA)
    const s = new FakeCirugiasService(fichas, AHORA)
    const base = { fichaTicket: 0, pacienteId: 900, numeroAfiliado: '', edad: 50, especialidad: 'GASTROENTEROLOGIA',
      cirujano: 'Patricia Arce', fecha: HOY, duracionMin: 60, gravedad: 'MEDIA', estado: 'PROGRAMADA' } as const
    s.cirugias = [
      { ...base, numero: 1, nombrePaciente: 'A', quirofano: 'G-1', inicio: '09:00', fin: '23:00', limpiezaHasta: '23:40', urgente: true },
      { ...base, numero: 2, nombrePaciente: 'B', quirofano: 'G-2', inicio: '09:00', fin: '12:00', limpiezaHasta: '12:30', urgente: false },
      { ...base, numero: 57, nombrePaciente: 'Luis Quispe', quirofano: 'G-2', inicio: '12:30', fin: '23:00', limpiezaHasta: '23:30', urgente: false },
    ] as Cirugia[]

    const p = await s.proponer({ fichaTicket: 1019, duracionMin: 60, gravedad: 'ALTA', urgente: true }, vargas)

    expect(p.desplaza?.numero).toBe(57)
    expect(p).toMatchObject({ quirofano: 'G-2', fecha: HOY, inicio: '12:30', fin: '13:30' })
  })
})
