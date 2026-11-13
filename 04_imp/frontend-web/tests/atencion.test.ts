import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { FakeAtencionService } from '~/services/fake/fakeAtencionService'
import { useAtencionStore } from '~/stores/atencion'

describe('atencion del medico', () => {
  let fake: FakeAtencionService

  beforeEach(() => {
    setActivePinia(createPinia())
    fake = new FakeAtencionService()
  })

  it('recetaVaciaNoEnvia', async () => {
    const atencion = useAtencionStore()
    atencion.usar(fake)
    await atencion.cargar(503)
    atencion.medicacion = '   '

    expect(atencion.puedeFinalizar).toBe(false)
    expect(await atencion.finalizar()).toBe(false)
    expect(fake.registradas).toEqual([])
  })

  it('receta de mas de 2000 caracteres no se envia', async () => {
    const atencion = useAtencionStore()
    atencion.usar(fake)
    await atencion.cargar(503)
    atencion.medicacion = 'x'.repeat(2001)

    expect(atencion.puedeFinalizar).toBe(false)
  })

  it('finalizar envia la receta sin espacios y marca finalizada', async () => {
    const atencion = useAtencionStore()
    atencion.usar(fake)
    await atencion.cargar(503)
    atencion.medicacion = '  Amoxicilina 500 mg c/8 h por 7 días  '

    expect(await atencion.finalizar()).toBe(true)
    expect(fake.registradas).toEqual([{ citaId: 503, medicacion: 'Amoxicilina 500 mg c/8 h por 7 días' }])
    expect(atencion.finalizada).toBe(true)
    expect(atencion.puedeFinalizar).toBe(false)
  })

  it('botonTicketSoloSiPuedeDerivar', async () => {
    fake.datosPorCita[5] = { ...fake.datosPorCita[503], citaId: 5, consulta: 'ODONTOLOGIA', puedeDerivar: false }
    const atencion = useAtencionStore()
    atencion.usar(fake)

    await atencion.cargar(503)
    expect(atencion.datos?.puedeDerivar).toBe(true)

    await atencion.cargar(5)
    expect(atencion.datos?.puedeDerivar).toBe(false)
  })

  it('cita inexistente muestra el error', async () => {
    const atencion = useAtencionStore()
    atencion.usar(fake)

    await atencion.cargar(404)

    expect(atencion.datos).toBeNull()
    expect(atencion.error).toBe('La cita no existe')
  })
})
