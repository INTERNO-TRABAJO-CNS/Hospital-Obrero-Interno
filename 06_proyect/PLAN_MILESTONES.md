# Plan de Milestones y Sprints — CNS Hospital Obrero

**Versión:** actualizada el 04/11/2026 (fechas reales de los Sprints 1–6; antes: propuesta del 23/10/2026). **Método del proyecto:** Modelo en V (desarrollo y verificación) + tablero Kanban para el flujo. Se conservan los nombres de backlog «Elaboración» y «Construcción» por su utilidad organizativa, sin convertir el proceso en una copia técnica de ChuroViaje.

| GitHub Milestone (título exacto recomendado) | Inicio | Due date en GitHub | Objetivo y salida |
|---|---|---|---|
| Sprint 1 - Documentacion | 2026-10-01 | **2026-10-22** | Documentos y avances ya realizados: no duplicar. |
| Sprint 2 - Arquitectura MVC | 2026-10-23 | **2026-10-29** | Arquitectura Java MVC, dominio, reglas de asignación, UI v1, API, WS y esquema BD. |
| Sprint 3 - Bases e identidad | 2026-10-30 | **2026-11-05** | Base Flutter/Java/PostgreSQL, identidad y pacientes. |
| Sprint 4 - Gestion de citas | 2026-11-06 | **2026-11-12** | Rama T100, catálogos, login/pacientes, agenda médica, historial del paciente (módulos con datos simulados). |
| Sprint 5 - Integracion y seguimiento | 2026-11-13 | **2026-11-20** | Validación y asignación de citas, solicitud, listado/TV, confirmación de asistencia, cancelación, receta, notificaciones y aviso T-45. |
| Sprint 6 - Validacion y entrega | 2026-11-21 | **2026-11-28** | Reportes y supervisión, reprogramar, historial de cambios, integración, pruebas finales y entrega. |

GitHub Milestones registra **fecha límite**, no campo de fecha de inicio; anotar el inicio en la descripción. Fechas confirmadas por el equipo el 04/11/2026 (Sprint 1: 1–22 oct; 2: 23–29 oct; 3: 30 oct–5 nov; 4: 6–12 nov; 5: 13–20 nov; 6: 21–28 nov). El Sprint 1 dejó de estar «sin fecha».

## Qué crear primero en GitHub (sin usar gh)

1. Entrar al repositorio de CNS **INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno** y abrir **Issues → Milestones → New milestone**. Verificar si un Milestone Sprint 1/Sprint 2 ya existe: **editar y reutilizar**, no duplicar.
2. Crear o editar cada título de la tabla. Pegar el objetivo como Description y la fecha final en Due date.
3. En **Issues → Labels**, crear solo etiquetas que no existan: `fase:elaboracion`, `fase:construccion`, `tipo:tarea`, `tipo:prueba`, `tipo:historia-usuario`, `tipo:bug`, `tec:arquitectura`, `tec:java`, `tec:flutter`, `tec:postgresql`, `tec:websocket`, `tec:pruebas`, `tec:servidor`, `estado:bloqueado`.
4. Revisar los issues existentes del Sprint 1 y la arquitectura ya trabajada. Asignarles Milestone correcto desde la barra lateral del issue; cerrarlos únicamente si están terminados.
5. Crear issues nuevos solo después de confirmar que no existen y numerar T/CP disponibles. Usar índices de los dos backlogs para seguir dependencias.
6. Cada issue debe tener: título, descripción, criterios verificables, tareas, responsable real (Assignee), etiquetas, Milestone, referencia RF/CU y dependencias.
7. Organizar un GitHub **Project** con vista **Board** y estado: `Backlog → Por hacer → En progreso → En revisión (PR) → Hecho`. Añadir todos los issues al tablero. El tablero muestra el estado; el Milestone muestra la fecha y porcentaje de issues/PR cerrados.

**Importante sobre los PR:** cuando un PR apunta a `develop` y `main` sigue siendo la rama por defecto, `Closes #N` en la descripción no cierra el issue automáticamente. En ese flujo enlazar manualmente en **Development** y **cerrar el issue a mano al fusionar el PR en develop**. No cerrar antes de revisión. Si el repositorio usa otra rama como default, revisar su configuración.

## Texto para copiar al crear los Milestones

### Sprint 1 - Documentacion

**Description:**

> Inicio: 2026-10-01. Etapa: Elaboración documental (ya finalizada). Objetivo: Documentación, requisitos, investigación y CU-01 a CU-20: conservar issues previos; sin recrearlos. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-10-22`

### Sprint 2 - Arquitectura MVC

**Description:**

> Inicio: 2026-10-23. Etapa: Elaboración. Objetivo: Modelo de dominio, MVC, reglas de asignación y reasignación, diseño UI v1, BD, contratos REST/WebSocket y revisión. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-10-29`

### Sprint 3 - Bases e identidad

**Description:**

> Inicio: 2026-10-30. Etapa: Construcción. Objetivo: Estructuras Java/Flutter, migraciones, seguridad/roles, pacientes y pruebas base. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-05`

### Sprint 4 - Gestion de citas

**Description:**

> Inicio: 2026-11-06. Etapa: Construcción. Objetivo: Rama T100, catálogos, login y pacientes, agenda médica e historial del paciente. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-12`

### Sprint 5 - Integracion y seguimiento

**Description:**

> Inicio: 2026-11-13. Etapa: Construcción. Objetivo: Validación y asignación, solicitud, listado y TV, confirmación de asistencia, cancelación, receta, notificaciones y aviso T-45. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-20`

### Sprint 6 - Validacion y entrega

**Description:**

> Inicio: 2026-11-21. Etapa: Pruebas/Transición. Objetivo: Pruebas de extremo a extremo, concurrencia, seguridad, respaldo, correcciones y entrega. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-28`
