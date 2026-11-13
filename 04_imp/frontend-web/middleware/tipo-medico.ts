import { useSesionStore } from '~/stores/sesion'

/**
 * Cada pagina solo para el tipo de medico que la tiene en su menu (definePageMeta({ middleware: 'tipo-medico' })).
 * Si entra otro (por ejemplo un medico general escribiendo /medico/cirugias), vuelve a su inicio.
 */
export default defineNuxtRouteMiddleware((to) => {
  const sesion = useSesionStore()
  if (!sesion.medico) return
  const permitida = sesion.menu.some(o => to.path === o.ruta || to.path.startsWith(`${o.ruta}/`))
  if (!permitida) return navigateTo(sesion.inicio)
})
