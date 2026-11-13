import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { esBasica, nombreEspecialidad, soloUrgencia, tipoDeMedico } from '~/models/especialidad'
import type { MedicoActual, OpcionMenu, Sesion } from '~/models/medico'
import type { MedicoService } from '~/services/medicoService'
import { ApiError, mensajeDeError } from '~/services/apiClient'

const CLAVE_GUARDADA = 'cns.web.sesion'

/**
 * Menu lateral segun el tipo de medico, igual que el Figma:
 * Medico General y Pediatria: Agenda, Atencion, Historial, Ticket interno y Cola de prioridad.
 * Odontologia: sin ticket interno ni cola (no deriva). Especialistas: agenda de fichas internas y, si su
 * especialidad opera, Cirugias.
 */
export function menuPara(medico: MedicoActual | null): OpcionMenu[] {
  if (!medico) return []
  if (!esBasica(medico.especialidad)) {
    const especialista: OpcionMenu[] = [{ texto: 'Agenda de fichas internas', ruta: '/medico/fichas-internas' }]
    if (medico.operaEnQuirofano && !soloUrgencia(medico.especialidad)) {
      especialista.push({ texto: 'Cirugías', ruta: '/medico/cirugias' })
    }
    return especialista
  }
  const menu: OpcionMenu[] = [
    { texto: 'Agenda', ruta: '/medico/agenda' },
    { texto: 'Atención', ruta: '/medico/atencion' },
    { texto: 'Historial', ruta: '/medico/historial' },
  ]
  if (medico.puedeDerivar) {
    menu.push({ texto: 'Ticket interno', ruta: '/medico/ticket-interno' })
    menu.push({ texto: 'Cola de prioridad', ruta: '/medico/cola' })
  }
  return menu
}

/** Texto de la pildora de la barra superior: «Médico General · Carlos Ramos». */
export function etiquetaDe(medico: MedicoActual | null): string {
  if (!medico) return ''
  const tipo = tipoDeMedico(medico.especialidad)
  return esBasica(medico.especialidad)
    ? `${tipo} · ${medico.nombre}`
    : `${tipo} · ${medico.nombre} · ${nombreEspecialidad(medico.especialidad)}`
}

/** Sesion del medico: su token (Authorization: Bearer) y sus datos de /medico/yo. */
export const useSesionStore = defineStore('sesion', () => {
  let servicio: MedicoService | null = null

  const usuario = ref<Sesion | null>(null)
  const medico = ref<MedicoActual | null>(null)
  const cargando = ref(false)
  const error = ref<string | null>(null)

  const autenticado = computed(() => !!usuario.value?.token)
  const esMedico = computed(() => usuario.value?.rol === 'MEDICO')
  const menu = computed(() => menuPara(medico.value))
  const etiqueta = computed(() => etiquetaDe(medico.value))
  const inicio = computed(() => menu.value[0]?.ruta ?? '/medico/agenda')

  /** Las pruebas inyectan un Fake; en la app lo pone el plugin de servicios. */
  function usar(s: MedicoService) {
    servicio = s
  }

  function srv(): MedicoService {
    if (!servicio) throw new Error('El store de sesion no tiene servicio')
    return servicio
  }

  /** Recupera la sesion guardada; si ya vencio, no la usa. */
  function restaurar() {
    try {
      const texto = localStorage.getItem(CLAVE_GUARDADA)
      const guardada = texto ? (JSON.parse(texto) as Sesion) : null
      const vencida = guardada?.venceEn ? new Date(guardada.venceEn).getTime() <= Date.now() : false
      usuario.value = guardada?.token && !vencida ? guardada : null
    } catch {
      usuario.value = null
    }
  }

  function guardar() {
    try {
      if (usuario.value) localStorage.setItem(CLAVE_GUARDADA, JSON.stringify(usuario.value))
      else localStorage.removeItem(CLAVE_GUARDADA)
    } catch {
      // sin almacenamiento local: la sesion dura hasta recargar
    }
  }

  /**
   * Carga los datos del medico. Si el backend no responde, NO se borra la sesion (se puede reintentar);
   * solo un 401/403 la invalida.
   */
  async function cargarMedico(): Promise<'ok' | 'sin-conexion' | 'rechazada'> {
    if (!esMedico.value) return 'rechazada'
    try {
      medico.value = await srv().yo()
      return 'ok'
    } catch (e) {
      error.value = mensajeDeError(e)
      return e instanceof ApiError && (e.status === 401 || e.status === 403) ? 'rechazada' : 'sin-conexion'
    }
  }

  /** Login real: POST /api/v1/auth/login devuelve el token. */
  async function entrar(nombreUsuario: string, clave: string): Promise<boolean> {
    cargando.value = true
    error.value = null
    try {
      usuario.value = await srv().login(nombreUsuario.trim(), clave)
      guardar()
      if (!esMedico.value) return true
      const resultado = await cargarMedico()
      if (resultado === 'rechazada') limpiar()
      return resultado === 'ok'
    } catch (e) {
      error.value = mensajeDeError(e)
      limpiar()
      return false
    } finally {
      cargando.value = false
    }
  }

  /** Cierra la sesion en el backend (el token deja de servir) y en el navegador. */
  async function salir() {
    if (usuario.value?.token) {
      try {
        await srv().logout()
      } catch {
        // si el backend no responde, igual se cierra aqui; el token vence solo
      }
    }
    limpiar()
    error.value = null
  }

  /** El backend respondio 401: la sesion vencio o se cerro en otro lado. */
  function sesionPerdida(mensaje: string) {
    limpiar()
    error.value = mensaje
  }

  function limpiar() {
    usuario.value = null
    medico.value = null
    guardar()
  }

  return { usuario, medico, cargando, error, autenticado, esMedico, menu, etiqueta, inicio, usar, restaurar,
    cargarMedico, entrar, salir, sesionPerdida }
})
