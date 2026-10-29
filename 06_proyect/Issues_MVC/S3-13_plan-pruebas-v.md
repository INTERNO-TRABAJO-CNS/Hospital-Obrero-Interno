# [S3-13][Pruebas] Plan de pruebas del modelo en V y casos de aceptación de CU-01, CU-02 y CU-03

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Benjamín (usuario de GitHub por confirmar) |
| Revisor | Abigail (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-13-plan-pruebas-v` |
| Labels | `fase:construccion` · `tipo:prueba` · `tec:pruebas` |
| Puntos | 2 (parte de CON-36 adelantada) |
| Depende de | Ninguna (se puede hacer ya) |
| Bloquea | Ejecución de aceptación al cerrar S3-04…S3-12 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
`05_pro/` hoy es solo un README: **el lado derecho del V está vacío** y `TRAZABILIDAD.md` dice «CP por definir» en los 20 CU. Esta tarjeta lo arranca con lo mínimo útil: cada criterio de aceptación «Dado/Cuando/Entonces» de CU-01 a CU-03 se convierte en un caso de prueba ejecutable por **otra persona**.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| — | — | transversal | RNF-01, RNF-02 | CON-36 (adelanto) | — |

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente

## Criterios de aceptación de esta tarjeta

- [ ] `05_pro/PLAN_PRUEBAS.md` con la tabla V de la guía (requisitos↔aceptación · arquitectura↔integración · diseño↔unitarias) y el criterio de entrada/salida de cada nivel.
- [ ] `05_pro/plantilla_caso_prueba.md` (ID, CU/HU/CA de origen, precondiciones, pasos, resultado esperado, resultado real, evidencia, ejecutor).
- [ ] `05_pro/CP_CU-01-02-03_aceptacion.md` con los casos de CU-01, CU-02 y CU-03 escritos desde su CA y sus flujos alternativos, **reutilizando los códigos `CP-CNS-xx` del catálogo existente** (no inventar numeración nueva).
- [ ] Regla de independencia en el plan: **quien implementa no ejecuta la aceptación** — CU-01 (implementó Matías) lo ejecuta Alejandro · CU-02 (Matías) lo ejecuta Alex · CU-03 (Alejandro) lo ejecuta Matías.
- [ ] `06_proyect/TRAZABILIDAD.md`: columna «Casos de prueba» de CU-01…03 con los CP reales.

## Tareas

- [ ] Escribir plan, plantilla y los casos de CU-01…03.
- [ ] Actualizar `TRAZABILIDAD.md`.
- [ ] Cuando S3-04…S3-12 cierren, cada ejecutor completa resultado real y evidencia en el mismo archivo (PR aparte).

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-03A` — Iniciar sesión con cuenta válida y comprobar las funciones autorizadas.
- [ ] `CP-CNS-03B` — Usar credenciales inválidas y tratar de consultar datos de otro rol; ambas operaciones deben rechazarse.
- [ ] `CP-CNS-05A` — Administrador modifica el rol de un usuario; las funciones visibles y permisos efectivos se actualizan.
- [ ] `CP-CNS-05B` — Paciente intenta cambiar su rol y el backend rechaza sin modificar nada.
- [ ] `CP-CNS-06A` — Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes.
- [ ] `CP-CNS-06B` — Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente.

## Fuera de alcance (no hacer en esta tarjeta)

- Los casos de CU-04…CU-20: se generan sprint a sprint con la misma plantilla.
- Pruebas de rendimiento/concurrencia a escala (CON-32, Sprint 6).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
