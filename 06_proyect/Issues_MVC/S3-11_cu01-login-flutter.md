# [S3-11][Flutter][CU-01] Pantalla de inicio de sesión y sesión en Flutter

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Abigail (`AbigailFarfan`) |
| Revisor | Alex (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-11-cu01-login-flutter` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:flutter` · `caso-uso:CU-01` |
| Puntos | 3 (= CON-07) |
| Depende de | S3-10 · (S3-08 para probar contra el backend real; antes usar `FakeAuthApi`) |
| Bloquea | S3-12 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
El paciente/personal ingresa sus credenciales y la app muestra **solo las opciones que su rol permite**.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-01 (Must) | HU-01 | CU-01 — Iniciar sesión | RNF-01, RNF-05 | CON-07 | `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml` |

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
| Camino principal | `LoginView` → `LoginController.iniciarSesion()` → `AuthApi.login` → guarda `SesionDto` y navega al menú del rol (carriles «Aplicación móvil» y «Usuario autorizado») | `login_view_test.ingresa_y_muestra_menu_del_rol` |
| `[2a]` Rechaza el acceso e informa que no fue posible autenticar | Error 401 → mensaje «no fue posible autenticar», sesión cerrada, opciones protegidas ocultas | `login_view_test.credenciales_rechazadas` (CP-CNS-07B) |
| `[3a]` Bloquea funciones no autorizadas | El menú se arma con `SesionDto.permisos`: un médico no ve pantallas de administración | `menu_test.medico_solo_ve_sus_pantallas` (CP-CNS-07A) |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```dart
// lib/controllers/login_controller.dart   class LoginController extends ChangeNotifier { Future<void> iniciarSesion(String usuario, String clave); SesionDto? sesion; String? error; bool cargando; }
// lib/views/login_view.dart               pantalla con usuario/clave/botón; consume LoginController
// lib/views/menu_view.dart                opciones según sesion.permisos
```

## Tareas

- [ ] Implementar `LoginView`, `LoginController` y menú por rol.
- [ ] Guardar la sesión según el mecanismo que decida S3-08 (PV-11); mientras tanto, en memoria.
- [ ] Widget tests con `FakeAuthApi`; luego una prueba manual contra el backend real y captura en el PR.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-07A` — Ingreso de médico muestra solo pantallas permitidas para ese rol.
- [ ] `CP-CNS-07B` — Credenciales rechazadas mantienen la sesión cerrada y las opciones protegidas ocultas.

## Fuera de alcance (no hacer en esta tarjeta)

- Registro de usuarios (CU-02) en pantalla.
- Recordar sesión entre arranques.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
