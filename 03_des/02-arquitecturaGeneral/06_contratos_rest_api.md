# ARQ-06 — Borrador de contratos REST (NO IMPLEMENTADO)

**Convención propuesta:** prefijo `/api/v1`; respuestas JSON; autenticación y roles bajo Spring Security o equivalente por definir. **No se ha aprobado JWT, OAuth ni nombres de endpoints.** Cada endpoint debe vincularse con el contrato funcional y RU/RF/HU/CU; la BD no es accesible desde Flutter.

| Método y ruta de ejemplo | Actor | Entrada mínima | Salida de éxito | CU | Errores / restricciones |
|---|---|---|---|---|---|
| `POST /auth/login` | Todos | credenciales | contexto autenticado | 01 | 401,403 |
| `GET /usuarios` / `PATCH /usuarios/{id}/roles` | Administrador | filtro / asignación de rol | usuario/roles | 02 | 401,403,422 |
| `POST /pacientes/autoregistro` | Paciente | nro asegurado, identidad, datos necesarios | referencia paciente o solicitud verificación | 03 (cambio de actor) | 409 duplicado, 422 verificación pendiente |
| `GET /pacientes?numeroAsegurado=` / `POST /pacientes` | Enfermería | afiliación/datos | paciente autorizado | 03 | 403,409 |
| `POST /solicitudes-cita` | Paciente | especialidadId, paciente del contexto | solicitud y resultado de asignación (si ocurre) | 04/05/07 | 422,409, sin disponibilidad |
| `GET /citas/mias` | Paciente | filtros autorizados | citas propias | 08 | 401,403 |
| `GET /citas` | Enfermería | paciente/médico/fecha/estado/consultorio | lista filtrada | 09 | 403 |
| `PATCH /citas/{id}/reprogramacion` | Enfermería | motivo + cambio autorizado + versión | cita actualizada | 10 | 403,404,409 |
| `POST /citas/{id}/cancelacion` | Enfermería | motivo + versión | cita CANCELADA | 11 | 403,404,409 |
| `POST /citas/{id}/llegada` | Secretaría/Enfermería | fechaHoraLlegada | confirmación de llegada | 12/15 (operación propuesta) | 403,409, validación de tiempo |
| `PATCH /citas/{id}/estado` | Rol autorizado | nuevo estado válido | estado persistido | 12 | 403,409, transición inválida |
| `GET /medicos/me/agenda` | Médico | día/semana | agenda autorizada | 13 | 403 |
| `GET /citas/{id}/paciente-autorizado` | Médico | citaId | información mínima permitida | 14 | 403,404 |
| `POST /citas/{id}/atencion` | Médico/enfermería autorizada | asistencia/espera/tiempo de atención | datos guardados | 15 | 403,422 |
| `GET /notificaciones/mias` | Paciente | paginación | buzón | 16/17 | 403 |
| `GET /citas/{id}/historial` | Enfermería/Admin | citaId | acciones autorizadas | 18 | 403 |
| `GET /reportes/citas` | Administrador | rango/agrupación | resumen | 19 | 403 |
| `GET /indicadores/gestion` | Administrador | rango | indicadores derivados | 20 | 403 |

## Contrato transversal de ejemplo
Solicitud CU-04 (propuesta): `{ "especialidadId": "..." }` — la identidad del paciente se resuelve **desde el contexto autenticado**, no se acepta arbitrariamente un `pacienteId` externo.

Éxito asignado (propuesta): `{ "solicitudId":"...", "resultado":"ASIGNADA", "cita": {"id":"...", "inicio":"...", "medicoId":"...", "consultorioId":"...", "estado":"PROGRAMADA"} }`.

Sin disponibilidad: resultado semántico definido por el equipo; **no confirmar** cita falsa. El código HTTP para solicitud aceptada pero sin cupo debe acordarse, sin asumir 409/404 como regla institucional.

## Consistencia y operación
- Validación de esquema y de permisos en servidor.
- Clave de idempotencia propuesta en operaciones de creación para reintentos del cliente (por acordar).
- `409 Conflict` cuando la versión de cita cambió o recurso ya fue ocupado.
- Error `403 Forbidden` cuando actor solicita datos de otro paciente, independientemente de la pantalla.
- ISO 8601 con zona horaria definida y unificada (Bolivia para visualización); configuración pendiente.
- WebSocket NO da respuestas finales de transacción: el resultado verdadero viene de REST.

**Propuesta futura no vigente:** `POST /ofertas/{id}/aceptar`, únicamente tras aprobación del cambio de reasignación; no incluir en contratos finales Sprint 2 por defecto.
