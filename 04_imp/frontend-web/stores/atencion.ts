import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { MAX_RECETA, type DatosAtencion } from '~/models/atencion'
import type { AtencionService } from '~/services/atencionService'
import { mensajeDeError } from '~/services/apiClient'

/** CU-15 en la web: datos autocompletados, receta y «Finalizar atención». */
export const useAtencionStore = defineStore('atencion', () => {
  let servicio: AtencionService | null = null

  const datos = ref<DatosAtencion | null>(null)
  const medicacion = ref('')
  const cargando = ref(false)
  const enviando = ref(false)
  const error = ref<string | null>(null)
  const finalizada = ref(false)

  const caracteres = computed(() => medicacion.value.trim().length)
  /** Solo evita enviar algo que el backend rechazaria; la regla la aplica el backend (RECETA_INVALIDA). */
  const recetaLista = computed(() => caracteres.value > 0 && caracteres.value <= MAX_RECETA)
  const puedeFinalizar = computed(() => !!datos.value && recetaLista.value && !enviando.value && !finalizada.value)

  function usar(s: AtencionService) {
    servicio = s
  }

  function srv(): AtencionService {
    if (!servicio) throw new Error('El store de atencion no tiene servicio')
    return servicio
  }

  async function cargar(citaId: number) {
    cargando.value = true
    error.value = null
    datos.value = null
    medicacion.value = ''
    finalizada.value = false
    try {
      datos.value = await srv().datos(citaId)
    } catch (e) {
      error.value = mensajeDeError(e)
    } finally {
      cargando.value = false
    }
  }

  /** Guarda la receta en el historial y pasa la cita a ATENDIDA. No envia si la receta esta vacia. */
  async function finalizar(): Promise<boolean> {
    if (!datos.value || !recetaLista.value || enviando.value) return false
    enviando.value = true
    error.value = null
    try {
      await srv().registrar(datos.value.citaId, medicacion.value.trim())
      finalizada.value = true
      datos.value = { ...datos.value, estado: 'ATENDIDA' }
      return true
    } catch (e) {
      error.value = mensajeDeError(e)
      return false
    } finally {
      enviando.value = false
    }
  }

  return { datos, medicacion, cargando, enviando, error, finalizada, caracteres, recetaLista, puedeFinalizar, usar,
    cargar, finalizar }
})
