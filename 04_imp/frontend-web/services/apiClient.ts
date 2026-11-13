import type { Sesion } from '~/models/medico'
import { useSesionStore } from '~/stores/sesion'

/** Error del backend ({codigo, mensaje}) o de la conexion (SIN_CONEXION). */
export class ApiError extends Error {
  constructor(public codigo: string, mensaje: string, public status: number) {
    super(mensaje)
    this.name = 'ApiError'
  }
}

export interface Api {
  get<T>(ruta: string): Promise<T>
  post<T>(ruta: string, cuerpo?: unknown): Promise<T>
  put<T>(ruta: string, cuerpo?: unknown): Promise<T>
}

export interface OpcionesApi {
  urlBase: string
  /** Usuario actual: su token va en «Authorization: Bearer <token>». */
  sesion: () => Sesion | null
  /** Se llama cuando el backend responde 401 (sesion vencida o cerrada). */
  alNoAutenticado?: (error: ApiError) => void
  fetchFn?: typeof fetch
}

/** Cliente REST unico de la web (mismo comportamiento que api_client.dart de Flutter). */
export function crearApi({ urlBase, sesion, alNoAutenticado, fetchFn = fetch }: OpcionesApi): Api {
  const base = urlBase.replace(/\/+$/, '')

  async function enviar<T>(metodo: string, ruta: string, cuerpo?: unknown): Promise<T> {
    const s = sesion()
    const cabeceras: Record<string, string> = { Accept: 'application/json' }
    if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json'
    if (s?.token) cabeceras.Authorization = `Bearer ${s.token}`
    let respuesta: Response
    try {
      respuesta = await fetchFn(base + (ruta.startsWith('/') ? ruta : `/${ruta}`), {
        method: metodo,
        headers: cabeceras,
        body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
      })
    } catch {
      throw new ApiError('SIN_CONEXION', 'No se pudo conectar con el servidor', 0)
    }
    const datos = leerJson(await respuesta.text())
    if (!respuesta.ok) {
      const error = new ApiError(
        datos?.codigo ?? 'ERROR_HTTP',
        datos?.mensaje ?? `El servidor respondió ${respuesta.status}`,
        respuesta.status,
      )
      if (respuesta.status === 401 && s) alNoAutenticado?.(error)
      throw error
    }
    return datos as T
  }

  return {
    get: <T>(ruta: string) => enviar<T>('GET', ruta),
    post: <T>(ruta: string, cuerpo?: unknown) => enviar<T>('POST', ruta, cuerpo ?? {}),
    put: <T>(ruta: string, cuerpo?: unknown) => enviar<T>('PUT', ruta, cuerpo ?? {}),
  }
}

/** Una respuesta que no es JSON (un proxy, un error HTML) no rompe la app. */
function leerJson(texto: string): any {
  if (!texto) return null
  try {
    return JSON.parse(texto)
  } catch {
    return null
  }
}

/** Cliente configurado con NUXT_PUBLIC_API_URL y la sesion actual. */
export function useApi(): Api {
  const config = useRuntimeConfig()
  const sesion = useSesionStore()
  return crearApi({
    urlBase: String(config.public.apiUrl),
    sesion: () => sesion.usuario,
    alNoAutenticado: (e) => {
      sesion.sesionPerdida(e.message)
      navigateTo('/login')
    },
  })
}

/** Mensaje para mostrar al usuario a partir de cualquier error. */
export function mensajeDeError(e: unknown): string {
  if (e instanceof ApiError) return e.message
  if (e instanceof Error) return e.message
  return 'Ocurrió un error inesperado'
}
