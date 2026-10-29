# [S3-08][Java][CU-01] Login por REST: PgUsuarioRepository, BcryptHasher, AuthController y Spring Security por rol

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Matías (`mtiasgarcia1712`) |
| Revisor | Alejandro (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-08-cu01-auth-infra-controller` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:infra` · `capa:controller` · `caso-uso:CU-01` |
| Puntos | 3 (de CON-03 = 5) |
| Depende de | S3-02 (usuarios semilla) · S3-07 · S3-01 (errores) |
| Bloquea | S3-06 (rol real), S3-09, S3-11 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Que se pueda iniciar sesión de verdad y que **cada ruta quede protegida por rol**. Reemplaza el `permitAll` temporal del perfil `dev` dejado en S2-IMP-01.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-01 (Must) | HU-01 | CU-01 — Iniciar sesión | RNF-01, RNF-05 | CON-03 | `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-01: Como usuario quiero iniciar sesión con mi cuenta para acceder únicamente a las funciones correspondientes a mi rol.**  
> **CA (RF-01):** Dado que el usuario posee credenciales registradas, cuando inicia sesión, entonces el sistema deberá identificar su rol y habilitar únicamente las funciones autorizadas.

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-05** — Dado que un usuario inicia sesión con un rol determinado, cuando accede a la aplicación, entonces deberá visualizar las opciones correspondientes a sus funciones sin mostrar operaciones que no le estén autorizadas.

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-01 — Iniciar sesión** · fuente: `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `AuthController.login` (`POST /api/v1/auth/login`) → 200 con `SesionDTO`; la credencial/sesión se maneja según la decisión de PV-11 | `AuthControllerTest.login_ok` · `AuthIT.login_con_usuario_semilla` (CP-CNS-03A) |
| `[2a]` Rechaza el acceso e informa que no fue posible autenticar | `CREDENCIALES_INVALIDAS` → 401 con `ErrorDTO` genérico | `login_invalido_401` (CP-CNS-03B) |
| `[3a]` Bloquea funciones no autorizadas | Spring Security: sin sesión → 401, con rol sin permiso → 403 («bloquea funciones no autorizadas») | `SeguridadIT.sin_sesion_401`, `SeguridadIT.rol_incorrecto_403` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// infrastructure/pg/PgUsuarioRepository.java   implements UsuarioRepository   (join usuario ⇄ rol ⇄ permiso)
// infrastructure/BcryptHasher.java             implements HasherClave           (BCryptPasswordEncoder por dentro)
// infrastructure/SecurityConfig.java           (SecurityFilterChain: /api/v1/auth/login público; resto autenticado; reglas por rol)
// controller/AuthController.java
@RestController @RequestMapping("/api/v1/auth")
class AuthController {
    AuthController(AuthService service)
    @PostMapping("/login") ResponseEntity<SesionDTO> login(@RequestBody CredencialesDTO c)    // 200 | 401
}
```

- **Decisión PV-11 (esquema de autenticación) sigue PENDIENTE y no hay JWT/OAuth aprobado.** El `pom` ya trae `starter-session-jdbc` (sesión en servidor). Propón en el PR, en 5 líneas, sesión vs token con justificación; el equipo aprueba y queda registrada en `11_decisiones_pendientes.md`. El Service de S3-07 no cambia sea cual sea.

## Tareas

- [ ] Implementar `PgUsuarioRepository` y `BcryptHasher`.
- [ ] Implementar `AuthController` y `SecurityConfig`; quitar el `permitAll` de `dev` (o dejarlo solo si el perfil lo pide explícitamente).
- [ ] Pruebas: MockMvc del login; IT con el usuario semilla de S3-02; acceso a una ruta protegida sin sesión (401) y con rol equivocado (403) — RNF-01.
- [ ] Las funciones visibles por rol (RNF-05) se resuelven en el cliente con `SesionDTO.permisos`, **pero el backend valida siempre** (ARQ-05).

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-03A` — Iniciar sesión con cuenta válida y comprobar las funciones autorizadas.
- [ ] `CP-CNS-03B` — Usar credenciales inválidas y tratar de consultar datos de otro rol; ambas operaciones deben rechazarse.

## Fuera de alcance (no hacer en esta tarjeta)

- Gestión de usuarios (S3-09).
- Recuperar contraseña, 2FA, bloqueo por intentos.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
