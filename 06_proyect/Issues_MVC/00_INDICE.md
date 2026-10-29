# 00 · Índice — Issues MVC para cerrar el Sprint 2 y arrancar el Sprint 3

> Generado el 2026-10-29 a partir del estado real del repo (`develop` = `bdb5c23`). Cada tarjeta trae CU, RF/HU, criterio de aceptación **copiado de la matriz**, el diagrama de comportamiento con cada flujo `[Na]` mapeado a una capa/clase/prueba, y el contrato de clases según la pizarra del profe (`01_GUIA_MVC.md`).

## Cómo usar este paquete

1. Leer `01_GUIA_MVC.md` (10 min) — todo el equipo, hoy.
2. Crear en GitHub un issue por cada archivo `S2-…` / `S3-…` (título = primera línea, cuerpo = el resto), con Milestone, Assignee y etiquetas. **No recrear** #28, #29 ni #10 (ya existen y están cerrados) ni los `S2-M0x` propuestos: las tarjetas `S2-DOC-0x` son su cierre.
3. Mover todas al tablero en «Backlog» y subir a «Por hacer» solo las de la semana 1 (límite: 2 en progreso por persona).
4. Anotar el `#` real y la rama en `06_proyect/TRAZABILIDAD.md` al crear cada issue. Los códigos `S2-/S3-` son internos de este paquete, no números de GitHub.

## Tarjetas

| Tarjeta | Qué | Responsable | Revisor | CU | Pts | Depende de |
|---|---|---|---|---|---:|---|
| [S2-IMP-01](S2-IMP-01_esqueleto-mvc-y-molde.md) | Esqueleto MVC del backend + caso de uso «molde»: listar especialidades | Benjamín | Alejandro | CU-04 | 3 | Ninguna |
| [S2-DOC-02](S2-DOC-02_cierre-mdl02-recursos-der.md) | Cerrar MDL-02 (recursos y agenda) y subir el DER real al repo | Alex | Abigail | CU-06, CU-13, CU-14 | 3 | Ninguna |
| [S2-DOC-03](S2-DOC-03_cierre-mdl05-consultas-ui.md) | Cerrar MDL-05 (consultas, avisos, reportes) y subir los diseños UI al repo | Abigail | Alex | CU-08, CU-09, CU-16, CU-17, CU-19, CU-20 | 3 | Ninguna |
| [S2-DOC-04](S2-DOC-04_cierre-mdl04-operaciones.md) | Cerrar MDL-04 (llegada, reprogramación, cancelación, atención, historial) | Matías | Alejandro | CU-10, CU-11, CU-12, CU-15, CU-18 | 2 | Ninguna |
| [S2-DOC-05](S2-DOC-05_cierre-mdl01-identidad.md) | Cerrar MDL-01 (identidad, roles y pacientes): ordenar archivos y abrir el PR | Alejandro | Matías | CU-01, CU-02, CU-03 | 2 | Ninguna |
| [S3-01](S3-01_base-transversal-backend.md) | Base transversal del backend: perfiles, errores uniformes y logging | Benjamín | Matías | transversal | 2 | S2-IMP-01 |
| [S3-02](S3-02_postgresql-identidad.md) | Esquema PostgreSQL de identidad con migraciones Flyway y datos semilla | Alex | Alejandro | transversal | 5 | S2-IMP-01 (Flyway + docker-compose) · S2-DOC-02 (el DER real en el repo) |
| [S3-03](S3-03_ci-backend.md) | Integración continua: compilar y probar el backend en cada PR | Alejandro | Alex | transversal | 3 | S2-IMP-01 |
| [S3-04](S3-04_cu03-paciente-model.md) | Paciente: entidad, DTO, Repository (interfaz) y Service con pruebas sin BD | Alejandro | Matías | CU-03 | 2 | S2-IMP-01 (molde) |
| [S3-05](S3-05_cu03-paciente-infra.md) | PgPacienteRepository: persistencia JDBC de pacientes con unicidad garantizada en BD | Alejandro | Matías | CU-03 | 2 | S3-02 (tablas) · S3-04 (interfaz) |
| [S3-06](S3-06_cu03-paciente-controller.md) | PacienteController: registrar y buscar pacientes por REST con control de rol | Alejandro | Matías | CU-03 | 1 | S3-04 · S3-01 (errores uniformes) |
| [S3-07](S3-07_cu01-auth-model.md) | Autenticación: Usuario, Rol, AuthService y HasherClave (interfaz) con pruebas sin BD | Matías | Alejandro | CU-01 | 2 | S2-IMP-01 (molde) |
| [S3-08](S3-08_cu01-auth-infra-controller.md) | Login por REST: PgUsuarioRepository, BcryptHasher, AuthController y Spring Security por rol | Matías | Alejandro | CU-01 | 3 | S3-02 (usuarios semilla) · S3-07 · S3-01 (errores) |
| [S3-09](S3-09_cu02-usuarios-roles.md) | Gestionar usuarios y roles (slice completo): UsuarioService, Pg y UsuarioController solo para ADMINISTRADOR | Matías | Alejandro | CU-02 | 3 | S3-07 · S3-08 (rol ADMINISTRADOR real) |
| [S3-10](S3-10_flutter-base.md) | Estructura base de la app Flutter (models · services · controllers · views) y cliente REST | Abigail | Alex | transversal | 5 | S2-DOC-03 (diseños UI y mapa de vistas) |
| [S3-11](S3-11_cu01-login-flutter.md) | Pantalla de inicio de sesión y sesión en Flutter | Abigail | Alex | CU-01 | 3 | S3-10 · (S3-08 para probar contra el backend real; antes usar `FakeAuthApi`) |
| [S3-12](S3-12_cu03-pacientes-flutter.md) | Pantallas Flutter de búsqueda y registro de pacientes (Enfermería / Admisión) | Alex | Abigail | CU-03 | 3 | S3-10 · S3-11 (sesión) · (S3-06 para el backend real; antes `FakePacienteApi`) |
| [S3-13](S3-13_plan-pruebas-v.md) | Plan de pruebas del modelo en V y casos de aceptación de CU-01, CU-02 y CU-03 | Benjamín | Abigail | transversal | 2 | Ninguna (se puede hacer ya) |

## Carga por persona

| Persona | Sprint 2 (cierre) | Sprint 3 | Tarjetas |
|---|---:|---:|---|
| Alejandro | 2 | 8 | S2-DOC-05, S3-03, S3-04, S3-05, S3-06 |
| Alex | 3 | 8 | S2-DOC-02, S3-02, S3-12 |
| Abigail | 3 | 8 | S2-DOC-03, S3-10, S3-11 |
| Matías | 2 | 8 | S2-DOC-04, S3-07, S3-08, S3-09 |
| Benjamín | 3 | 4 | S2-IMP-01, S3-01, S3-13 |

Se respetó el reparto del backlog existente (`07_Estimacion.md`, ~8 pts por persona en Sprint 3). Benjamín muestra menos puntos porque además integra los submodelos (S2-M03/S2-M06), mantiene `develop` y es revisor secundario. Único cambio: **CON-09 (CI, 3 pts) pasa de Benjamín a Alejandro**, que tenía la carga más baja (5 pts), para que Benjamín —integrador, revisor y autor del molde— deje de ser el cuello de botella.

## Calendario sugerido del Sprint 3 (2 → 13 de noviembre)

| Semana | Tarjetas | Resultado |
|---|---|---|
| Antes del 2/11 | **S2-IMP-01** y las 4 `S2-DOC-0x` | Molde MVC en `develop`; DER en el repo; submodelos cerrados |
| 1 (2–6 nov) | S3-01, S3-02, S3-03, S3-04, S3-07, S3-10, S3-13 | Cada persona con una tarjeta propia que **no** depende de otra; backend con BD y errores; modelo de pacientes y auth probados sin BD |
| 2 (9–13 nov) | S3-05, S3-06, S3-08, S3-09, S3-11, S3-12 + ejecución de aceptación | Slices completos; **demo el viernes 13**: login → admin crea usuario/rol → Admisión busca y registra paciente (y no duplica) |

## Reglas de revisión (rotación, para que no revise todo una sola persona)

| Autor | Lo revisa |
|---|---|
| Benjamín | Alejandro — S2-IMP-01 |
| Alex | Abigail — S2-DOC-02, S3-12 |
| Abigail | Alex — S2-DOC-03, S3-10, S3-11 |
| Matías | Alejandro — S2-DOC-04, S3-07, S3-08, S3-09 |
| Alejandro | Matías — S2-DOC-05, S3-04, S3-05, S3-06 |
| Benjamín | Matías — S3-01 |
| Alex | Alejandro — S3-02 |
| Alejandro | Alex — S3-03 |
| Benjamín | Abigail — S3-13 |

Nadie mergea su propio PR. Benjamín queda como revisor secundario opcional y como integrador de `develop` → `main` al cierre de cada sprint.

## Sprint 4 (16–27 nov): se generan con la misma plantilla cuando Sprint 3 llegue al 70 %

| Backlog | Qué | Responsable (plan actual) | CU |
|---|---|---|---|
| CON-10 | Catálogos de médicos, especialidades, recursos y agendas | Alex | CU-05/06/13 |
| CON-11 | Disponibilidad y no doble reserva | Benjamín | CU-06 |
| CON-12 | Motor de asignación automática (Java) | Benjamín | CU-05 |
| CON-13 | Solicitud y confirmación de citas (transacción) | Alejandro | CU-04/07 |
| CON-14 | Consulta de citas del paciente | Alejandro | CU-08 |
| CON-15 | Consulta de citas para admisión | Matías | CU-09 |
| CON-16 | Reprogramación y cancelación | Matías | CU-10/11 |
| CON-17 / 18 | Pantallas Flutter de paciente y de admisión | Abigail | CU-04/08/09/10/11 |
| CON-19 | Agenda médica y estado de la cita | Alex | CU-12/13 |
| CON-20 | Pruebas integradas | Benjamín | CU-04…11 |

Aviso de carga: Benjamín tiene **15 pts** en Sprint 4 (CON-11, 12, 20), el máximo del equipo. Decidirlo en la planning del 2 de noviembre, no a mitad del sprint.

## Congelado hasta que CU-01…CU-07 funcionen de punta a punta

- **Reasignación de cupo** (CON-21, `S2-M07`, `ARQ-06_SecuenciaReasignacion_PENDIENTE`): no está aprobada como requisito (`11_decisiones_pendientes.md`, DC-11/PV-12) y consume planificación que el núcleo necesita.
- Cualquier tecnología nueva (WebFlux, OpenAPI, Testcontainers…) sin decisión escrita en el PR.

## Limpieza del repo antes de mostrarlo al docente

1. Quitar restos de prompts: los 7 `S2-M0x` dicen «Lectura obligatoria antes de usar IA»; `PLAN_MILESTONES.md` y `Backlog_Construccion/README.md` nombran «ChuroViaje».
2. Enlaces rotos: `CONTEXTO_COMPARTIDO.md` y `MAPA_INTERFACES.md` están referenciados y **no existen**.
3. Diagramas duplicados: `03_des/diagramaCU` y `03_des/01-diagramaCompor` (8 archivos con sufijo ` (1)`). Dejar una sola versión por CU.
4. Ramas con caracteres inválidos o typos (`feature/S2-M03]Arquitectura-MVC]-…`, `feature/T01-documentos,-revison,-analicis`, `feature/S3-backend-Inicla`): no crear más así; borrar las ya mergeadas.
5. `develop` y `main` sin protección real: activar PR obligatorio + check de CI (S3-03).

## Dudas que hay que cerrar esta semana (ninguna bloquea empezar)

- **JDBC (como en clase) vs JPA:** el `pom` trae `starter-data-jpa` y `ARQ-02` habla de JPA; la pizarra del profe usa `Repository` + JDBC. Este paquete asume **JDBC**. Si el docente acepta JPA, cambia solo la capa `infrastructure/pg`; el resto de las tarjetas no se toca.
- **PV-11:** esquema de autenticación (sesión vs token). Se decide dentro de S3-08.
- **DER:** ¿dónde está? Si no existe, S3-02 no puede empezar.
- **Librería de estado en Flutter:** se decide dentro de S3-10.
