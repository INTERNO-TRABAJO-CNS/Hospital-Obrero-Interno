# [T100-29][Nuxt][Ajuste CU-15] Registrar receta y fin de la atención en la web Nuxt, con acceso a «Ticket interno»

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Abigail |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-29-ajuste-cu15-atencion-nuxt` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 2 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-15 |
| Requisitos | RF-15 · RNF-07 |
| Depende de / Bloquea | T100-14 (backend), T100-18 / Bloquea: — |

## Objetivo
La pantalla de atención del médico pasa de Flutter a la web Nuxt. El backend de T100-14 no cambia.

## Reglas de negocio que debes cumplir
- Mismas reglas de T100-14 (datos autocompletados, receta de 1 a 2000 caracteres, «Finalizar atención»).
- Botón **Generar ticket interno** (solo si el médico `puedeDerivar`) → `/medico/ticket-interno?citaId=` (T100-21). Funciona antes o después de finalizar, siempre que sea el mismo día.
- Si ya hiciste `atencion_view.dart` en Flutter, no se integra.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-15_RegistrarDatosAtencion/CU-15_RegistrarDatosAtencion.puml` *(lo crea T100-16)*
- Tarjeta `T100-14`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/frontend-web/pages/medico/atencion/[citaId].vue` | tarjeta con datos, campo «Medicación», «Finalizar atención», «Generar ticket interno» |
| `04_imp/frontend-web/stores/atencion.ts, services/atencionService.ts + fake, models/atencion.ts, tests/atencion.test.ts` | `GET/POST /api/v1/citas/{id}/atencion` |

## Paso a paso
1. Rama `feature/T100-29-ajuste-cu15-atencion-nuxt`.
2. *Service*, *store* y tests.
3. Página.
4. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `atencion.test: recetaVaciaNoEnvia` |  |
| `atencion.test: botonTicketSoloSiPuedeDerivar` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-29-ajuste-cu15-atencion-nuxt
# ... trabajar, commits pequeños: feat(T100-29): descripción
git push -u origin feature/T100-29-ajuste-cu15-atencion-nuxt
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- **MVC también en la web:** `pages/` = vista, `stores/` (Pinia) = controlador de interfaz, `services/` = llamadas REST, `models/` = tipos. Una página **nunca** llama `fetch` directo: usa su *store* y el *store* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con Vitest (`npm run test`) para *stores* y *services*.
- La URL del backend sale de `NUXT_PUBLIC_API_URL`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- Backend de atención (T100-14).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-29-ajuste-cu15-atencion-nuxt`
