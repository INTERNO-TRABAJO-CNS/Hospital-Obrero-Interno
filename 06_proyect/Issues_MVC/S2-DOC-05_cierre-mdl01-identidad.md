# [S2-DOC-05][Docs][CU-01/CU-02/CU-03] Cerrar MDL-01 (identidad, roles y pacientes): ordenar archivos y abrir el PR

| Campo | Valor |
|---|---|
| Milestone | Sprint 2 - Arquitectura MVC (vence 2026-10-30) |
| Responsable | Alejandro (`Alejandro5989`) |
| Revisor | Matías |
| Rama (desde `develop`, actualizada: `git merge develop`) | `feature/S2-M01-identidad-pacientes (ya existe en remoto)` |
| Labels | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| Puntos | 2 |
| Origen | issue propuesto `S2-M01` (esta tarjeta es su **cierre**, no un duplicado) |

## Objetivo
MDL-01 es uno de los dos submodelos con contenido real hecho por su dueño; falta ponerlo en su lugar y abrir el PR.

## Estado actual (medido en el repo el 2026-10-29)

- Tu `MDL-01_identidad_pacientes.md` (103 líneas, commit `4503c20`) quedó en la **raíz del repo**; debe ir en `03_des/02-arquitecturaGeneral/modelo/` (ya existe allí un borrador del integrador con el mismo tema: fusionar, no duplicar).
- Tu `.puml` quedó en `modelo/diagramas/` (carpeta que no está en el plan); va en `03_des/02-arquitecturaGeneral/diagramas/MDL-01_Identidad.puml`.
- La rama no tiene PR y está **3 commits detrás** de `develop`.

## Tareas

- [ ] Mover/fusionar los dos archivos a su ruta, `git merge develop` en la rama y abrir el PR a `develop`.
- [ ] Dejar escrita la tensión **CU-03 original (solo Enfermería/Admisión registra) vs. decisión DC-07 (el paciente puede crear su cuenta)**: no cambiar el CU, solo proponer el ajuste de trazabilidad.
- [ ] Agregar la sección «Clases MVC» por CU (tabla de abajo): de aquí salen las tarjetas S3-04…S3-09.
- [ ] Revisar el PR de Matías (S2-DOC-04) y pasarle a Alex los nombres exactos de tablas/columnas de identidad para S3-02 (salen del DER).

## Casos de uso de este módulo: qué hay que dejar escrito por cada uno

Para **cada** CU, en el `MDL-0X` agregar la sección **«Clases MVC»** con la tabla de abajo (nombres propuestos, ajustables) y el mapeo de cada flujo `[Na]` a un resultado.

### CU-01 — Iniciar sesión  (RF-01, Must)

> **HU-01: Como usuario quiero iniciar sesión con mi cuenta para acceder únicamente a las funciones correspondientes a mi rol.**  
> **CA:** Dado que el usuario posee credenciales registradas, cuando inicia sesión, entonces el sistema deberá identificar su rol y habilitar únicamente las funciones autorizadas.

- Diagrama: `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Rechaza el acceso e informa que no fue posible autenticar; `[3a]` Bloquea funciones no autorizadas

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | Usuario, Rol, Permiso, CredencialesDTO, SesionDTO, UsuarioRepository, HasherClave (interfaz), AuthService |
| Infrastructure (`infrastructure/pg`) | PgUsuarioRepository, BcryptHasher |
| Controller (`controller`) | AuthController  POST /api/v1/auth/login |

### CU-02 — Gestionar usuarios, roles y permisos  (RF-02, Must)

> **HU-02: Como administrador, quiero gestionar usuarios y roles para controlar quién puede utilizar cada función del sistema.**  
> **CA:** Dado que el administrador está autenticado, cuando registra o modifica el rol de un usuario, entonces el sistema deberá guardar el cambio y aplicar los permisos correspondientes.

- Diagrama: `03_des/01-diagramaCompor/CU-02_GestionarUsuariosRolesPermisos.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Impide el acceso a la administración; `[3a]` No guarda, solicita corregir información

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | Usuario, Rol, UsuarioDTO, RolDTO, UsuarioRepository, RolRepository, UsuarioService |
| Infrastructure (`infrastructure/pg`) | PgUsuarioRepository, PgRolRepository |
| Controller (`controller`) | UsuarioController  GET /api/v1/usuarios · PATCH /api/v1/usuarios/{id}/roles |

### CU-03 — Registrar y consultar pacientes  (RF-03, Must)

> **HU-03: Como personal de enfermería / admisión, quiero registrar o localizar al paciente para gestionar sus citas sin duplicar su información.**  
> **CA:** Dado que se dispone de la identificación del paciente, cuando se intenta registrarlo, entonces el sistema deberá verificar si ya existe y evitar la creación de un registro duplicado.

- Diagrama: `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Recupera y muestra el paciente existente, no duplica; `[3a]` No registra, solicita completar o corregir datos

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | Paciente, PacienteDTO, PacienteRepository, PacienteService |
| Infrastructure (`infrastructure/pg`) | PgPacienteRepository |
| Controller (`controller`) | PacienteController  GET/POST /api/v1/pacientes |

Rutas tomadas del borrador `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md` (ARQ-06, no aprobado). Si una decisión está marcada PENDIENTE (PV-xx), **proponer y marcar como propuesta**: no bloquear la entrega.

## Definition of Done

- [ ] `.md` y `.puml` del módulo completos, en la ruta correcta, y coherentes con el DER real.
- [ ] Sección «Clases MVC» por cada CU del módulo.
- [ ] PR a `develop` con la rama al día (`git merge develop` antes de abrir el PR), revisado y aprobado por otra persona.
- [ ] Entregado **antes del 2026-10-30**; si no se logra, se avisa hoy y se registra como arrastre al Sprint 3 (no se entrega callado fuera de fecha).

## Registro
Issue `#____` · PR `#____`
