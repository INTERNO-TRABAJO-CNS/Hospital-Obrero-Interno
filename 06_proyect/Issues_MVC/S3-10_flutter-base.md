# [S3-10][Flutter] Estructura base de la app Flutter (models · services · controllers · views) y cliente REST

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Abigail (`AbigailFarfan`) |
| Revisor | Alex (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-10-flutter-base` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:flutter` |
| Puntos | 5 (= CON-04) |
| Depende de | S2-DOC-03 (diseños UI y mapa de vistas) |
| Bloquea | S3-11, S3-12 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Dejar el proyecto Flutter con la **misma disciplina de capas** que el backend, para que las pantallas de S3-11 y S3-12 no mezclen HTTP con widgets. Hoy `04_imp/frontend` está vacío.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| — | — | transversal | RNF-05 | CON-04 | — |

**RNF aplicables:**

- **RNF-05** — Dado que un usuario inicia sesión con un rol determinado, cuando accede a la aplicación, entonces deberá visualizar las opciones correspondientes a sus funciones sin mostrar operaciones que no le estén autorizadas.

## Criterios de aceptación de esta tarjeta

- [ ] Proyecto Flutter creado en `04_imp/frontend/` con `lib/models`, `lib/services`, `lib/controllers`, `lib/views`.
- [ ] `models/` = DTO con `fromJson/toJson` (espejo exacto de los DTO Java), sin lógica de pantalla.
- [ ] `services/ApiClient`: URL base configurable (`--dart-define`), timeout, parseo de `ErrorDTO` y errores de red controlados (el backend caído **no** cierra la app: CP-CNS-04B).
- [ ] `controllers/` = estado de cada pantalla (`ChangeNotifier`, o lo que el equipo decida) + validación **de formato**; las reglas de negocio siguen en Java.
- [ ] `views/` = widgets; **ninguna** llamada HTTP directa desde un widget.
- [ ] Cada `*Api` tiene una versión `Fake*Api` para trabajar sin backend (contrato = el de los DTO de ARQ-06).

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```dart
// lib/models/sesion_dto.dart      class SesionDto { int usuarioId; String nombreUsuario; String rol; List<String> permisos; factory SesionDto.fromJson(Map<String, dynamic> j) }
// lib/models/error_dto.dart       class ErrorDto { String codigo; String mensaje; }
// lib/services/api_client.dart    class ApiClient { Future<Map<String, dynamic>> post(String path, Map<String, dynamic> body); Future<dynamic> get(String path); }   // lanza ApiException(ErrorDto | red)
// lib/services/auth_api.dart      abstract class AuthApi { Future<SesionDto> login(String usuario, String clave); }   +   RestAuthApi  +  FakeAuthApi
// lib/controllers/…               un ChangeNotifier por pantalla
// lib/views/…                     widgets
```

- La librería de estado (Provider/Riverpod/Bloc) **no está decidida**: proponer una en el PR con una línea de justificación y registrarla.
- Qué roles usan Flutter es PV-01 (pendiente): empezar con **una sola app** que muestra opciones según `SesionDto.permisos` (RNF-05).

## Tareas

- [ ] `flutter create` y estructura de carpetas.
- [ ] `ApiClient` + `ErrorDto` + manejo de errores de red.
- [ ] Un `FakeAuthApi` y un widget test que lo use.
- [ ] README de `04_imp/frontend` (versión de Flutter, cómo apuntar al backend).
- [ ] Agregar el job `flutter test` al CI (S3-03).

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-04A` — Compilar y ejecutar pruebas de widgets desde una instalación limpia.
- [ ] `CP-CNS-04B` — Simular backend no disponible y confirmar que la app muestra un error controlado sin cerrarse.
- [ ] Widget test: backend no disponible → mensaje de error, sin cierre (CP-CNS-04B).

## Fuera de alcance (no hacer en esta tarjeta)

- Pantallas de negocio (S3-11, S3-12).
- WebSocket (Sprint 5).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
