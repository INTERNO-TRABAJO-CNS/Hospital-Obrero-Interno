# [S3-01][Java] Base transversal del backend: perfiles, errores uniformes y logging

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Benjamín (usuario de GitHub por confirmar) |
| Revisor | Matías (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-01-base-transversal` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` |
| Puntos | 2 (segunda mitad de CON-01 = 5) |
| Depende de | S2-IMP-01 |
| Bloquea | S3-06, S3-08, S3-09 (usan el manejo de errores) |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Que todas las capas hablen el mismo idioma de errores: el Service lanza/retorna errores **de dominio** (sin saber de HTTP) y un único `@RestControllerAdvice` los traduce a códigos HTTP con un cuerpo uniforme y sin datos personales.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| — | — | transversal | RNF-01, RNF-02 | CON-01 (parte 2) | — |

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente

## Criterios de aceptación de esta tarjeta

- [ ] Existe la jerarquía de excepciones de dominio en `model/` (Java puro): `NegocioException` y subclases `DatosInvalidosException`, `NoAutorizadoException`, `NoEncontradoException`, `ConflictoException`.
- [ ] `ErrorDTO(codigo, mensaje)` y `controller/ManejadorErrores` (`@RestControllerAdvice`) mapean: DatosInvalidos→422 · NoAutorizado→403 · NoEncontrado→404 · Conflicto→409 · cualquier otra→500 con mensaje genérico (sin stack ni datos del paciente).
- [ ] Perfiles `dev` y `test` con configuración externa; **ninguna clave en el repo**.
- [ ] Logger (SLF4J) en lugar de `System.out`; una línea por error técnico con identificador de correlación sencillo.
- [ ] `GET /actuator/health` responde (el starter de actuator ya está en el `pom`).
- [ ] El catálogo de errores queda escrito en `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md` (ARQ-05 pide «catálogo de errores compartido»).

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// model/NegocioException.java   (+ DatosInvalidosException, NoAutorizadoException, NoEncontradoException, ConflictoException)
public class NegocioException extends RuntimeException { public NegocioException(String codigo, String mensaje) {...} }
// model/ErrorDTO.java
public record ErrorDTO(String codigo, String mensaje) {}
// controller/ManejadorErrores.java
@RestControllerAdvice class ManejadorErrores { /* un @ExceptionHandler por excepción de dominio */ }
```

## Tareas

- [ ] Crear las excepciones, `ErrorDTO` y `ManejadorErrores`.
- [ ] Separar `application.properties` (común) de `application-dev.properties` y `application-test.properties`.
- [ ] Reemplazar cualquier `System.out`/`printStackTrace` por logger.
- [ ] Pruebas: una por cada excepción → código HTTP y cuerpo (MockMvc con un controller de prueba).
- [ ] Documentar el catálogo de errores en ARQ-06.

## Pruebas (lado derecho del V)

- [ ] Enviar un cuerpo inválido y comprobar `422` con `ErrorDTO` sin eco de datos personales (CP-CNS-01B).

## Fuera de alcance (no hacer en esta tarjeta)

- Autenticación (S3-08).
- OpenAPI/Swagger: no está aprobado; solo si el equipo lo decide.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
