import { useSesionStore } from '~/stores/sesion'

const PUBLICAS = ['/login', '/sin-acceso']

/**
 * Sin sesion -> /login. Rol distinto de MEDICO -> /sin-acceso. Carga /medico/yo una vez.
 * Si el backend no responde, la sesion NO se borra: se muestra la pagina y se reintenta en la siguiente
 * navegacion. Solo un 401/403 la cierra.
 */
export default defineNuxtRouteMiddleware(async (to) => {
  if (PUBLICAS.includes(to.path)) return
  const sesion = useSesionStore()
  if (!sesion.autenticado) return navigateTo('/login')
  if (!sesion.esMedico) return navigateTo('/sin-acceso')
  if (!sesion.medico) {
    const resultado = await sesion.cargarMedico()
    if (resultado === 'rechazada') {
      await sesion.salir()
      return navigateTo('/login')
    }
  }
})
