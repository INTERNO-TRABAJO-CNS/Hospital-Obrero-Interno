# [T100-27][Nuxt][Ajuste CU-13/12] Agenda del médico en la web Nuxt (en lugar de Flutter) con acceso a «Atender» y «Ticket interno»

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Alejandro |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-27-ajuste-cu13-agenda-medico-nuxt` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-13, CU-12 (parte del médico) |
| Requisitos | RF-13 · RNF-07 |
| Depende de / Bloquea | T100-08 (backend de la agenda), T100-18 / Bloquea: — |

## Objetivo
Por la decisión del 10/11/2026, **las pantallas del médico van en la web Nuxt**. El backend de T100-08 no cambia; la vista `views/medico/agenda_view.dart` de Flutter se reemplaza por una página Nuxt.

## Reglas de negocio que debes cumplir
- Mismas reglas de T100-08: solo sus citas, hoy por defecto, selector de fecha, orden por hora y CANCELADAS marcadas.
- Encabezado «N citas hoy»; filas: ficha · hora · paciente · estado.
- Botón **Atender** (cita ASISTENCIA_CONFIRMADA) → `/medico/atencion/[citaId]` (T100-29).
- Botón **Ticket interno** solo si el médico `puedeDerivar` y la cita es de hoy, ASISTENCIA_CONFIRMADA o ATENDIDA → `/medico/ticket-interno?citaId=` (T100-21).
- La confirmación de asistencia de enfermería **se queda en Flutter** (T100-10 no cambia).
- Si ya hiciste la vista Flutter del médico, **no se integra**: deja en el PR solo el backend y la página Nuxt.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-13_ConsultarAgendaMedica/CU-13_ConsultarAgendaMedica.puml` *(lo crea T100-16)*
- Tarjeta `T100-08`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/frontend-web/pages/medico/agenda.vue` | vista |
| `04_imp/frontend-web/stores/agenda.ts, services/agendaService.ts + services/fake/fakeAgendaService.ts, models/agenda.ts` | `GET /api/v1/medico/agenda?fecha=` |
| `04_imp/frontend-web/tests/agenda.test.ts` | Vitest |

## Paso a paso
1. Rama `feature/T100-27-ajuste-cu13-agenda-medico-nuxt` (después de T100-18).
2. *Service* + *fake* + *store* + tests.
3. Página.
4. Probar con el backend de T100-08.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `agenda.test: contadorCitasHoy` |  |
| `agenda.test: ticketInternoSoloSiPuedeDerivar` | odontólogo no ve el botón |
| `agenda.test: canceladaMarcada` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-27-ajuste-cu13-agenda-medico-nuxt
# ... trabajar, commits pequeños: feat(T100-27): descripción
git push -u origin feature/T100-27-ajuste-cu13-agenda-medico-nuxt
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- **MVC también en la web:** `pages/` = vista, `stores/` (Pinia) = controlador de interfaz, `services/` = llamadas REST, `models/` = tipos. Una página **nunca** llama `fetch` directo: usa su *store* y el *store* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con Vitest (`npm run test`) para *stores* y *services*.
- La URL del backend sale de `NUXT_PUBLIC_API_URL`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- Backend de la agenda (ya está en T100-08).
- Confirmar asistencia (enfermería, Flutter).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-27-ajuste-cu13-agenda-medico-nuxt`
