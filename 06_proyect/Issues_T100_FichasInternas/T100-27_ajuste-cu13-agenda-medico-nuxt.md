# [T100-27][Flutter][Ajuste CU-13/12] Agenda del médico en la app Flutter con acceso a «Atender» y «Ticket interno»

> **Actualización 15/11/2026:** el proyecto ya no usa Nuxt. Todas las pantallas del médico (general, pediatría, odontología y especialistas) están en la **app Flutter** (`04_imp/frontend`), igual que paciente, enfermería, administrador y TV. Equivalencias: `pages/` → `lib/views/medico/`, `stores/` → `lib/controllers/`, `services/` → `lib/services/`, `models/` → `lib/models/medico/`; `npm run test` → `flutter test`. CORS del backend: `http://localhost:5050` (`flutter run -d chrome --web-port 5050`). El proyecto Nuxt (`04_imp/frontend-web`) se borró.

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
Por la decisión del 10/11/2026, **las pantallas del médico van en la app Flutter**. El backend de T100-08 no cambia; la agenda queda en `lib/views/medico/agenda_medico_view.dart` (decisión del 15/11/2026).

## Reglas de negocio que debes cumplir
- Mismas reglas de T100-08: solo sus citas, hoy por defecto, selector de fecha, orden por hora y CANCELADAS marcadas.
- Encabezado «N citas hoy»; filas: ficha · hora · paciente · estado.
- Botón **Atender** (cita ASISTENCIA_CONFIRMADA) → `/medico/atencion/[citaId]` (T100-29).
- Botón **Ticket interno** solo si el médico `puedeDerivar` y la cita es de hoy, ASISTENCIA_CONFIRMADA o ATENDIDA → `/medico/ticket-interno?citaId=` (T100-21).
- La confirmación de asistencia de enfermería **se queda en Flutter** (T100-10 no cambia).
- Ya no aplica: desde el 15/11/2026 la agenda del médico está en Flutter (`lib/views/medico/agenda_medico_view.dart`).

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-13_ConsultarAgendaMedica/CU-13_ConsultarAgendaMedica.puml` *(lo crea T100-16)*
- Tarjeta `T100-08`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/frontend/lib/views/medico/agenda_medico_view.dart` | vista |
| `04_imp/frontend/lib/controllers/medico_controller.dart, lib/services/medico_service.dart + fake_medico_service.dart, lib/models/medico/agenda_medico.dart` | `GET /api/v1/medico/agenda?fecha=` |
| `04_imp/frontend/test/medico/agenda_y_atencion_test.dart` | `flutter test` |

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
- **MVC en Flutter:** `views/` = vista, `controllers/` = controlador, `services/` = llamadas REST, `models/` = datos. Una vista **nunca** llama a `http` directo: usa su *controller* y el *controller* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con `flutter test` para *controllers*, *services* y vistas.
- La URL del backend sale de `--dart-define=API_URL=...`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- Backend de la agenda (ya está en T100-08).
- Confirmar asistencia (enfermería, Flutter).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si la tarjeta toca Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-27-ajuste-cu13-agenda-medico-nuxt`
