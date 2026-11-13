import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { FakeCirugiasService } from '~/services/fake/fakeCirugiasService'
import { FakeFichasInternasService } from '~/services/fake/fakeFichasInternasService'
import { FakeHistorialService } from '~/services/fake/fakeHistorialService'
import { useHistorialStore } from '~/stores/historial'

describe('historial del paciente', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('une citas pasadas y recetas en una linea de tiempo, de la mas reciente a la mas antigua', async () => {
    const historial = useHistorialStore()
    historial.usar(new FakeHistorialService())

    await historial.cargar(100)

    const linea = historial.lineaDeTiempo
    expect(linea.map(e => e.fecha)).toEqual(['2026-10-14', '2026-08-02'])
    expect(linea[0].tipo).toBe('ATENCION')
    expect(linea[0].recetas).toEqual(['Losartán 50 mg cada 24 h por 30 días.'])
    expect(linea[1].estado).toBe('CANCELADA')
    expect(historial.totalRecetas).toBe(1)
  })

  it('suma fichas internas y cirugias y se puede filtrar', async () => {
    const fichas = new FakeFichasInternasService()
    const historial = useHistorialStore()
    const fake = new FakeHistorialService()
    fake.porPaciente[113] = { pacienteId: 113, nombrePaciente: 'Marco Ríos', citasPasadas: [], atenciones: [] }
    historial.usar(fake, fichas, new FakeCirugiasService(fichas))

    await historial.cargar(113)

    expect(historial.lineaDeTiempo.map(e => e.tipo).sort()).toEqual(['CIRUGIA', 'FICHA_INTERNA'])
    historial.filtro = 'CIRUGIAS'
    expect(historial.eventosFiltrados.map(e => e.tipo)).toEqual(['CIRUGIA'])
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
