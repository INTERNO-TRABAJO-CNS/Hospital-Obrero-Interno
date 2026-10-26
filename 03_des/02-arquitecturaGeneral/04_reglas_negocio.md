# ARQ-04 — Reglas de negocio y asignación automática

## Reglas confirmadas (26/10/2026)
**RN-01.** El paciente solicita por especialidad, sin escoger médico, fecha, hora ni consultorio (CU-04).

**RN-02.** El motor Java busca la **fecha y hora disponibles más cercanas** compatibles con especialidad, médico, turno y consultorio. Si hoy no hay, continúa al **siguiente día disponible y/o hábil**. El calendario institucional de días hábiles/feriados es PENDIENTE DE VALIDACIÓN.

**RN-03.** En cada combinación, el backend verifica disponibilidad **vigente**. En la confirmación de CU-07 se debe impedir la carrera entre solicitudes (RNF-03/RNF-04).

**RN-04.** Si no existe combinación válida en el período de búsqueda, informa indisponibilidad y no crea cita. El horizonte máximo de búsqueda es **PENDIENTE DE VALIDACIÓN**; no inventar una lista de espera vigente.

**RN-05.** El paciente se registra/identifica por `numeroAseguradoCNS` obligatorio; CI es dato secundario. Se permite auto-registro de cuenta, con procedimiento de verificación de afiliación **por definir**.

**RN-06.** Secretaría/enfermería verifica llegada y puede cancelar por inasistencia después de una tolerancia de **10 minutos respecto de la hora programada**. La cancelación requiere autorización, motivo y auditoría; no se presume automática sin intervención.

**RN-07.** Estados persistidos: PROGRAMADA, ATENDIDA y CANCELADA. Inasistencia es circunstancia/motivo, no estado nuevo.

**RN-08.** El administrador supervisa cumplimiento mediante reportes/indicadores; gestiona usuarios y roles en CU-02. No administrar agendas por defecto.

**RN-09.** WebSocket comunica cambios luego de confirmación persistente; no ejecuta reservas ni define ganadores.

## Proceso principal (Java, diseño; no código)
1. Registrar `SolicitudCita` autenticada e identificar `Paciente` por su afiliación.
2. Buscar la **primera fecha/hora compatible** a partir del punto de inicio definido; filtrar por médico habilitado, especialidad, agenda y consultorio.
3. Evaluar restricciones de superposición, estado y autorizaciones.
4. Bloquear/revalidar disponibilidad con mecanismo de concurrencia respaldado por PostgreSQL.
5. Confirmar `CitaMedica` y su relación con recursos en **una transacción**; persistir evento de auditoría.
6. Solo después del commit, poner en cola evento para notificación y difundir cambio de agenda por WebSocket.
7. Si aparece conflicto simultáneo, descartar esa opción y probar la siguiente; si agota las opciones, comunicar sin disponibilidad.

> Criterio secundario de desempate (dos médicos con idéntica primera hora), horario de inicio y duración de cita: **PENDIENTE DE VALIDACIÓN**. Elegir la hora más próxima no define por sí solo esos parámetros.

## Cambio aprobado en intención, PENDIENTE DE APROBACIÓN DOCUMENTAL
**Reasignación tras cancelación/inasistencia:** el equipo desea incorporarla, pero no está formalmente incorporada a RF/CU del Sprint 1. NO codificar como funcionalidad base del Sprint 2. Ver [decisiones](11_decisiones_pendientes.md) y [secuencia futura](diagramas/ARQ-06_SecuenciaReasignacion_PENDIENTE.puml).

Comportamiento propuesto: enfermería cancela/libera cupo; motor Java identifica citas del día siguiente sin mover citas del día actual; hace ofertas simultáneas según prioridad todavía no aprobada; pacientes aceptan voluntariamente en buzón; backend confirma atómicamente primera aceptación válida, invalida las otras; libera la cita anterior para posible cadena. No quitar cita previa por solo haber ofertado, y no permitir doble adjudicación.

**Pendientes críticos:** criterio de prioridad, tiempo de aceptación, ventana futura, elegibilidad de pacientes cercanos, rechazos, disponibilidad sin conexión, la validación exacta del punto +10 minutos y proceso formal de aprobación.
