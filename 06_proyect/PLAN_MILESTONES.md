# Plan de Milestones y Sprints — CNS Hospital Obrero

**Versión:** propuesta de planificación del 23/10/2026. **Método del proyecto:** Modelo en V (desarrollo y verificación) + tablero Kanban para el flujo. Se conservan los nombres de backlog «Elaboración» y «Construcción» por su utilidad organizativa, sin convertir el proceso en una copia técnica de ChuroViaje.

| GitHub Milestone (título exacto recomendado) | Inicio | Due date en GitHub | Objetivo y salida |
|---|---|---|---|
| Sprint 1 - Documentacion | Finalizado; fechas originales por confirmar | Dejar sin due date si no está confirmada | Documentos y avances ya realizados: no duplicar. |
| Sprint 2 - Arquitectura MVC | 2026-10-19 | **2026-10-30** | Arquitectura Java MVC, dominio, reglas de asignación, UI v1, API, WS y esquema BD. |
| Sprint 3 - Bases e identidad | 2026-11-02 | **2026-11-13** | Base Flutter/Java/PostgreSQL, identidad y pacientes. |
| Sprint 4 - Gestion de citas | 2026-11-16 | **2026-11-27** | Disponibilidad, asignación, solicitud y confirmación, cambio/cancelación y agendas. |
| Sprint 5 - Integracion y seguimiento | 2026-11-30 | **2026-12-11** | Reasignación, WebSocket, notificaciones, reportes, ambiente integrado. |
| Sprint 6 - Validacion y entrega | 2026-12-14 | **2026-12-23** | Pruebas finales, rendimiento, seguridad, respaldo y entrega (sprint corto, nueva propuesta). |

GitHub Milestones registra **fecha límite**, no campo de fecha de inicio; anotar el inicio en la descripción. Sprint 2–5 mantienen las fechas de dos semanas usadas en la planificación reciente. Sprint 6 es una **ampliación propuesta** para no confundir desarrollo con la validación y evitar dar por completa la implementación al final de Sprint 5.

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

> Inicio: por confirmar. Etapa: Elaboración documental (ya finalizada). Objetivo: Documentación, requisitos, investigación y CU-01 a CU-20: conservar issues previos; sin recrearlos. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `SIN FECHA: confirmar el cierre del Sprint 1`

### Sprint 2 - Arquitectura MVC

**Description:**

> Inicio: 2026-10-19. Etapa: Elaboración. Objetivo: Modelo de dominio, MVC, reglas de asignación y reasignación, diseño UI v1, BD, contratos REST/WebSocket y revisión. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-10-30`

### Sprint 3 - Bases e identidad

**Description:**

> Inicio: 2026-11-02. Etapa: Construcción. Objetivo: Estructuras Java/Flutter, migraciones, seguridad/roles, pacientes y pruebas base. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-13`

### Sprint 4 - Gestion de citas

**Description:**

> Inicio: 2026-11-16. Etapa: Construcción. Objetivo: Disponibilidad, asignación, consultas, confirmación, reprogramación, cancelación y agenda. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-11-27`

### Sprint 5 - Integracion y seguimiento

**Description:**

> Inicio: 2026-11-30. Etapa: Construcción. Objetivo: Reasignación, WebSocket, notificaciones, atención, historial, indicadores y despliegue de prueba. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-12-11`

### Sprint 6 - Validacion y entrega

**Description:**

> Inicio: 2026-12-14. Etapa: Pruebas/Transición. Objetivo: Pruebas de extremo a extremo, concurrencia, seguridad, respaldo, correcciones y entrega. Seguimiento mediante Issues, Pull Requests y trazabilidad RF-CU. Cerrar solo trabajos verificados.

**Due date:** `2026-12-23`
