import { useSesionStore } from '~/stores/sesion'

const PUBLICAS = ['/login', '/sin-acceso']

/** Sin sesion -> /login. Rol distinto de MEDICO -> /sin-acceso. Carga /medico/yo una vez. */
export default defineNuxtRouteMiddleware(async (to) => {
  if (PUBLICAS.includes(to.path)) return
  const sesion = useSesionStore()
  if (!sesion.autenticado) return navigateTo('/login')
  if (!sesion.esMedico) return navigateTo('/sin-acceso')
  if (!sesion.medico && !(await sesion.cargarMedico())) {
    sesion.salir()
    return navigateTo('/login')
  }
})
