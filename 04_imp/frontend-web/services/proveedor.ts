import { useApi } from './apiClient'
import { AgendaServiceApi, type AgendaService } from './agendaService'
import { AtencionServiceApi, type AtencionService } from './atencionService'
import { HistorialServiceApi, type HistorialService } from './historialService'
import { MedicoServiceApi, type MedicoService } from './medicoService'
import { FakeAgendaService } from './fake/fakeAgendaService'
import { FakeAtencionService } from './fake/fakeAtencionService'
import { FakeHistorialService } from './fake/fakeHistorialService'
import { FakeMedicoService } from './fake/fakeMedicoService'

export interface Servicios {
  medico: MedicoService
  agenda: AgendaService
  historial: HistorialService
  atencion: AtencionService
}

let fakes: Servicios | null = null

/** Servicios reales (REST) o Fake* si NUXT_PUBLIC_USAR_FAKES=true. Solo lo usan los stores. */
export function servicios(): Servicios {
  const config = useRuntimeConfig()
  if (String(config.public.usarFakes) === 'true') {
    fakes ??= {
      medico: new FakeMedicoService(),
      agenda: new FakeAgendaService(),
      historial: new FakeHistorialService(),
      atencion: new FakeAtencionService(),
    }
    return fakes
  }
  const api = useApi()
  return {
    medico: new MedicoServiceApi(api),
    agenda: new AgendaServiceApi(api),
    historial: new HistorialServiceApi(api),
    atencion: new AtencionServiceApi(api),
  }
}
