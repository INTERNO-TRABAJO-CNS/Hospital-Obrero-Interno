import { useApi } from './apiClient'
import { AgendaServiceApi, type AgendaService } from './agendaService'
import { AtencionServiceApi, type AtencionService } from './atencionService'
import type { CirugiasService } from './cirugiasService'
import type { FichasInternasService } from './fichasInternasService'
import { HistorialServiceApi, type HistorialService } from './historialService'
import { MedicoServiceApi, type MedicoService } from './medicoService'
import { FakeAgendaService } from './fake/fakeAgendaService'
import { FakeAtencionService } from './fake/fakeAtencionService'
import { FakeCirugiasService } from './fake/fakeCirugiasService'
import { FakeFichasInternasService } from './fake/fakeFichasInternasService'
import { FakeHistorialService } from './fake/fakeHistorialService'
import { FakeMedicoService } from './fake/fakeMedicoService'

export interface Servicios {
  medico: MedicoService
  agenda: AgendaService
  historial: HistorialService
  atencion: AtencionService
  fichas: FichasInternasService
  cirugias: CirugiasService
}

let fakes: Servicios | null = null
let fichasYCirugias: Pick<Servicios, 'fichas' | 'cirugias'> | null = null

/**
 * Servicios reales (REST) o Fake* si NUXT_PUBLIC_USAR_FAKES=true. Solo los usan los stores y las paginas.
 * Fichas internas y cirugias son Fake siempre: su backend llega con T100-20..23.
 */
export function servicios(): Servicios {
  const config = useRuntimeConfig()
  fichasYCirugias ??= crearFichasYCirugias()
  if (String(config.public.usarFakes) === 'true') {
    fakes ??= {
      medico: new FakeMedicoService(),
      agenda: new FakeAgendaService(),
      historial: new FakeHistorialService(),
      atencion: new FakeAtencionService(),
      ...fichasYCirugias,
    }
    return fakes
  }
  const api = useApi()
  return {
    medico: new MedicoServiceApi(api),
    agenda: new AgendaServiceApi(api),
    historial: new HistorialServiceApi(api),
    atencion: new AtencionServiceApi(api),
    ...fichasYCirugias,
  }
}

function crearFichasYCirugias(): Pick<Servicios, 'fichas' | 'cirugias'> {
  const fichas = new FakeFichasInternasService(undefined, 'cns.prueba.fichas')
  return { fichas, cirugias: new FakeCirugiasService(fichas, undefined, 'cns.prueba.cirugias') }
}
