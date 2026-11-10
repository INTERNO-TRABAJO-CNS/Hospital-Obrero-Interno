# [T100-19][Java+Flutter][CU-02] Catálogo: médicos especialistas (10 especialidades) y quirófanos por especialidad

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-19-cu02-especialistas-quirofanos` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-02 |
| Requisitos | RF-02 · RU-13 · RF-24 |
| Depende de / Bloquea | T100-01, T100-17 / Bloquea: T100-23 |

## Objetivo
El administrador registra médicos de las 10 especialidades internas y administra los quirófanos (cada uno de una sola especialidad, con su horario y su tiempo de limpieza).

## Reglas de negocio que debes cumplir
- Médico especialista: la duración de su ficha debe estar en el rango de su especialidad y el cupo entre 5 y 7 (regla existente de `Medico`, no se duplica).
- Quirófano: nombre único entre los activos; especialidad que `operaEnQuirofano()` (si no → `ESPECIALIDAD_NO_OPERA`); `apertura < cierre`; limpieza entre **30 y 45 min**.
- Un quirófano no se comparte: su especialidad no se puede cambiar si tiene cirugías PROGRAMADAS futuras (`QUIROFANO_CON_CIRUGIAS`, 409); tampoco se puede desactivar en ese caso.
- Horario de quirófano por defecto 07:00–19:00 (`// PENDIENTE PV-19`).
- Solo `ADMINISTRADOR`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-02_GestionarUsuariosRolesPermisos/CU-02_GestionarUsuariosRolesPermisos.puml` *(lo crea T100-16)* (actualizado por T100-16)
- `06_proyect/Issues_T100/T100-01_cu02-catalogos-crud.md`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/Quirofano.java, QuirofanoRepository.java` | entidad con `isValid()` y la interfaz |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/CatalogoService.java` | agregar `listarQuirofanos()`, `crearQuirofano(QuirofanoDTO)`, `editarQuirofano(int id, QuirofanoDTO)`, `desactivarQuirofano(int id)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgQuirofanoRepository.java` | sobre `quirofano.quirofano` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/AdminCatalogoController.java` | `GET/POST /api/v1/admin/quirofanos`, `PUT/DELETE /api/v1/admin/quirofanos/{id}` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/recursos/CatalogoServiceTest.java + FakeQuirofanoRepository.java` | casos nuevos |
| `04_imp/frontend/lib/models/catalogo/especialidad.dart` | 13 especialidades agrupadas en «Básicas» e «Internas», con nombre en español |
| `04_imp/frontend/lib/views/admin/catalogo_medicos_view.dart` | selector de especialidad agrupado; muestra el rango de duración permitido |
| `04_imp/frontend/lib/views/admin/catalogo_quirofanos_view.dart + models/catalogo/quirofano.dart` | nueva pestaña en `catalogos_view.dart` |
| `04_imp/frontend/lib/services/catalogo_service.dart, services/fake_catalogo_service.dart, controllers/catalogo_controller.dart` | métodos de quirófanos |

## Paso a paso
1. Rama `feature/T100-19-cu02-especialistas-quirofanos`.
2. Modelo + tests con `FakeQuirofanoRepository`.
3. `Pg*` y endpoints.
4. Flutter: especialidades agrupadas y pestaña Quirófanos.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `crearQuirofano_limpieza29_rechaza / 46_rechaza / 30_ok` | rango de limpieza |
| `crearQuirofano_infectologia_ESPECIALIDAD_NO_OPERA` | solo especialidades que operan |
| `cambiarEspecialidad_conCirugias_409` | no se comparte ni se reasigna |
| `crearMedico_cardiologia_duracion45_rechaza` | rango 20–40 |
| `catalogos_view_test.dart` | aparece la pestaña Quirófanos |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-19-cu02-especialistas-quirofanos
# ... trabajar, commits pequeños: feat(T100-19): descripción
git push -u origin feature/T100-19-cu02-especialistas-quirofanos
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

## Fuera de alcance
- Programar cirugías (T100-23).
- Supervisión de quirófanos (T100-25).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-19-cu02-especialistas-quirofanos`
