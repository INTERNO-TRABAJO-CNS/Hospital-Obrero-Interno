# [S2-DOC-03][Docs][CU-08/CU-09/CU-16/CU-17/CU-19/CU-20] Cerrar MDL-05 (consultas, avisos, reportes) y subir los diseños UI al repo

| Campo | Valor |
|---|---|
| Milestone | Sprint 2 - Arquitectura MVC (vence 2026-10-30) |
| Responsable | Abigail (`AbigailFarfan`) |
| Revisor | Alex |
| Rama (desde `develop`, actualizada: `git merge develop`) | `feature/S2-M05-consultas-comunicacion-ui (crearla; no existe)` |
| Labels | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| Puntos | 3 |
| Origen | issue propuesto `S2-M05` (esta tarjeta es su **cierre**, no un duplicado) |

## Objetivo
Entregar el submodelo de consultas/comunicación/reportes y dejar en el repo los diseños de pantalla que `09_interfaz_flutter_v1.md` dice «conservar».

## Estado actual (medido en el repo el 2026-10-29)

- Tu único commit en el repo es la subida de `Investigacion_concurrencia_citas_Hospital_Obrero.docx` (rama INV-009).
- `modelo/MDL-05_consultas_comunicacion_reportes.md` sigue siendo el borrador del integrador.
- `03_des/diseñoUI/` solo tiene un `README.md`: los diseños de pantalla **no están en el repo**.

## Tareas

- [ ] Completar `MDL-05_consultas_comunicacion_reportes.md` (objetos propios, contratos que consume, riesgos) y `diagramas/MDL-05_Comunicacion.puml`.
- [ ] Subir los diseños UI existentes a `03_des/diseñoUI/` (export PNG/PDF + enlace al Figma/herramienta) y vincular cada pantalla a su CU en `09_interfaz_flutter_v1.md`. Si una pantalla **no existe**, decirlo: no inventar que está hecha.
- [ ] Agregar la sección «Clases MVC» por CU (tabla de abajo).
- [ ] Convertir tu investigación de concurrencia (`.docx`) a `.md` (se puede revisar en PR y buscar texto) — opcional, si queda tiempo.
- [ ] Abrir PR `feature/S2-M05-consultas-comunicacion-ui` → `develop`.

## Casos de uso de este módulo: qué hay que dejar escrito por cada uno

Para **cada** CU, en el `MDL-0X` agregar la sección **«Clases MVC»** con la tabla de abajo (nombres propuestos, ajustables) y el mapeo de cada flujo `[Na]` a un resultado.

### CU-08 — Consultar citas del paciente  (RF-08, Must)

> **HU-08: Como paciente, quiero consultar mis citas para conocer cuándo, dónde y con quién seré atendido.**  
> **CA:** Dado que el paciente tiene citas registradas, cuando consulta sus citas, entonces el sistema deberá mostrar únicamente las que le pertenecen con médico, especialidad, fecha, hora, consultorio y estado.

- Diagrama: `03_des/01-diagramaCompor/CU-08_ConsultarCitasPaciente.puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Muestra que no existen citas para el paciente; `[4a]` Rechaza acceso a datos ajenos

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | ConsultaCitaService.citasDelPaciente, CitaResumenDTO |
| Infrastructure (`infrastructure/pg`) | PgCitaRepository.search |
| Controller (`controller`) | CitaController  GET /api/v1/citas/mias |

### CU-09 — Consultar citas registradas  (RF-09, Must)

> **HU-09: Como personal de enfermería /admisión, quiero buscar citas con diferentes criterios para localizar rápidamente la información necesaria.**  
> **CA:** Dado que existen citas registradas, cuando el usuario aplica uno de los criterios habilitados, entonces el sistema deberá presentar las citas coincidentes.

- Diagrama: `03_des/01-diagramaCompor/CU-09_ConsultarCitasRegistradas (1).puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Solicita corregir el criterio de búsqueda; `[3a]` Informa que no se encontraron citas

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | ConsultaCitaService.buscar(filtro), CitaResumenDTO |
| Infrastructure (`infrastructure/pg`) | PgCitaRepository.search |
| Controller (`controller`) | CitaController  GET /api/v1/citas |

### CU-16 — Generar notificación de cita  (RF-16, Should)

> **HU-16: Como paciente, quiero recibir información cuando mi cita sea creada o cambie para conocer oportunamente su situación.**  
> **CA:** Dado que una cita es registrada, modificada o cancelada, cuando la operación queda confirmada, entonces el sistema deberá generar la notificación correspondiente para el paciente.

- Diagrama: `03_des/01-diagramaCompor/CU-16_GenerarNotificacionCita.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` No genera avisos de cambios inexistentes; `[2a]` No genera aviso incompleto, requiere revisión

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | Notificacion, NotificacionDTO, NotificacionRepository, NotificacionService.generarPorEvento |
| Infrastructure (`infrastructure/pg`) | PgNotificacionRepository |
| Controller (`controller`) | NotificacionController  GET /api/v1/notificaciones/mias |

### CU-17 — Generar recordatorio de cita  (RF-17, Should)

> **HU-17: Como paciente, quiero recibir un recordatorio de mi cita para reducir la posibilidad de olvidarla.**  
> **CA:** Dado que existe una cita programada, cuando se aproxima su fecha conforme a la configuración establecida, entonces el sistema deberá generar el recordatorio correspondiente.

- Diagrama: `03_des/01-diagramaCompor/CU-17_GenerarRecordatorioCita.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` No genera recordatorio todavía; `[2a]` No genera aviso con datos cancelados o antiguos

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | Recordatorio, RecordatorioRepository, RecordatorioService (proceso programado) |
| Infrastructure (`infrastructure/pg`) | PgRecordatorioRepository |
| Controller (`controller`) | mismo buzón de CU-16 |

### CU-19 — Generar reportes de citas  (RF-19, Should)

> **HU-19: Como administrador, quiero consultar reportes de citas para supervisar el funcionamiento del proceso de atención.**  
> **CA:** Dado que existen datos de citas registrados, cuando el administrador solicita un reporte contemplado, entonces el sistema deberá agrupar y presentar la información según el criterio seleccionado.

- Diagrama: `03_des/01-diagramaCompor/CU-19_GenerarReportesCitas.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Impide el acceso al módulo de reportes; `[3a]` Informa ausencia de resultados

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | ReporteService.reportePorCriterio, ReporteCitasDTO |
| Infrastructure (`infrastructure/pg`) | PgReporteRepository (select agrupado / count que devuelve DTO) |
| Controller (`controller`) | ReporteController  GET /api/v1/reportes/citas |

### CU-20 — Consultar indicadores de gestión  (RF-20, Should)

> **HU-20: Como administrador, quiero analizar indicadores de la gestión de citas para apoyar la toma de decisiones del hospital.**  
> **CA:** Dado que existen datos consolidados de las citas, cuando se consulta el componente analítico, entonces el sistema deberá mostrar los indicadores disponibles calculados a partir de los registros almacenados.

- Diagrama: `03_des/01-diagramaCompor/CU-20_ConsultarIndicadoresGestion.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Deniega acceso al componente analítico; `[3a]` Indica falta de datos, no muestra valores sin respaldo

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | IndicadorService.indicadores, IndicadorDTO |
| Infrastructure (`infrastructure/pg`) | PgReporteRepository |
| Controller (`controller`) | IndicadorController  GET /api/v1/indicadores/gestion |

Rutas tomadas del borrador `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md` (ARQ-06, no aprobado). Si una decisión está marcada PENDIENTE (PV-xx), **proponer y marcar como propuesta**: no bloquear la entrega.

## Definition of Done

- [ ] `.md` y `.puml` del módulo completos, en la ruta correcta, y coherentes con el DER real.
- [ ] Sección «Clases MVC» por cada CU del módulo.
- [ ] PR a `develop` con la rama al día (`git merge develop` antes de abrir el PR), revisado y aprobado por otra persona.
- [ ] Entregado **antes del 2026-10-30**; si no se logra, se avisa hoy y se registra como arrastre al Sprint 3 (no se entrega callado fuera de fecha).

## Registro
Issue `#____` · PR `#____`
