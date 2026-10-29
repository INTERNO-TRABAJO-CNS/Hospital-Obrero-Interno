# [S3-06][Java][CU-03] PacienteController: registrar y buscar pacientes por REST con control de rol

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alejandro (`Alejandro5989`) |
| Revisor | Matías (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-06-cu03-paciente-controller` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:controller` · `caso-uso:CU-03` |
| Puntos | 1 (de CON-06 = 5) |
| Depende de | S3-04 · S3-01 (errores uniformes) |
| Bloquea | S3-12 (pantallas Flutter de pacientes) |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Exponer el caso de uso por REST sin meter lógica: el Controller solo traduce DTO ⇄ HTTP y aplica el control de rol (ENFERMERIA/administración).

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-03 (Must) | HU-03 | CU-03 — Registrar y consultar pacientes | RNF-01, RNF-02 | CON-06 | `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-03: Como personal de enfermería / admisión, quiero registrar o localizar al paciente para gestionar sus citas sin duplicar su información.**  
> **CA (RF-03):** Dado que se dispone de la identificación del paciente, cuando se intenta registrarlo, entonces el sistema deberá verificar si ya existe y evitar la creación de un registro duplicado.

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-03 — Registrar y consultar pacientes** · fuente: `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `PacienteController.registrar` → `201` con `PacienteDTO` | `PacienteControllerTest.post_nuevo_201` |
| `[1a]` Recupera y muestra el paciente existente, no duplica | `YA_EXISTIA` → **200** con el paciente existente (el CU manda mostrar el existente, no fallar). El borrador ARQ-06 hablaba de 409: dejar la decisión escrita en el PR | `post_existente_200_sin_duplicar` (CP-CNS-06A) |
| `[3a]` No registra, solicita completar o corregir datos | `DATOS_INVALIDOS` → **422** con `ErrorDTO` (sin eco de datos personales) | `post_invalido_422` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// controller/PacienteController.java
@RestController @RequestMapping("/api/v1/pacientes")
class PacienteController {
    PacienteController(PacienteService service)
    @PostMapping ResponseEntity<PacienteDTO> registrar(@RequestBody PacienteDTO datos)                   // 201 | 200 | 422
    @GetMapping  List<PacienteDTO> buscar(@RequestParam(required = false) String numeroAsegurado,
                                          @RequestParam(required = false) String filtro)                 // 200 (lista vacía también es 200)
}
```

## Tareas

- [ ] Implementar el Controller (sin SQL, sin reglas, sin crear entidades).
- [ ] Restringir a roles ENFERMERIA y ADMINISTRADOR: sin sesión → 401, otro rol → 403 (RNF-01). Mientras S3-08 no esté, probar con `@WithMockUser`.
- [ ] Pruebas MockMvc con `PacienteService` simulado: 201, 200, 422, 401, 403 y búsqueda.
- [ ] Documentar en ARQ-06 los códigos reales.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-06A` — Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes.
- [ ] `CP-CNS-06B` — Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente.
- [ ] Un paciente no puede consultar pacientes ajenos (RNF-02): `GET /pacientes` con rol PACIENTE → 403.

## Fuera de alcance (no hacer en esta tarjeta)

- Endpoint de **auto-registro** del paciente (DC-07 / ARQ-06 `POST /pacientes/autoregistro`): pendiente de trazabilidad y de verificación de afiliación (PV-07).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
