# ARQ-04 — Reglas de negocio y asignación automática (v2 — 04/11/2026)

> **Modificado 04/11/2026.** Sustituye la versión del 26/10/2026. Cambios y motivos en `06_proyect/CAMBIOS_04-11-2026.md`. Las reglas marcadas «(cambia)» contradecían decisiones nuevas del equipo.

## Reglas vigentes
**RN-01 (cambia).** El paciente solicita **especialidad y día**; no escoge médico, hora ni consultorio (CU-04). Especialidades: Consultorio Familiar (general), Odontología y Pediatría.

**RN-02 (cambia).** El sistema asigna médico, hora y consultorio. Las solicitudes se atienden por **orden de llegada** (cola). Las de pacientes que cancelaron o reprogramaron entran con **prioridad menor**. Si no hay cupo en el día pedido, el sistema informa **NO HAY CUPOS** y deja la solicitud **EN_ESPERA** para el siguiente día hábil.

**RN-03.** En cada combinación el backend verifica la disponibilidad vigente. La confirmación (CU-07) evita la carrera entre solicitudes simultáneas con PostgreSQL: `SELECT … FOR UPDATE SKIP LOCKED` y restricción `UNIQUE(médico, fecha, hora)`.

**RN-04 (cambia).** La búsqueda llega como máximo a **15 días hábiles**. Si no hay cupo en ese rango la solicitud pasa a **SIN_CUPO** y se informa al paciente. No se crea cita ficticia.

**RN-05 (cambia).** Solo el **asegurado titular** crea cuenta, y el registro se valida contra la **tabla interna de afiliados**. Los beneficiarios no son afiliados: se atienden con la afiliación del titular y su CI. La primera vez se crea su historial clínico.

**RN-06 (cambia).** Enfermería confirma la asistencia del paciente (en el hospital) **hasta 30 minutos antes** de la cita. Si no la confirma, el sistema **cancela automáticamente a T-30** (motivo «No confirmó asistencia»). Se envía un aviso a **T-45**. La cita **nunca se borra**: queda CANCELADA.

**RN-07 (cambia).** Estados de cita: **PROGRAMADA, ASISTENCIA_CONFIRMADA, ATENDIDA, CANCELADA**. Estados de solicitud: **PENDIENTE, EN_ESPERA, ASIGNADA, SIN_CUPO**.

**RN-08 (cambia).** El administrador mantiene los catálogos (médicos, especialidades, consultorios, horarios, cupos, días no hábiles, pacientes, usuarios) y supervisa una **agenda de solo lectura en tiempo real** por WebSocket.

**RN-09.** WebSocket comunica cambios luego del commit; no ejecuta reservas ni define ganadores.

**RN-10 (nueva).** Un paciente no puede tener **más de una cita el mismo día**, ni siquiera en especialidades distintas.

**RN-11 (nueva).** Cada médico atiende **máximo 7 fichas por día**; la **meta mínima es 5**.

**RN-12 (nueva).** Duración por ficha: Consultorio General 30–45 min; Odontología 45–60 min; Pediatría 30–45 min.

**RN-13 (nueva).** El médico solo registra el **fin** de la atención y agrega la receta/medicación al historial clínico; no inicia la cita.

**RN-14 (nueva).** Reprogramar = cancelar la cita vigente (CU-11) y crear una **nueva solicitud** con prioridad menor para el siguiente día hábil.

**RN-15 (nueva).** El cupo liberado por una cancelación **no se reasigna automáticamente** (decisión 04/11/2026; queda para una etapa posterior).

**RN-16 (nueva).** La pantalla de TV es de **solo lectura** y muestra únicamente el listado de fichas del día.

## Proceso principal (Java; diseño)
1. Registrar `SolicitudCita` (PENDIENTE) con su orden de llegada.
2. Tomar la siguiente de la cola (prioridad normal antes que la de quien canceló).
3. Buscar en el día pedido un médico de la especialidad con menos de 7 fichas, hora según duración, consultorio libre y paciente sin otra cita ese día.
4. Bloquear y revalidar con PostgreSQL (`FOR UPDATE SKIP LOCKED`; `UNIQUE` como red de seguridad).
5. En **una transacción**: crear `CitaMedica` PROGRAMADA con número de ficha, marcar la solicitud ASIGNADA, escribir `CambioCita`.
6. Tras el commit: notificar (CU-16) y emitir el cambio por WebSocket.
7. Sin cupo: EN_ESPERA → siguiente día hábil, hasta 15 días hábiles; luego SIN_CUPO.

## Pendientes de validar (abiertos)
- Criterio de desempate entre dos médicos con la misma primera hora.
- Numeración de ficha (propuesta: correlativo por médico y día).
- ¿Puede el paciente cancelar después de ASISTENCIA_CONFIRMADA? (supuesto actual: no).
- Tiempo de espera y de atención por separado no son medibles con los datos actuales (solo confirmación → fin).
