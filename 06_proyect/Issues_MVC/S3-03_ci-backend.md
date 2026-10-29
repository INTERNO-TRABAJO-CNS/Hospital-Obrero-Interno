# [S3-03][Pruebas] Integración continua: compilar y probar el backend en cada PR

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alejandro (`Alejandro5989`) |
| Revisor | Alex (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-03-ci-backend` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:pruebas` |
| Puntos | 3 (= CON-09; se movió de Benjamín a Alejandro para equilibrar carga) |
| Depende de | S2-IMP-01 |
| Bloquea | Ninguna (pero desde aquí un PR con tests rotos no se puede mergear) |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Que el lado derecho del V sea automático: cada PR a `develop` compila y corre las pruebas; si fallan, el check queda rojo.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| — | — | transversal | — | CON-09 | — |

## Criterios de aceptación de esta tarjeta

- [ ] Workflow `.github/workflows/backend-ci.yml`: en `pull_request` hacia `develop` y `push` a `develop`, JDK 21, `./mvnw -B verify` en `04_imp/backend`.
- [ ] Las pruebas de integración corren con un servicio PostgreSQL del workflow (si S2-IMP-01 ya las dejó).
- [ ] Plantilla de PR (`.github/pull_request_template.md`) con la **checklist del revisor** de `01_GUIA_MVC.md`.
- [ ] Ajuste manual en GitHub (lo hace quien administra el repo): proteger `develop` y `main` (PR obligatorio + check obligatorio).

## Tareas

- [ ] Escribir el workflow y la plantilla de PR.
- [ ] Provocar un test que falle en un PR temporal y comprobar el check rojo (CP-CNS-09A); luego uno verde (CP-CNS-09B).
- [ ] Cuando S3-10 deje el proyecto Flutter, agregar el job `flutter test` en una tarjeta aparte.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-09A` — Crear PR temporal con test que falla y comprobar que el check queda fallido.
- [ ] `CP-CNS-09B` — Ejecutar pipeline exitoso con ejemplos JUnit y widget test.

## Fuera de alcance (no hacer en esta tarjeta)

- Despliegue (CON-30).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
