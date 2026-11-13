import { servicios } from '~/services/proveedor'
import { useAgendaStore } from '~/stores/agenda'
import { useAtencionStore } from '~/stores/atencion'
import { useHistorialStore } from '~/stores/historial'
import { useSesionStore } from '~/stores/sesion'

/**
 * Conecta cada store con su service (REST o Fake), recupera la sesion guardada y deja los servicios disponibles
 * para las paginas con {@code useServicios()}.
 */
export default defineNuxtPlugin(() => {
  const s = servicios()
  const sesion = useSesionStore()
  sesion.usar(s.medico)
  useAgendaStore().usar(s.agenda, undefined, s.fichas)
  useHistorialStore().usar(s.historial, s.fichas, s.cirugias)
  useAtencionStore().usar(s.atencion)
  sesion.restaurar()
  return { provide: { servicios: s } }
})
