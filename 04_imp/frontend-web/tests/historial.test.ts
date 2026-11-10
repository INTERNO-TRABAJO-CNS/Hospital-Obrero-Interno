import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { FakeHistorialService } from '~/services/fake/fakeHistorialService'
import { useHistorialStore } from '~/stores/historial'

describe('historial del paciente', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('une citas pasadas y recetas en una linea de tiempo, de la mas reciente a la mas antigua', async () => {
    const historial = useHistorialStore()
    historial.usar(new FakeHistorialService())

    await historial.cargar(100)

    const linea = historial.lineaDeTiempo
    expect(linea.map(e => e.fecha)).toEqual(['2026-10-28', '2026-09-15'])
    expect(linea[0].recetas).toEqual(['Paracetamol 500 mg cada 8 h por 3 días'])
    expect(linea[1].estado).toBe('CANCELADA')
    expect(historial.totalRecetas).toBe(1)
  })

  it('paciente fuera de su agenda: sin permiso y con el mensaje del backend', async () => {
    const historial = useHistorialStore()
    historial.usar(new FakeHistorialService())

    await historial.cargar(999)

    expect(historial.sinPermiso).toBe(true)
    expect(historial.historial).toBeNull()
    expect(historial.error).toContain('Solo puede ver el historial')
  })
})
