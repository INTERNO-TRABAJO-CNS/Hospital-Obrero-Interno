# [S3-07][Java][CU-01] Autenticación: Usuario, Rol, AuthService y HasherClave (interfaz) con pruebas sin BD

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Matías (`mtiasgarcia1712`) |
| Revisor | Alejandro (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-07-cu01-auth-model` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:model` · `caso-uso:CU-01` |
| Puntos | 2 (de CON-03 = 5) |
| Depende de | S2-IMP-01 (molde) |
| Bloquea | S3-08, S3-09, S3-11 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
La **lógica** de iniciar sesión sin depender de Spring ni de BD. Ejemplo vivo de la regla clave del profe: la verificación de clave es una **interfaz del modelo** (`HasherClave`) y BCrypt vive afuera, en `infrastructure`.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-01 (Must) | HU-01 | CU-01 — Iniciar sesión | RNF-01 | CON-03 | `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-01: Como usuario quiero iniciar sesión con mi cuenta para acceder únicamente a las funciones correspondientes a mi rol.**  
> **CA (RF-01):** Dado que el usuario posee credenciales registradas, cuando inicia sesión, entonces el sistema deberá identificar su rol y habilitar únicamente las funciones autorizadas.

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-01 — Iniciar sesión** · fuente: `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `AuthService.autenticar(CredencialesDTO)`: busca el `Usuario`, `hasher.coincide(...)`, devuelve `SesionDTO` con rol y permisos (pasos «identifica el rol» y «aplica los permisos») | `AuthServiceTest.autentica_y_devuelve_rol_y_permisos` |
| `[2a]` Rechaza el acceso e informa que no fue posible autenticar | `CREDENCIALES_INVALIDAS` con **la misma respuesta** para usuario inexistente, clave errónea o usuario inactivo (no revelar cuál) | `usuario_inexistente`, `clave_erronea`, `usuario_inactivo` (CP-CNS-03B) |
| `[3a]` Bloquea funciones no autorizadas | `AuthService.tienePermiso(...)` falso → el Controller/Security responde 403 (ver S3-08) | `sin_permiso_devuelve_false` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// model/identidad/Rol.java        public class Rol { String nombre; Set<String> permisos; }   // PACIENTE | ENFERMERIA | MEDICO | ADMINISTRADOR
// model/identidad/Usuario.java
public class Usuario {
    public Usuario(int id, String nombreUsuario, String passwordHash, boolean activo, Rol rol)
    public boolean isValid()
    public boolean puedeIniciarSesion()          // activo
    public boolean tienePermiso(String permiso)
}
public record CredencialesDTO(String nombreUsuario, String clave) {}
public record SesionDTO(int usuarioId, String nombreUsuario, String rol, Set<String> permisos) {}
public record ResultadoAutenticacion(Estado estado, SesionDTO sesion) { public enum Estado { OK, CREDENCIALES_INVALIDAS } }
// model/identidad/HasherClave.java   (INTERFAZ en el modelo; BcryptHasher va en infrastructure)
public interface HasherClave { boolean coincide(String claveEnClaro, String hash); String hash(String claveEnClaro); }
// model/identidad/UsuarioRepository.java   (S3-09 agrega search)
public interface UsuarioRepository { int nextIdentity(); boolean store(Usuario u); Usuario getById(int id); Usuario getByNombreUsuario(String n); }
// model/identidad/AuthService.java
public class AuthService {
    public AuthService(UsuarioRepository repo, HasherClave hasher)
    public ResultadoAutenticacion autenticar(CredencialesDTO c)
    public boolean tienePermiso(int usuarioId, String permiso)
}
```

- Los permisos de cada rol salen de la semilla de S3-02; no se inventan permisos nuevos.
- Esta tarjeta es **independiente del mecanismo** (sesión vs token, PV-11): el Service solo devuelve `SesionDTO`.

## Tareas

- [ ] Escribir el contrato.
- [ ] `FakeUsuarioRepository` y `FakeHasher` en `src/test`.
- [ ] Pruebas: éxito (rol y permisos), usuario inexistente, clave errónea, usuario inactivo, y que **las tres fallas devuelven el mismo resultado**; `tienePermiso` positivo y negativo.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-03A` — Iniciar sesión con cuenta válida y comprobar las funciones autorizadas.
- [ ] `CP-CNS-03B` — Usar credenciales inválidas y tratar de consultar datos de otro rol; ambas operaciones deben rechazarse.

## Fuera de alcance (no hacer en esta tarjeta)

- BCrypt, JDBC, Spring Security y HTTP (S3-08).
- Recuperar contraseña, bloqueo por intentos: no están en el CU.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
