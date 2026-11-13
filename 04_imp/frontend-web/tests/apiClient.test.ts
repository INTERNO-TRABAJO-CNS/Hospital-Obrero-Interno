import { describe, expect, it, vi } from 'vitest'
import { ApiError, crearApi } from '~/services/apiClient'

function respuesta(status: number, cuerpo: unknown) {
  return new Response(cuerpo === undefined ? '' : JSON.stringify(cuerpo), { status })
}

const sesion = () => ({ usuarioId: 5, rol: 'MEDICO', personaId: 7, token: 'abc123' })

describe('apiClient', () => {
  it('envia el token como Authorization: Bearer y arma la URL', async () => {
    const fetchFn = vi.fn().mockResolvedValue(respuesta(200, { ok: true }))
    const api = crearApi({ urlBase: 'http://localhost:8080/', sesion, fetchFn })

    await api.get('/api/v1/medico/yo')

    const [url, opciones] = fetchFn.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/v1/medico/yo')
    expect(opciones.headers.Authorization).toBe('Bearer abc123')
    expect(opciones.headers['X-Rol']).toBeUndefined()
  })

  it('un 409 con {codigo} lanza ApiError con ese codigo', async () => {
    const fetchFn = vi.fn().mockResolvedValue(
      respuesta(409, { codigo: 'ESTADO_NO_PERMITE', mensaje: 'La cita debe tener la asistencia confirmada' }))
    const api = crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn })

    const error = (await api.post('/api/v1/citas/1/atencion', { medicacion: 'x' }).catch((e: ApiError) => e)) as ApiError

    expect(error).toBeInstanceOf(ApiError)
    expect(error.codigo).toBe('ESTADO_NO_PERMITE')
    expect(error.status).toBe(409)
    expect(error.message).toBe('La cita debe tener la asistencia confirmada')
  })

  it('un 401 con sesion avisa que la sesion se perdio', async () => {
    const fetchFn = vi.fn().mockResolvedValue(respuesta(401, { codigo: 'SESION_VENCIDA', mensaje: 'Su sesion vencio' }))
    const alNoAutenticado = vi.fn()
    const api = crearApi({ urlBase: 'http://x', sesion, fetchFn, alNoAutenticado })

    await api.get('/a').catch(() => undefined)

    expect(alNoAutenticado).toHaveBeenCalledOnce()
    expect(alNoAutenticado.mock.calls[0][0].codigo).toBe('SESION_VENCIDA')
  })

  it('sin sesion no envia Authorization', async () => {
    const fetchFn = vi.fn().mockResolvedValue(respuesta(200, {}))
    await crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn }).get('/a')

    expect(fetchFn.mock.calls[0][1].headers.Authorization).toBeUndefined()
  })

  it('una respuesta que no es JSON no rompe: sale ERROR_HTTP', async () => {
    const fetchFn = vi.fn().mockResolvedValue(new Response('<html>Bad gateway</html>', { status: 502 }))

    const error = (await crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn }).get('/a').catch((e: ApiError) => e)) as ApiError

    expect(error.codigo).toBe('ERROR_HTTP')
    expect(error.status).toBe(502)
  })

  it('si no hay conexion lanza SIN_CONEXION', async () => {
    const fetchFn = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'))

    const error = (await crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn }).get('/a').catch((e: ApiError) => e)) as ApiError

    expect((error as ApiError).codigo).toBe('SIN_CONEXION')
  })
})
