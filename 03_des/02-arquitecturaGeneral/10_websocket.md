# ARQ-10 — Comunicación WebSocket

**Propósito:** disminuir necesidad de refrescar vistas tras eventos **ya confirmados** en Java y PostgreSQL.

| Evento conceptual | Emisor después de commit | Destinatario permitido | Carga mínima | Acción de vista |
|---|---|---|---|---|
| `cita.confirmada` | M03 | paciente propietario, agenda del médico y Secretaría autorizados | eventoId,citaId,version,timestamp | refrescar consulta / aviso |
| `cita.reprogramada` | M04 | paciente y personal autorizado | eventoId,citaId,version | actualizar agenda |
| `cita.cancelada` | M04 | paciente y personal autorizado | eventoId,citaId,version | actualizar estado |
| `atencion.registrada` | M04 | personal que tenga permiso | eventoId,citaId,version | refrescar reporte |
| `notificacion.disponible` | M05 | destinatario | eventoId,notificacionId | actualizar buzón |

**Propuesta técnica, pendiente:** suscripciones autenticadas, filtradas por rol/identidad, con token/canal seguro; clientes reconectan y consultan REST como fuente de verdad.

**Prohibición:** no reservar, modificar estado o decidir ganadores de reasignación por un mensaje entrante WebSocket; aceptar/cancelar se realiza mediante REST con control transaccional.

**Cambio futuro:** `oferta.creada`, `oferta.expirada`, `oferta.invalida` requieren validación de la funcionalidad de reasignación y no se incluyen en API vigente.
