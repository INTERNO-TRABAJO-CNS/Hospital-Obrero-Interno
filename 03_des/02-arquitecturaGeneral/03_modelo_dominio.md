# ARQ-03 — Modelo de dominio integrado (PROPUESTA V1)

**Estado:** pendiente de cotejar con el diagrama de clases y DER PostgreSQL que el equipo declara ya diseñados. **No son nombres aprobados de tablas o código**. Su propósito es acordar dueños y contratos antes de codificar.

## Lenguaje ubicuo
- **Persona:** identidad personal general, distinta del acceso digital.
- **Paciente/Asegurado:** persona afiliada identificada de forma principal por **número de asegurado CNS**; CI solo atributo de identificación complementario, no sustituto de afiliación.
- **Usuario:** cuenta capaz de autenticarse; puede estar asociada a un paciente o un profesional; no todo paciente debe presumirse con cuenta preexistente.
- **SolicitudCita:** petición por especialidad; **no** es todavía una cita confirmada.
- **Agenda / franja / recurso:** disponibilidad utilizable del médico y consultorio para una especialidad y fecha/hora.
- **CitaMedica:** asignación ya confirmada a paciente, médico, especialidad, consultorio e intervalo.
- **RegistroLlegada:** constancia de llegada/confirmación por Secretaría; no constituye un estado independiente de la cita.
- **CambioCita:** evento auditable de cambio de estado o reprogramación/cancelación.
- **DatosAtencion:** registro de asistencia, espera y atención autorizado.
- **Notificacion/Recordatorio:** mensajes emitidos por cambios confirmados.

## Propiedad de entidades por submodelo
| Dueño | Entidades / agregado | Referenciadas por |
|---|---|---|
| MDL-01 Alejandro | Persona, Paciente, Usuario, Rol, Permiso | Todos |
| MDL-02 Alex | Medico, Especialidad, Consultorio, AgendaMedica, FranjaDisponible | MDL-03/04/05 |
| MDL-03 Benjamín | **SolicitudCita, CitaMedica** (único dueño de clase y repositorio de citas) | MDL-04/05 |
| MDL-04 Matías | CambioCita, RegistroLlegada, DatosAtencion, RegistroAuditoria de operaciones | MDL-03/05 |
| MDL-05 Abigal | Notificacion, Recordatorio, vistas de consulta, indicadores derivados | MDL-01/02/03/04 |

Estas agrupaciones son **responsabilidades de diseño**, no crean cinco bases de datos ni cinco clases `CitaMedica` distintas. Los otros módulos interactúan mediante identificadores y contratos.

## Relaciones propuestas a contrastar
1. Persona 1 — 0..1 Paciente; Persona 1 — 0..1 Usuario (cardinalidad por validar: identidad de personal y cuentas múltiples).
2. Paciente 1 — 0..* SolicitudCita, 1 — 0..* CitaMedica.
3. Medico * — * Especialidad (validar si el modelo existente usa solo 1 especialidad por médico).
4. Medico 1 — 0..* AgendaMedica; AgendaMedica contiene franjas de disponibilidad.
5. CitaMedica referencia **una** especialidad, **un** médico, **un** consultorio, **un** paciente y un intervalo.
6. CitaMedica 1 — 0..* CambioCita / 0..1 o 0..* DatosAtencion según reglas actuales; revisión requerida.
7. CitaMedica 1 — 0..* Notificacion y 0..* eventos de auditoría.

## Estados y transiciones confirmados por respuesta del equipo
`PROGRAMADA`, `ATENDIDA`, `CANCELADA`. **INEXISTENTE no es un estado persistido**; significa ausencia de cita. `INASISTENTE` no se agrega como estado: se documenta como motivo del proceso de cancelación/registro de asistencia, sujeto a ajuste del RF-12.

- Nueva cita validada: `PROGRAMADA`.
- Atención registrada: `PROGRAMADA → ATENDIDA`.
- Cancelación autorizada con motivo: `PROGRAMADA → CANCELADA`.
- Reprogramación: permanece `PROGRAMADA`, modifica recursos/fecha de forma atómica y deja historial.
- No se habilitan reversión de atendida/cancelada sin regla aprobada.

## Invariantes estructurales
- `numeroAseguradoCNS` indispensable y único para paciente (política de univocidad por validar contra DER vigente).
- No confirmar recursos superpuestos con la misma agenda/médico/consultorio; ver [concurrencia](07_base_de_datos.md).
- Una solicitud fallida NO fabrica una cita ni ocupa un recurso.
- Cada transición registra autor, instante y motivo cuando aplica.
- Un usuario no puede acceder a citas de otro paciente sin autorización.

## Diagrama
[ARQ-03_ModeloDominio.puml](diagramas/ARQ-03_ModeloDominio.puml). Debe compararse **clase por clase** con el DER real, no copiarse directamente a JPA ni generar SQL automáticamente.
