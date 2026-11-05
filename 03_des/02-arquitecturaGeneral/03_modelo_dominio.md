# ARQ-03 — Modelo de dominio integrado (PROPUESTA V1)

**Modificado 04/11/2026** (estados, entidades y dueños). **Estado:** pendiente de cotejar con el diagrama de clases y DER PostgreSQL que el equipo declara ya diseñados. **No son nombres aprobados de tablas o código**. Su propósito es acordar dueños y contratos antes de codificar.

## Lenguaje ubicuo
- **Persona:** identidad personal general, distinta del acceso digital.
- **Paciente/Asegurado:** persona afiliada identificada de forma principal por **número de asegurado CNS**; CI solo atributo de identificación complementario, no sustituto de afiliación.
- **Usuario:** cuenta capaz de autenticarse; puede estar asociada a un paciente o un profesional; no todo paciente debe presumirse con cuenta preexistente.
- **SolicitudCita:** petición por especialidad y día; **no** es todavía una cita confirmada. Estados: PENDIENTE, EN_ESPERA, ASIGNADA, SIN_CUPO.
- **Agenda / franja / recurso:** disponibilidad utilizable del médico y consultorio para una especialidad y fecha/hora.
- **CitaMedica:** asignación ya confirmada a paciente, médico, especialidad, consultorio e intervalo.
- **RegistroLlegada:** constancia de la confirmación de asistencia por enfermería (CU-12); la cita pasa a ASISTENCIA_CONFIRMADA.
- **CambioCita:** evento auditable de cambio de estado o reprogramación/cancelación.
- **Receta:** medicación que el médico agrega al historial clínico al finalizar (CU-15). **DatosAtencion se elimina** (04/11/2026): llegada → RegistroLlegada; fin → `CitaMedica.horaFinAtencion`; receta → Receta.
- **Notificacion/Recordatorio:** mensajes emitidos por cambios confirmados.

## Propiedad de entidades por persona (04/11/2026)
| Dueño | Entidades / agregado | Referenciadas por |
|---|---|---|
| Matías (CU-01, 03, 04, 08, 10) | Persona, Paciente, Afiliado, Usuario, Rol, SolicitudCita (alta) | Todos |
| Benjamín (CU-02, 05, 06, 07, 19, 20) | Medico, Especialidad, Consultorio, AgendaMedica, FranjaDisponible, DiaNoHabil, **CitaMedica** (único dueño del repositorio de citas) | Todos |
| Alejandro (CU-09, 12, 13) | RegistroLlegada, vistas de listado y agenda | Alex, Abigail |
| Alex (CU-11, 14, 18) | CambioCita, HistorialClinico (lectura) | Benjamín, Abigail |
| Abigail (CU-15, 16, 17) | Receta, Notificacion, Recordatorio | Alex |

Estas agrupaciones son **responsabilidades de diseño**, no crean cinco bases de datos ni cinco clases `CitaMedica` distintas. Los otros módulos interactúan mediante identificadores y contratos.

## Relaciones propuestas a contrastar
1. Persona 1 — 0..1 Paciente; Persona 1 — 0..1 Usuario (cardinalidad por validar: identidad de personal y cuentas múltiples).
2. Paciente 1 — 0..* SolicitudCita, 1 — 0..* CitaMedica.
3. Medico * — * Especialidad (validar si el modelo existente usa solo 1 especialidad por médico).
4. Medico 1 — 0..* AgendaMedica; AgendaMedica contiene franjas de disponibilidad.
5. CitaMedica referencia **una** especialidad, **un** médico, **un** consultorio, **un** paciente y un intervalo.
6. CitaMedica 1 — 0..* CambioCita; 0..1 RegistroLlegada; 0..1 Receta.
7. CitaMedica 1 — 0..* Notificacion y 0..* eventos de auditoría.

## Estados y transiciones (04/11/2026)
**CitaMedica:** `PROGRAMADA`, `ASISTENCIA_CONFIRMADA`, `ATENDIDA`, `CANCELADA`.
**SolicitudCita:** `PENDIENTE`, `EN_ESPERA`, `ASIGNADA`, `SIN_CUPO`.

- Cita asignada y registrada: `PROGRAMADA`.
- Enfermería confirma asistencia hasta T-30: `PROGRAMADA → ASISTENCIA_CONFIRMADA`.
- Médico registra receta y fin: `ASISTENCIA_CONFIRMADA → ATENDIDA`.
- Paciente cancela, o el sistema a T-30 sin confirmación: `PROGRAMADA → CANCELADA` (la cita se conserva).
- Reprogramación = cancelar + nueva solicitud con prioridad menor.
- INEXISTENTE e INASISTENTE no son estados. Sin reversión de ATENDIDA/CANCELADA.

## Invariantes estructurales
- `numeroAseguradoCNS` indispensable y único para paciente (política de univocidad por validar contra DER vigente).
- No confirmar recursos superpuestos con la misma agenda/médico/consultorio; ver [concurrencia](07_base_de_datos.md).
- Una solicitud fallida NO fabrica una cita ni ocupa un recurso.
- Cada transición registra autor, instante y motivo cuando aplica.
- Un paciente tiene como máximo una cita por día; un médico, como máximo 7 por día.
- Un usuario no puede acceder a citas de otro paciente sin autorización.

## Diagrama
[ARQ-03_ModeloDominio.puml](diagramas/ARQ-03_ModeloDominio.puml). Debe compararse **clase por clase** con el DER real, no copiarse directamente a JPA ni generar SQL automáticamente.
