import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { esBasica } from '~/models/especialidad'
import type { MedicoActual, OpcionMenu, Sesion } from '~/models/medico'
import type { MedicoService } from '~/services/medicoService'
import { mensajeDeError } from '~/services/apiClient'

const CLAVE_GUARDADA = 'cns.web.sesion'

/**
 * Menu segun el tipo de medico (T100-18). Agenda, atencion e historial se abren desde la agenda.
 * Las opciones de otras tarjetas aparecen deshabilitadas hasta que existan sus pantallas.
 */
export function menuPara(medico: MedicoActual | null): OpcionMenu[] {
  if (!medico) return []
  if (!esBasica(medico.especialidad)) {
    const especialista: OpcionMenu[] = [
      { texto: 'Agenda de fichas internas', ruta: '/medico/fichas-internas', pendiente: true }, // T100-22
    ]
    if (medico.operaEnQuirofano) {
      especialista.push({ texto: 'Cirugías', ruta: '/medico/cirugias', pendiente: true }) // T100-23
    }
    return especialista
  }
  const menu: OpcionMenu[] = [{ texto: 'Agenda', ruta: '/medico/agenda' }]
  if (medico.puedeDerivar) {
    menu.push({ texto: 'Ticket interno', ruta: '/medico/ticket-interno', pendiente: true }) // T100-21
    menu.push({ texto: 'Cola de prioridad', ruta: '/medico/cola', pendiente: true }) // T100-21
  }
  return menu
}

/** Sesion del medico: quien es (headers temporales) y sus datos de /medico/yo. */
export const useSesionStore = defineStore('sesion', () => {
  let servicio: MedicoService | null = null

  const usuario = ref<Sesion | null>(null)
  const medico = ref<MedicoActual | null>(null)
  const cargando = ref(false)
  const error = ref<string | null>(null)

  const autenticado = computed(() => usuario.value !== null)
  const esMedico = computed(() => usuario.value?.rol === 'MEDICO')
  const menu = computed(() => menuPara(medico.value))

  /** Las pruebas inyectan un Fake; en la app lo pone el plugin de servicios. */
  function usar(s: MedicoService) {
    servicio = s
  }

  function srv(): MedicoService {
    if (!servicio) throw new Error('El store de sesion no tiene servicio')
    return servicio
  }

  function restaurar() {
    try {
      const texto = localStorage.getItem(CLAVE_GUARDADA)
      usuario.value = texto ? (JSON.parse(texto) as Sesion) : null
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

  async function cargarMedico(): Promise<boolean> {
    if (!esMedico.value) return false
    try {
      medico.value = await srv().yo()
      return true
    } catch (e) {
      error.value = mensajeDeError(e)
      return false
    }
  }

  /** Login real (POST /api/v1/auth/login de T100-05). */
  async function entrar(nombreUsuario: string, clave: string): Promise<boolean> {
    cargando.value = true
    error.value = null
    try {
      usuario.value = await srv().login(nombreUsuario.trim(), clave)
      guardar()
      return esMedico.value ? await cargarMedico() : true
    } catch (e) {
      error.value = mensajeDeError(e)
      usuario.value = null
      return false
    } finally {
      cargando.value = false
    }
  }

  /**
   * TEMPORAL (hasta S3-08): entrar indicando el id del medico, igual que los headers de Postman.
   * El backend valida que el medico exista con GET /medico/yo.
   */
  async function entrarTemporal(medicoId: number): Promise<boolean> {
    cargando.value = true
    error.value = null
    try {
      usuario.value = { usuarioId: medicoId, rol: 'MEDICO', personaId: medicoId }
      const ok = await cargarMedico()
      if (!ok) usuario.value = null
      guardar()
      return ok
    } finally {
      cargando.value = false
    }
  }

  function salir() {
    usuario.value = null
    medico.value = null
    error.value = null
    guardar()
  }

  return { usuario, medico, cargando, error, autenticado, esMedico, menu, usar, restaurar, cargarMedico, entrar,
    entrarTemporal, salir }
})
