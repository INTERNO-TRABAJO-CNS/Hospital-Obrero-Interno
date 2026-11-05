# ARQ-07 — PostgreSQL y alineación del modelo existente

**Importante:** el equipo ya diseñó tablas y un DER; este archivo **NO sustituye** esas tablas ni provee SQL de migración. Benjamín debe cotejar nombres reales antes de aprobar la arquitectura.

## Trazabilidad conceptual → DER real
| Concepto de dominio | Entidad/tabla DER existente | Responsable | Estado de cotejo |
|---|---|---|---|
| Persona / Usuario / Rol / Permiso | **PENDIENTE de nombre exacto** | Alejandro | No verificado |
| Paciente (número asegurado CNS) | **PENDIENTE** | Alejandro | No verificado |
| Médico / Especialidad / Consultorio | **PENDIENTE** | Alex | No verificado |
| Agenda médica / turno / intervalo | **PENDIENTE** | Alex | No verificado |
| SolicitudCita | **PENDIENTE** | Benjamín | No verificado |
| CitaMedica | **PENDIENTE** | Benjamín | No verificado |
| Registro de llegada, cambios, atención | **PENDIENTE** | Matías | No verificado |
| Notificación / auditoría | **PENDIENTE** | Abigal / Matías | No verificado |

## Restricciones imprescindibles (criterios, no SQL)
1. Única identificación principal por número de asegurado (confirmar collation, duplicados históricos e integración con afiliación).
2. FKs consistentes entre cita, paciente, médico, especialidad, consultorio y agenda.
3. Evitar solapamientos de intervalos activos tanto de **médico** como de **consultorio**; la combinación de índice único simple es insuficiente si hay duraciones variables. Considerar `EXCLUDE USING gist` sobre `tstzrange` y `btree_gist`, o transacciones/bloqueos y constraints equivalentes, a definir tras revisar esquema real.
4. Transacción ACID que valida y confirma la cita en una sola operación; controlar carreras de selección, cambios y aceptación futura.
5. Versionado optimista de CitaMedica (`version` propuesta) más comprobación transaccional de recursos; no depender solo de `@Version` para impedir dos citas distintas sobre mismo médico/consultorio.
6. Evento auditable de cancelación, reprogramación, llegada o atención con usuario, hora, motivo cuando corresponda.
7. Si se usa entrega asincrónica de notificaciones, considerar patrón **outbox transaccional** o mecanismo equivalente para no perder eventos; propuesta no implementada.
8. Información de paciente mínima según rol, credenciales hasheadas, migraciones versionadas, respaldo y no uso de datos reales para pruebas.

## Tarea de integración
Cuando se disponga del archivo DER/SQL ya diseñado, llenar la columna de tablas exactas; comprobar claves y cardinalidades; corregir diagrama `.puml` del dominio para reflejar el modelo real; adjuntar evidencia en PR. Si el esquema contradice requisitos, registrar decisión antes de cambiarlo.
