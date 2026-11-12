# DoD — [T100-01][Java+Flutter][CU-02] Administrar catálogos: médicos, consultorios, horarios, cupos y días no hábiles

| Campo | Valor |
|---|---|
| Issue | [#61](https://github.com/INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno/issues/61) — CERRADO el 09/11/2026 |
| Pull Request | [#78](https://github.com/INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno/pull/78) — integrado el 09/11/2026 (4 commits, +4848 / −30 líneas) |
| Rama | `feature/T100-01-cu02-catalogos-crud` → `feature/T100-algoritmos-ejecucion` |
| Milestone | Sprint 4 - Gestion de citas (6–12 nov) |
| Responsable | Benjamín |
| Revisor | Alejandro |
| Puntos | 5 |
| Caso de uso / Requisitos | CU-02 · RF-02 · RU-12 · RU-13 · RNF-01 |
| Depende de / Bloquea | T100-00 (#60) / T100-02, T100-03 |
| Fecha de verificación del DoD | 12/11/2026, sobre `feature/T100-algoritmos-ejecucion` (commit `78a4b38`) |

## 1. Definition of Done de la tarjeta
- [x] Cumple las reglas de negocio y todos los flujos alternativos del CU-02 (ver secciones 2 y 3).
- [x] `./mvnw test` en verde: **157 pruebas, 0 fallos, 0 errores** (de ellas, 40 de `CatalogoServiceTest` y 18 de `AdminCatalogoControllerTest`).
- [x] `flutter test` en verde: **43 pruebas, todas pasan** (incluye controlador, modelos y vista de catálogos).
- [x] Pantallas de **escritorio** del administrador (tabla + formulario) probadas con `FakeCatalogoService`.
- [x] Endpoints de catálogos incluidos en la colección de Postman (`06_proyect/POSTMAN/CNS_Postman_Collection.json`: `/api/v1/admin/medicos`, `consultorios`, `horarios`, `dias-no-habiles`).
- [x] PR hacia la rama T100 integrado por Benjamín.
- [x] Resumen del PR disponible para la revisión correcto/incorrecto.

## 2. Reglas de negocio
| Regla | Estado | Pruebas que lo demuestran |
|---|---|---|
| Cupo diario por médico entre 5 y 7 | ✅ | `crearMedico_cupo8_rechaza`, `crearMedico_cupo4_rechaza`, `crearMedico_cupoEnLosLimites5y7_acepta` |
| Duración de ficha dentro del rango de la especialidad | ✅ | `crearMedico_duracionFueraDeRango`, `crearMedico_duracionDependeDeLaEspecialidad` |
| No se aplica un cambio que afecte citas PROGRAMADAS → `CAMBIO_AFECTA_CITAS` | ✅ | `modificarMedico_conCitasProgramadas`, `darDeBajaMedico_conCitasProgramadas`, `darDeBajaConsultorio_conCitasProgramadas`, `modificarHorario_conCitasProgramadas`, `registrarDiaNoHabil_conCitasProgramadasEseDia_afectaCitas` |
| Las citas CANCELADAS no bloquean cambios | ✅ | `modificarMedico_citasCanceladasNoCuentan` |
| Baja lógica (`activo=false`), nada se borra | ✅ | `darDeBajaMedico_esLogica_noBorra`, `darDeBajaConsultorio_logica_ySinCitas`, `darDeBajaHorario_logica_yConCitasRechaza`, `darDeBajaDiaNoHabil_esLogica` |
| Horarios sin solapamiento (mismo médico o mismo consultorio) → `HORARIO_SOLAPADO` | ✅ | `definirHorario_solapado`, `definirHorario_consultorioOcupadoPorOtroMedico_solapado`, `definirHorario_franjasContiguasOEnOtroDia_noSolapan` |
| Día no hábil solo en fecha futura y sin duplicados | ✅ | `registrarDiaNoHabil_fechaHoyOPasada_rechaza`, `registrarDiaNoHabil_duplicado_rechaza` |
| Solo el rol `ADMINISTRADOR` (RNF-01) | ✅ | `controller_sinRolAdmin_403`, `controller_rolMedico_403_tambienEnEscritura` |
| Usuarios y roles se reutilizan de S3-09 (no se duplican) | ✅ | Sin cambios en el módulo de identidad |

## 3. Flujos alternativos del CU-02
| Flujo | Respuesta esperada | Prueba |
|---|---|---|
| `[1a]` usuario sin rol administrador | 403 | `controller_sinRolAdmin_403` ✅ |
| `[3a]` cupo o duración inválidos | 422 | `crearMedico_cupo8_422`, `crearMedico_duracionFueraDeRango_422` ✅ (Flutter: `[3a] cupo 8 muestra el error y no agrega el medico`) |
| `[3b]` el cambio afecta citas programadas | 409 | `modificarMedico_conCitasProgramadas_409`, `horario_delete_conCitasProgramadas_409` ✅ (Flutter: `[3b] baja con citas programadas: no se aplica`) |
| Recurso inexistente | 404 | `modificarMedico_noExiste_404` ✅ |
| Sin encabezados de usuario | 400 | `controller_sinEncabezados_400` ✅ |

## 4. Entregables (rutas)
| Entregable | Estado |
|---|---|
| `backend/.../model/recursos/` — entidades `Medico`, `Consultorio`, `HorarioMedico`, `DiaNoHabil` con `isValid()`, sus DTO (`record`) y las 4 interfaces Repository | ✅ |
| `backend/.../model/recursos/CatalogoService.java` con las firmas del contrato | ✅ |
| `backend/.../infrastructure/pg/PgMedicoRepository`, `PgConsultorioRepository`, `PgHorarioRepository`, `PgDiaNoHabilRepository` | ✅ |
| `backend/src/main/resources/db/migration/V1__recursos_catalogos.sql` (esquema `recursos` + semilla de especialidades) | ✅ |
| `backend/.../controller/AdminCatalogoController.java` bajo `/api/v1/admin/{medicos,consultorios,horarios,dias-no-habiles}` (POST, PUT, DELETE = baja lógica, GET) | ✅ |
| `backend/src/test/.../model/recursos/CatalogoServiceTest.java` + 4 `Fake*Repository` | ✅ |
| `backend/src/test/.../controller/AdminCatalogoControllerTest.java` (MockMvc) | ✅ |
| `frontend/lib/models/catalogo/`, `services/catalogo_service.dart`, `services/fake_catalogo_service.dart`, `controllers/catalogo_controller.dart` | ✅ |
| `frontend/lib/views/admin/catalogo_medicos_view.dart`, `catalogo_consultorios_view.dart`, `catalogo_horarios_view.dart`, `catalogo_dias_view.dart` (+ `catalogos_view.dart`) | ✅ |

## 5. Pruebas que pedía la tarjeta
| Prueba pedida | Resultado |
|---|---|
| `crearMedico_cupo8_rechaza` → `VALIDACION_CUPO` | ✅ |
| `crearMedico_duracionFueraDeRango` → `VALIDACION_DURACION` | ✅ |
| `modificarMedico_conCitasProgramadas` → `CAMBIO_AFECTA_CITAS` | ✅ |
| `definirHorario_solapado` → `HORARIO_SOLAPADO` | ✅ |
| `controller_sinRolAdmin_403` (MockMvc con `X-Rol: PACIENTE`) | ✅ |

Además de las 5 pedidas, hay pruebas de modificar y dar de baja consultorios, horarios y días no hábiles (40 en el Service, 18 en el Controller y las de Flutter para controlador, modelos y vista).

## 6. Reglas de código
- [x] Controller solo traduce Service → HTTP (sin SQL ni reglas).
- [x] Entidades no salen del modelo; DTO como `record`; Repository como interfaz en `model` e implementación `Pg*` en `infrastructure/pg`.
- [x] Service probado con `Fake*` (sin BD); Controller con MockMvc.
- [x] Un caso de uso = un método de `CatalogoService`; cada flujo alternativo tiene su código de error y su prueba.
- [x] El PR solo trae código y migraciones.

## 7. Observaciones
- **Aprobación del revisor:** el PR #78 se integró sin una revisión aprobada en GitHub. Si el docente exige la aprobación formal, pedirle a Alejandro que deje un comentario de aprobación en el PR o en el issue #61.
- Los `Pg*Repository` contra PostgreSQL se prueban a mano con la colección de Postman; no hay pruebas automáticas de integración con la base (quedan para T100-31).
- Fuera de alcance: usuarios y roles (S3-09) y el calendario de feriados oficiales (los días no hábiles se cargan a mano).
