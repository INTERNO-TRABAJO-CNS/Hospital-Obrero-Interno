# DoD — [T100-00][Java+Flutter] Preparar la rama T100, contratos compartidos y base Flutter responsive

| Campo | Valor |
|---|---|
| Issue | [#60](https://github.com/INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno/issues/60) — CERRADO el 06/11/2026 |
| Pull Request | [#76](https://github.com/INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno/pull/76) — integrado el 06/11/2026 (3 commits, +4724 líneas) |
| Rama | `feature/T100-00-base-contratos-flutter` → `feature/T100-algoritmos-ejecucion` |
| Milestone | Sprint 4 - Gestion de citas (6–12 nov) |
| Responsable | Benjamín |
| Revisor | Alejandro |
| Puntos | 5 |
| Requisitos | RNF-03 · RNF-07 · RNF-08 (transversal) |
| Fecha de verificación del DoD | 12/11/2026, sobre `feature/T100-algoritmos-ejecucion` (commit `78a4b38`) |

## 1. Definition of Done de la tarjeta
- [x] Cumple las reglas de negocio de la tarjeta (ver sección 2).
- [x] `./mvnw test` en verde: **157 pruebas, 0 fallos, 0 errores**.
- [x] `flutter test` en verde: **43 pruebas, todas pasan**.
- [x] Pantallas adaptables a celular / escritorio / TV, cubiertas por `responsive_test.dart` (599 → celular, 600 y 1599 → escritorio, 1600 y ruta `/tv` → TV).
- [x] PR hacia la rama T100 integrado por Benjamín.
- [ ] Aprobación formal del revisor (Alejandro) en GitHub: **no está registrada** (el PR quedó como `REVIEW_REQUIRED`). Ver observaciones.
- [x] Resumen del PR disponible para la revisión correcto/incorrecto.

## 2. Reglas de negocio
| Regla | Estado | Dónde se comprueba |
|---|---|---|
| Los 4 estados de cita, 4 de solicitud, 4 roles y 3 especialidades salen solo de `model/compartido` | ✅ | `EstadoCita`, `EstadoSolicitud`, `RolUsuario`, `TipoEspecialidad` |
| Duraciones por especialidad: General 30–45, Odontología 45–60, Pediatría 30–45 | ✅ | `TipoEspecialidadTest` (4 pruebas) |
| Se programa contra interfaces y se prueba con `Fake*` en memoria | ✅ | 9 Fakes en `src/test/.../model/compartido` |
| Una cita nunca se borra: pasa a `CANCELADA` | ✅ | `CitaRepository.cambiarEstado` condicional; `FakeCitaRepositoryTest` |
| `store` rechaza dos citas del mismo médico + fecha + hora (salvo CANCELADA) | ✅ | `FakeCitaRepositoryTest` (12 pruebas) |
| `DominioException(codigo)` se traduce a HTTP `{codigo, mensaje}` | ✅ | `ManejadorErroresTest` (4 pruebas) |
| Usuario temporal por headers `X-Usuario-Id`, `X-Rol`, `X-Persona-Id`, marcado como TEMPORAL | ✅ | `ResolutorUsuario` + `ResolutorUsuarioTest` (4 pruebas: lectura, falta header, id no numérico, rol desconocido → 400) |

## 3. Entregables (rutas)
| Entregable | Estado |
|---|---|
| `backend/.../model/compartido/` — enums, `DominioException`, 7 DTO (`record`), interfaces (`CitaRepository`, `SolicitudRepository`, `HistorialRepository`, `CambioCitaRepository`, `NotificacionPublicador`, `AgendaEventos`, `CancelacionCita`, `FinalizacionCita`, `Reloj`, `RelojSistema`) y `ResultadoCancelacion`/`ResultadoFinalizacion` | ✅ |
| `backend/.../controller/UsuarioAutenticado.java`, `ResolutorUsuario.java`, `ManejadorErrores.java` | ✅ |
| `backend/src/test/.../model/compartido/Fake*.java` + `RelojFijo` | ✅ |
| `frontend/lib/core/responsive.dart`, `api_client.dart`, `app_router.dart`, `app_theme.dart` | ✅ |
| `frontend/lib/views/shell/app_shell.dart` (NavigationBar / NavigationRail / sin menú en TV) | ✅ |
| `frontend/test/core/responsive_test.dart` | ✅ |

## 4. Pruebas que pedía la tarjeta
| Prueba pedida | Resultado |
|---|---|
| `RelojFijoTest` / `FakeCitaRepositoryTest` (UNIQUE simulado y `cambiarEstado` condicional) | ✅ 2 + 12 pruebas en verde |
| `ResponsiveTest` (Flutter): 599 → celular, 600 → escritorio, 1600 → TV | ✅ 5 pruebas en verde |
| Extra: `FakeSolicitudRepositoryTest`, `TipoEspecialidadTest`, `ManejadorErroresTest`, `ResolutorUsuarioTest`, `api_client_test.dart` | ✅ 5 + 4 + 4 + 4 + 4 pruebas en verde |

## 5. Reglas de código
- [x] Controller sin SQL ni reglas; DTO como `record`; Repository como interfaz en `model`.
- [x] El PR solo trae código (sin `.md` ni capturas).
- [x] El contrato de `model/compartido` quedó publicado para el resto de las tarjetas T100 (de él dependen T100-01 … T100-15).

## 6. Observaciones
- **Aprobación del revisor:** el PR #76 se integró sin una revisión aprobada en GitHub. Si el docente exige la aprobación formal, pedirle a Alejandro que deje un comentario de aprobación en el PR o en el issue #60.
- Las pantallas en Chrome, Windows y TV se comprobaron con las pruebas de puntos de corte; la evidencia de la prueba manual (capturas) no está en el repositorio.
- Seguridad real (Spring Security) queda fuera de alcance: el `ResolutorUsuario` es temporal hasta S3-08.
