import { servicios } from '~/services/proveedor'
import { useAgendaStore } from '~/stores/agenda'
import { useAtencionStore } from '~/stores/atencion'
import { useHistorialStore } from '~/stores/historial'
import { useSesionStore } from '~/stores/sesion'

/** Conecta cada store con su service (REST o Fake) y recupera la sesion guardada. */
export default defineNuxtPlugin(() => {
  const s = servicios()
  const sesion = useSesionStore()
  sesion.usar(s.medico)
  useAgendaStore().usar(s.agenda)
  useHistorialStore().usar(s.historial)
  useAtencionStore().usar(s.atencion)
  sesion.restaurar()
})
