# [S2-DOC-04][Docs][CU-10/CU-11/CU-12/CU-15/CU-18] Cerrar MDL-04 (llegada, reprogramación, cancelación, atención, historial)

| Campo | Valor |
|---|---|
| Milestone | Sprint 2 - Arquitectura MVC (vence 2026-10-30) |
| Responsable | Matías (`mtiasgarcia1712`) |
| Revisor | Alejandro |
| Rama (desde `develop`, actualizada: `git merge develop`) | `feature/S2-M04-ciclo-vida-citas (ya existe en remoto; ver estado)` |
| Labels | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| Puntos | 2 |
| Origen | issue propuesto `S2-M04` (esta tarjeta es su **cierre**, no un duplicado) |

## Objetivo
Dejar MDL-04 correcto en el repo (nombre, carpeta y rama) y con las clases MVC de las operaciones sobre la cita.

## Estado actual (medido en el repo el 2026-10-29)

- Tu commit `212f9ee` está en `feature/S2-integracion-modelo-dominio` (nombre ambiguo), **no** en `feature/S2-M04-ciclo-vida-citas`.
- Subiste `modelo/MDL-04_operaciones_historial.mdy` (extensión equivocada: debe ser `.md`) y un `.puml` dentro de `modelo/`; ya existe `MDL-04_operaciones_historial.md` (borrador del integrador) y el diagrama va en `diagramas/MDL-04_Operaciones.puml`.
- La rama se creó sobre una versión vieja de `develop` (le faltan los cambios de `04_imp`): hay que actualizarla antes del PR.

## Tareas

- [ ] Mover tu trabajo a `feature/S2-M04-ciclo-vida-citas` (o renombrar la rama) y hacer `git merge develop` en ella.
- [ ] Fusionar el contenido de `.mdy` dentro de `MDL-04_operaciones_historial.md`, borrar el `.mdy` y mover el `.puml` a `diagramas/MDL-04_Operaciones.puml`.
- [ ] Completar: transiciones de estado confirmadas (PROGRAMADA→ATENDIDA, PROGRAMADA→CANCELADA, reprogramar mantiene PROGRAMADA), tolerancia de 10 min (PV-09 sigue **pendiente**: marcarlo así).
- [ ] Agregar la sección «Clases MVC» por CU (tabla de abajo).
- [ ] Abrir PR → `develop` y pedir revisión a Alejandro.

## Casos de uso de este módulo: qué hay que dejar escrito por cada uno

Para **cada** CU, en el `MDL-0X` agregar la sección **«Clases MVC»** con la tabla de abajo (nombres propuestos, ajustables) y el mapeo de cada flujo `[Na]` a un resultado.

### CU-10 — Reprogramar cita  (RF-10, Must)

> **HU-10: Como personal de enfermería /admisión, quiero reprogramar una cita para ajustarla cuando sea necesario sin provocar conflictos de horario.**  
> **CA:** Dado que existe una cita registrada, cuando se solicita su reprogramación, entonces el sistema deberá validar nuevamente médico, horario y consultorio antes de guardar los cambios.

- Diagrama: `03_des/01-diagramaCompor/CU-10_ReprogramarCita (1).puml`
- Flujos alternativos que el diseño debe resolver: `[3a]` No modifica, solicita otra disponibilidad; `[3b]` Impide doble asignación, requiere nueva validación

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | OperacionCitaService.reprogramar, CambioCita, CambioCitaRepository |
| Infrastructure (`infrastructure/pg`) | PgCitaRepository, PgCambioCitaRepository |
| Controller (`controller`) | CitaOperacionController  PATCH /api/v1/citas/{id}/reprogramacion |

### CU-11 — Cancelar cita  (RF-11, Must)

> **HU-11: Como personal de enfermería / admisión, quiero cancelar una cita indicando el motivo para mantener un registro de lo ocurrido.**  
> **CA:** Dado que existe una cita activa, cuando el usuario solicita cancelarla, entonces el sistema deberá requerir el motivo, cambiar su estado a cancelada y conservar el registro.

- Diagrama: `03_des/01-diagramaCompor/CU-11_CancelarCita (1).puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Informa que no puede cancelarse el registro; `[3a]` No cancela, solicita registrar el motivo

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | OperacionCitaService.cancelar, CambioCita |
| Infrastructure (`infrastructure/pg`) | PgCitaRepository, PgCambioCitaRepository |
| Controller (`controller`) | CitaOperacionController  POST /api/v1/citas/{id}/cancelacion |

### CU-12 — Actualizar estado de cita  (RF-12, Must)

> **HU-12: Como médico o personal autorizado, quiero actualizar el estado de una cita para reflejar lo que ocurrió con la atención.**  
> **CA:** Dado que existe una cita registrada, cuando ocurre su atención, cancelación o inasistencia, entonces el sistema deberá reflejar el estado correspondiente.

- Diagrama: `03_des/01-diagramaCompor/CU-12_ActualizarEstadoCita.puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Rechaza cambio y conserva estado anterior; `[2b]` No modifica la cita, informa estado inválido

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | OperacionCitaService.actualizarEstado (reglas de transición en CitaMedica) |
| Infrastructure (`infrastructure/pg`) | PgCitaRepository |
| Controller (`controller`) | CitaOperacionController  PATCH /api/v1/citas/{id}/estado |

### CU-15 — Registrar datos de atención  (RF-15, Must)

> **HU-15: Como personal médico, quiero registrar los datos de la atención para conocer el comportamiento real del proceso de citas.**  
> **CA:** Dado que una cita llega al proceso de atención, cuando se registran sus datos operativos, entonces el sistema deberá asociar a la cita la asistencia del paciente, el tiempo de espera y el tiempo de atención registrados.

- Diagrama: `03_des/01-diagramaCompor/CU-15_RegistrarDatosAtencion.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Impide registrar datos de atención; `[3a]` No guarda, solicita corregir datos

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | DatosAtencion, DatosAtencionDTO, DatosAtencionRepository, AtencionService.registrar |
| Infrastructure (`infrastructure/pg`) | PgDatosAtencionRepository |
| Controller (`controller`) | AtencionController  POST /api/v1/citas/{id}/atencion |

### CU-18 — Consultar historial de cambios  (RF-18, Must)

> **HU-18: Como responsable de la gestión de citas, quiero consultar el historial de cambios para saber qué se modificó, quién lo hizo y cuándo.**  
> **CA:** Dado que una cita es modificada, reprogramada o cancelada, cuando la operación se guarda, entonces el sistema deberá registrar la acción, el usuario responsable y la fecha/hora correspondiente.

- Diagrama: `03_des/01-diagramaCompor/CU-18_ConsultarHistorialCambios.puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Impide el acceso a la trazabilidad; `[3a]` Informa que no hay cambios adicionales

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | HistorialService.historialDeCita, CambioCitaDTO |
| Infrastructure (`infrastructure/pg`) | PgCambioCitaRepository |
| Controller (`controller`) | CitaController  GET /api/v1/citas/{id}/historial |

Rutas tomadas del borrador `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md` (ARQ-06, no aprobado). Si una decisión está marcada PENDIENTE (PV-xx), **proponer y marcar como propuesta**: no bloquear la entrega.

## Definition of Done

- [ ] `.md` y `.puml` del módulo completos, en la ruta correcta, y coherentes con el DER real.
- [ ] Sección «Clases MVC» por cada CU del módulo.
- [ ] PR a `develop` con la rama al día (`git merge develop` antes de abrir el PR), revisado y aprobado por otra persona.
- [ ] Entregado **antes del 2026-10-30**; si no se logra, se avisa hoy y se registra como arrastre al Sprint 3 (no se entrega callado fuera de fecha).

## Registro
Issue `#____` · PR `#____`
