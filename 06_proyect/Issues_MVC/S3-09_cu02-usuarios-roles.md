# [S3-09][Java][CU-02] Gestionar usuarios y roles (slice completo): UsuarioService, Pg y UsuarioController solo para ADMINISTRADOR

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Matías (`mtiasgarcia1712`) |
| Revisor | Alejandro (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-09-cu02-usuarios-roles` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:model` · `capa:infra` · `capa:controller` · `caso-uso:CU-02` |
| Puntos | 3 (= CON-05) |
| Depende de | S3-07 · S3-08 (rol ADMINISTRADOR real) |
| Bloquea | S3-13 (pruebas de aceptación de CU-02) |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
El administrador crea usuarios y cambia su rol; el cambio se refleja en el siguiente inicio de sesión. Es el primer slice que Matías hace **completo** (las tres capas), siguiendo el patrón de S3-04/05/06.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-02 (Must) | HU-02 | CU-02 — Gestionar usuarios, roles y permisos | RNF-01, RNF-05 | CON-05 | `03_des/01-diagramaCompor/CU-02_GestionarUsuariosRolesPermisos.puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-02: Como administrador, quiero gestionar usuarios y roles para controlar quién puede utilizar cada función del sistema.**  
> **CA (RF-02):** Dado que el administrador está autenticado, cuando registra o modifica el rol de un usuario, entonces el sistema deberá guardar el cambio y aplicar los permisos correspondientes.

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-05** — Dado que un usuario inicia sesión con un rol determinado, cuando accede a la aplicación, entonces deberá visualizar las opciones correspondientes a sus funciones sin mostrar operaciones que no le estén autorizadas.

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-02 — Gestionar usuarios, roles y permisos** · fuente: `03_des/01-diagramaCompor/CU-02_GestionarUsuariosRolesPermisos.puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `UsuarioService.crear` / `asignarRol` guardan y los permisos efectivos salen del rol: el siguiente `login` ya devuelve el rol nuevo | `UsuarioServiceTest.asignar_rol_ok` · `UsuarioIT.rol_nuevo_se_refleja_en_login` (CP-CNS-05A) |
| `[1a]` Impide el acceso a la administración | Actor sin rol ADMINISTRADOR → `NO_AUTORIZADO` → 403 (verificar en el Service **y** en Security) | `no_admin_no_puede` (CP-CNS-05B) |
| `[3a]` No guarda, solicita corregir información | Rol o usuario inexistente / datos inválidos → resultado de error → 422; **no guarda nada** | `rol_invalido_no_modifica` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
public record UsuarioDTO(int id, String nombreUsuario, String rol, boolean activo) {}
public record NuevoUsuarioDTO(String nombreUsuario, String claveInicial, String rol) {}
public record ResultadoGestion(Estado estado) { public enum Estado { OK, NO_AUTORIZADO, USUARIO_INEXISTENTE, ROL_INVALIDO, YA_EXISTE, DATOS_INVALIDOS } }
// model/identidad/RolRepository.java      Rol getByNombre(String n);  List<Rol> search();
// UsuarioRepository (de S3-07) agrega:    List<UsuarioDTO> search(String filtro);   boolean updateRol(int usuarioId, int rolId)  (transacción)
// model/identidad/UsuarioService.java
public class UsuarioService {
    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, HasherClave hasher)
    public List<UsuarioDTO> search(String filtro, SesionDTO actor)
    public ResultadoGestion crear(NuevoUsuarioDTO datos, SesionDTO actor)
    public ResultadoGestion asignarRol(int usuarioId, String rol, SesionDTO actor)
}
// infrastructure/pg/PgRolRepository.java  · infrastructure/pg/PgUsuarioRepository.java (amplía)
// controller/UsuarioController.java       GET /api/v1/usuarios · POST /api/v1/usuarios · PATCH /api/v1/usuarios/{id}/roles   (solo ADMINISTRADOR)
```

- **Alcance Sprint 3:** crear usuario y asignar rol. Los **permisos de cada rol** vienen de la semilla de S3-02; editarlos desde la UI queda como decisión pendiente (CU-02 dice «roles y permisos»: confirmar con el docente/equipo).
- La clave inicial se guarda con `hasher.hash(...)`; nunca se devuelve en ningún DTO.

## Tareas

- [ ] Service + `FakeRolRepository` + pruebas unitarias.
- [ ] `PgRolRepository` y ampliación de `PgUsuarioRepository` con prueba de integración.
- [ ] `UsuarioController` con MockMvc: 200, 403, 422 (RNF-01).
- [ ] Prueba extremo a extremo: administrador cambia el rol → nuevo login devuelve el rol nuevo.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-05A` — Administrador modifica el rol de un usuario; las funciones visibles y permisos efectivos se actualizan.
- [ ] `CP-CNS-05B` — Paciente intenta cambiar su rol y el backend rechaza sin modificar nada.

## Fuera de alcance (no hacer en esta tarjeta)

- Edición de permisos por rol; cambio de contraseña; baja lógica de usuarios.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
