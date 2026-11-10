import { describe, expect, it, vi } from 'vitest'
import { ApiError, crearApi } from '~/services/apiClient'

function respuesta(status: number, cuerpo: unknown) {
  return new Response(cuerpo === undefined ? '' : JSON.stringify(cuerpo), { status })
}

describe('apiClient', () => {
  it('envia los 3 headers temporales y arma la URL', async () => {
    const fetchFn = vi.fn().mockResolvedValue(respuesta(200, { ok: true }))
    const api = crearApi({
      urlBase: 'http://localhost:8080/',
      sesion: () => ({ usuarioId: 5, rol: 'MEDICO', personaId: 7 }),
      fetchFn,
    })

    await api.get('/api/v1/medico/yo')

    const [url, opciones] = fetchFn.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/v1/medico/yo')
    expect(opciones.headers['X-Usuario-Id']).toBe('5')
    expect(opciones.headers['X-Rol']).toBe('MEDICO')
    expect(opciones.headers['X-Persona-Id']).toBe('7')
  })

  it('un 409 con {codigo} lanza ApiError con ese codigo', async () => {
    const fetchFn = vi.fn().mockResolvedValue(
      respuesta(409, { codigo: 'ESTADO_NO_PERMITE', mensaje: 'La cita debe tener la asistencia confirmada' }))
    const api = crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn })

    const error = await api.post('/api/v1/citas/1/atencion', { medicacion: 'x' }).catch(e => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.codigo).toBe('ESTADO_NO_PERMITE')
    expect(error.status).toBe(409)
    expect(error.message).toBe('La cita debe tener la asistencia confirmada')
  })

  it('sin sesion no envia headers X-*', async () => {
    const fetchFn = vi.fn().mockResolvedValue(respuesta(200, {}))
    await crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn }).get('/a')

    expect(fetchFn.mock.calls[0][1].headers['X-Rol']).toBeUndefined()
  })

  it('si no hay conexion lanza SIN_CONEXION', async () => {
    const fetchFn = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'))

    const error = await crearApi({ urlBase: 'http://x', sesion: () => null, fetchFn }).get('/a').catch(e => e)

    expect(error.codigo).toBe('SIN_CONEXION')
  })
})
