# ARQ-08 — Seguridad, roles y privacidad

**Fuente:** RF-01/02, RNF-01/02/05; el mecanismo de autenticación técnico todavía no está elegido en la fuente original.

| Función | Paciente | Enfermería/Secretaría | Médico | Admin |
|---|---:|---:|---:|---:|
| Crear cuenta / consultar sus citas | Sí / Sí | Consulta autorizada | No por defecto | No por defecto |
| Registrar/buscar pacientes | Autoregistro propio* | Sí | No por defecto | No por defecto |
| Solicitar especialidad para su cita | Sí | Según autorización de CU-07 | No | No |
| Registrar llegada / cancelar por inasistencia | No | Sí | No | No |
| Consultar agenda propia / datos del paciente citado | No | Vista autorizada* | Sí | No por defecto |
| Registrar atención | No | Solo si autorizado | Sí | No |
| Gestión usuarios y roles (CU-02) | No | No | No | Sí |
| Reportes e indicadores generales | No | No por defecto | No por defecto | Sí |

`*` El alcance exacto y los campos visibles requieren revisión contra CU y acuerdos institucionales. Este cuadro no amplía permisos sin validación.

## Controles fundamentales
- Validar autorización en Spring Boot para **cada endpoint y cada registro**, no solo en Flutter.
- Nunca confiar en el `pacienteId` enviado por cliente para consultar citas ajenas: vincular al sujeto autenticado.
- Acceso del médico a la información de paciente **solo** si existe relación autorizada con una cita.
- Separar los datos mínimos operativos de una historia clínica extensa (fuera de alcance).
- Trazabilidad de cambios sin exponer contraseñas, tokens ni campos clínicos sensibles en logs/eventos.
- En tránsito TLS para REST y WSS para WebSocket en producción (arquitectura recomendada; infraestructura pendiente).
- La verificación de número de asegurado en auto-registro y recuperación de cuenta sigue **PENDIENTE DE VALIDACIÓN**.
