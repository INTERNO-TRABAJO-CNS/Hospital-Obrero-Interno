import type { Servicios } from '~/services/proveedor'

/** Servicios (REST o Fake) que dejo el plugin servicios.client.ts. */
export function useServicios(): Servicios {
  return useNuxtApp().$servicios as Servicios
}
