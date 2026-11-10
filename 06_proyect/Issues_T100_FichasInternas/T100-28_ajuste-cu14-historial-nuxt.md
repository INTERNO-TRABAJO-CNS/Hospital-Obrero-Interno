# [T100-28][Java+Nuxt][Ajuste CU-14] Historial del paciente en la web Nuxt, con sus fichas internas y cirugías

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Alex |
| Revisor (aprueba) | Abigail |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-28-ajuste-cu14-historial-nuxt` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-14 |
| Requisitos | RF-14 · RNF-07 |
| Depende de / Bloquea | T100-12, T100-17, T100-18 / Bloquea: — |

## Objetivo
El historial del paciente («hist web JS» de la pizarra) se muestra en la web del médico, y ahora incluye las derivaciones (fichas internas) y las cirugías.

## Reglas de negocio que debes cumplir
- Mismas reglas de T100-12. Además puede verlo el especialista que tiene una ficha interna o una cirugía con ese paciente (`// PENDIENTE PV-22` sobre qué ve exactamente).
- Nuevo endpoint `GET /api/v1/medico/pacientes/{id}/derivaciones` → fichas internas y cirugías del paciente (especialidad, fecha, hora, estado). La **gravedad** solo se incluye si quien consulta es médico general o pediatra.
- La vista Flutter `historial_paciente_view.dart` ya no se usa para el médico.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-14_ConsultarDatosPaciente/CU-14_ConsultarDatosPaciente.puml` *(lo crea T100-16)*
- Tarjeta `T100-12`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/historial/HistorialService.java` | permiso del especialista + `derivaciones(medicoId, pacienteId)` (usa `FichaInternaRepository` y `CirugiaRepository`) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/historial/DerivacionDTO.java` | record |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/HistorialController.java` | nuevo GET |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/historial/HistorialServiceTest.java` | casos nuevos |
| `04_imp/frontend-web/pages/medico/pacientes/[pacienteId]/historial.vue` | línea de tiempo de atenciones, recetas, fichas internas y cirugías |
| `04_imp/frontend-web/stores/historial.ts, services/historialService.ts + fake, models/historial.ts, tests/historial.test.ts` |  |

## Paso a paso
1. Rama `feature/T100-28-ajuste-cu14-historial-nuxt`.
2. Backend + tests.
3. Página Nuxt.
4. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `especialistaConFicha_puedeVerHistorial` |  |
| `derivaciones_especialista_sinGravedad` |  |
| `historial.test: muestraFichasYCirugias` | Vitest |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-28-ajuste-cu14-historial-nuxt
# ... trabajar, commits pequeños: feat(T100-28): descripción
git push -u origin feature/T100-28-ajuste-cu14-historial-nuxt
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
- Editar el historial.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-28-ajuste-cu14-historial-nuxt`
