# [S3-12][Flutter][CU-03] Pantallas Flutter de búsqueda y registro de pacientes (Enfermería / Admisión)

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alex (usuario de GitHub por confirmar) |
| Revisor | Abigail (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-12-cu03-pacientes-flutter` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:flutter` · `caso-uso:CU-03` |
| Puntos | 3 (= CON-08) |
| Depende de | S3-10 · S3-11 (sesión) · (S3-06 para el backend real; antes `FakePacienteApi`) |
| Bloquea | Ninguna |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Que Admisión pueda localizar a un paciente por su número de asegurado y, si no existe, registrarlo; **sin crear duplicados**.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-03 (Must) | HU-03 | CU-03 — Registrar y consultar pacientes | RNF-02, RNF-05 | CON-08 | `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-03: Como personal de enfermería / admisión, quiero registrar o localizar al paciente para gestionar sus citas sin duplicar su información.**  
> **CA (RF-03):** Dado que se dispone de la identificación del paciente, cuando se intenta registrarlo, entonces el sistema deberá verificar si ya existe y evitar la creación de un registro duplicado.

**RNF aplicables:**

- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente
- **RNF-05** — Dado que un usuario inicia sesión con un rol determinado, cuando accede a la aplicación, entonces deberá visualizar las opciones correspondientes a sus funciones sin mostrar operaciones que no le estén autorizadas.

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-03 — Registrar y consultar pacientes** · fuente: `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `PacienteBusquedaView` → `PacienteController.buscar/registrar` → `PacienteApi` → DTO; formulario de registro cuando no hay resultado | `paciente_flow_test.busca_y_registra` |
| `[1a]` Recupera y muestra el paciente existente, no duplica | Si el backend responde con el paciente existente (200), la app abre su ficha y **no** muestra «registrado nuevo» | `paciente_flow_test.existente_abre_ficha` (CP-CNS-08A) |
| `[3a]` No registra, solicita completar o corregir datos | Validación de formato antes de enviar + mensaje del 422 del backend; no se genera alta | `paciente_form_test.campos_faltantes` (CP-CNS-08B) |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```dart
// lib/models/paciente_dto.dart               espejo de PacienteDTO
// lib/services/paciente_api.dart             abstract class PacienteApi { Future<List<PacienteDto>> buscar(String filtro); Future<ResultadoRegistro> registrar(PacienteDto p); }  + Rest… + Fake…
// lib/controllers/paciente_controller.dart   ChangeNotifier
// lib/views/paciente_busqueda_view.dart · paciente_form_view.dart · paciente_ficha_view.dart
```

## Tareas

- [ ] Implementar `PacienteApi` (Rest + Fake), controller y las tres vistas según `03_des/diseñoUI/` y `09_interfaz_flutter_v1.md`.
- [ ] Mostrar el formulario solo a roles ENFERMERIA/ADMINISTRADOR (la regla real la aplica el backend).
- [ ] Widget tests con `FakePacienteApi`; prueba manual contra el backend y captura en el PR.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-08A` — Buscar paciente existente abre su ficha sin crear una nueva.
- [ ] `CP-CNS-08B` — Registrar paciente con campos faltantes muestra validación y no genera alta.

## Fuera de alcance (no hacer en esta tarjeta)

- Auto-registro del paciente desde su propia app (DC-07, pendiente).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
