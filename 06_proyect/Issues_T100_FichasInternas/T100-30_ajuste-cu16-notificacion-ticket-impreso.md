# [T100-30][Java+Flutter+Nuxt][Ajuste CU-16] Notificación en la app y ticket impreso para citas comunes, fichas internas y cirugías

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Abigail |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-30-ajuste-cu16-notificacion-ticket-impreso` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-16 |
| Requisitos | RF-16 · RNF-03 |
| Depende de / Bloquea | T100-15, T100-17, T100-18 / Bloquea: — |

## Objetivo
Toda cita común, ficha interna o cirugía se avisa **en la app** y además se puede **imprimir** el ticket por si el paciente no tiene internet.

## Reglas de negocio que debes cumplir
- Tipos nuevos de notificación (los publican T100-20 y T100-23 con `citaId = null`): `FICHA_INTERNA_ASIGNADA`, `FICHA_INTERNA_EN_ESPERA`, `FICHA_INTERNA_REPROGRAMADA`, `CIRUGIA_PROGRAMADA`, `CIRUGIA_REPROGRAMADA`.
- `TicketDTO`: tipo, número, paciente, número de afiliado, especialidad, profesional, lugar (consultorio o quirófano), fecha, hora, indicaciones y fecha de emisión. **Nunca** la gravedad.
- Quién puede pedir un ticket: el paciente dueño, el médico o especialista involucrado, enfermería (citas comunes) y el administrador.
- Impresión: Nuxt con una página de impresión (`window.print()` y CSS `@media print`); Flutter (enfermería/admisión) con los paquetes `pdf` + `printing`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-16_GenerarNotificacionCita/CU-16_GenerarNotificacionCita.puml` *(lo crea T100-16)*
- Tarjeta `T100-15`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/comunicacion/TicketService.java + TicketDTO.java` | `deCita(citaId, u)`, `deFichaInterna(fichaId, u)`, `deCirugia(cirugiaId, u)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/comunicacion/PlantillasNotificacion.java` | texto de cada tipo (lo usan T100-20 y T100-23) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/TicketController.java` | `GET /api/v1/tickets/cita/{id}`, `GET /api/v1/tickets/ficha-interna/{id}`, `GET /api/v1/tickets/cirugia/{id}` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/comunicacion/TicketServiceTest.java` | permisos y que no salga la gravedad |
| `04_imp/frontend-web/pages/tickets/[tipo]/[id].vue` | ticket imprimible |
| `04_imp/frontend-web/services/ticketService.ts + fake, models/ticket.ts, tests/ticket.test.ts` |  |
| `04_imp/frontend/lib/views/paciente/notificaciones_view.dart` | íconos y textos de los tipos nuevos; al tocar abre el ticket |
| `04_imp/frontend/lib/views/paciente/ticket_view.dart, views/enfermeria/imprimir_ticket_view.dart` | ver el ticket en la app / imprimirlo en admisión |
| `04_imp/frontend/pubspec.yaml` | agregar `pdf` y `printing` |

## Paso a paso
1. Rama `feature/T100-30-ajuste-cu16-notificacion-ticket-impreso`.
2. `TicketService` + tests.
3. Plantillas de notificación.
4. Páginas Nuxt y Flutter.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `ticket_fichaInterna_sinGravedad` |  |
| `ticket_pacienteAjeno_403` |  |
| `plantillas_todosLosTipos` |  |
| `ticket.test: imprime` | Vitest |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-30-ajuste-cu16-notificacion-ticket-impreso
# ... trabajar, commits pequeños: feat(T100-30): descripción
git push -u origin feature/T100-30-ajuste-cu16-notificacion-ticket-impreso
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- Controller sin SQL ni reglas; la Entidad no sale del modelo; DTO = `record`; Repository = interfaz en `model`, implementación `Pg*` en `infrastructure/pg`.
- `PreparedStatement` con `?`, `try-with-resources`, logger (nada de `System.out` ni `printStackTrace`).
- Un caso de uso = un método de Service. Cada flujo alternativo `[Na]` del diagrama = un resultado/excepción + un test.
- Service probado con `Fake*` (sin BD); `Pg*` contra PostgreSQL; Controller con MockMvc.
- **En el PR solo código y migraciones** (nada de `.md`, capturas ni documentación). Cambios fuera de las carpetas de la tabla: pedirlos antes a Benjamín.
- Si necesitas cambiar un contrato de `model/compartido`, **no lo cambies**: abre un comentario en este issue y avisa a Benjamín.
- Nada «demasiado automático»: el sistema calcula y propone; las acciones las dispara una persona (médico, especialista, administrador). El único proceso programado nuevo es el de fichas EN_ESPERA (T100-20).
- **MVC también en la web:** `pages/` = vista, `stores/` (Pinia) = controlador de interfaz, `services/` = llamadas REST, `models/` = tipos. Una página **nunca** llama `fetch` directo: usa su *store* y el *store* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con Vitest (`npm run test`) para *stores* y *services*.
- La URL del backend sale de `NUXT_PUBLIC_API_URL`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- SMS o correo electrónico.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-30-ajuste-cu16-notificacion-ticket-impreso`
