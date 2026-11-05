# [T100-04][Java+Flutter][CU-19/20] Reportes, indicadores y agenda de supervisión en tiempo real (solo lectura)

| Campo | Valor |
|---|---|
| Milestone | Sprint 6 - Validacion y entrega (vence 2026-11-28) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-04-cu19-cu20-reportes-supervision` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-19, CU-20 |
| Requisitos | RF-19 · RF-20 · RNF-08 |
| Depende de / Bloquea | T100-03 (o `FakeCitaRepository`) / Bloquea: — |

## Objetivo
El administrador ve por especialidad → médico → fichas del día (número, horario, estado), reportes por estado/médico/especialidad/consultorio, y la vista se actualiza sola por WebSocket. No puede modificar nada.

## Reglas de negocio que debes cumplir
- Solo lectura: cualquier intento de modificar → `SOLO_LECTURA` (403).
- Cumplimiento por médico: meta mínima 5 fichas, máximo 7.
- Tiempos de espera/atención por separado **no son medibles**: solo confirmación→fin (`// PENDIENTE PV-17`).
- Solo `ADMINISTRADOR`.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-19_GenerarReportesCitas.puml`, `03_des/01-diagramaCompor/CU-20_ConsultarIndicadoresGestion.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/reportes/ReporteService.java, SupervisionService.java` | consultas que devuelven DTO directo |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/reportes/ResumenEspecialidadDTO.java, FichaSupervisionDTO.java, ReporteEstadoDTO.java, CumplimientoMedicoDTO.java` | records |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/reportes/ReporteRepository.java` | interfaz de lecturas agregadas (`count`/`group by`) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgReporteRepository.java` | SQL de lectura |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/ws/AgendaEventosWs.java` | implementa `AgendaEventos`: difunde `CitaDTO` a los suscritos de `/ws/supervision` y `/ws/fichas` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/ReporteController.java, SupervisionController.java` | GET `/api/v1/admin/reportes/...`, GET `/api/v1/admin/supervision?fecha=` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/reportes/ReporteServiceTest.java` | con `FakeReporteRepository` |
| `04_imp/frontend/lib/views/admin/supervision_view.dart, reportes_view.dart` | pantallas de escritorio; supervisión con `WebSocketChannel` |
| `04_imp/frontend/lib/services/supervision_service.dart, controllers/supervision_controller.dart, models/supervision/*.dart` |  |

## Contrato (copiar las firmas tal cual)
```java
public class SupervisionService { public List<ResumenEspecialidadDTO> resumenDelDia(LocalDate fecha); public List<FichaSupervisionDTO> fichasDeMedico(long medicoId, LocalDate fecha); }
public class ReporteService { public List<ReporteEstadoDTO> porEstado(LocalDate d, LocalDate h); public List<CumplimientoMedicoDTO> cumplimiento(LocalDate d, LocalDate h); }
```

## Paso a paso
1. Rama `feature/T100-04-cu19-cu20-reportes`.
2. Services + tests con Fake.
3. `Pg` de lectura y controllers (sin lógica).
4. `AgendaEventosWs` (Spring WebSocket). Reemplaza el fake en producción.
5. Pantallas Flutter de escritorio con datos de `FakeSupervisionService` y luego reales.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `resumenDelDia_agrupaPorEspecialidad` |  |
| `cumplimiento_marcaMenosDe5` |  |
| `intentoModificar_403` |  |
| `ws_difundeAlSuscrito` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-04-cu19-cu20-reportes-supervision
# ... trabajar, commits pequeños: feat(T100-04): descripción
git push -u origin feature/T100-04-cu19-cu20-reportes-supervision
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- Controller sin SQL ni reglas; la Entidad no sale del modelo; DTO = `record`; Repository = interfaz en `model`, implementación `Pg*` en `infrastructure/pg`.
- `PreparedStatement` con `?`, `try-with-resources`, logger (nada de `System.out` ni `printStackTrace`).
- Un caso de uso = un método de Service. Cada flujo alternativo `[Na]` del diagrama = un resultado/excepción + un test.
- Service probado con `Fake*` (sin BD); `Pg*` contra PostgreSQL; Controller con MockMvc.
- **En el PR solo código y migraciones** (nada de `.md`, capturas ni documentación). Cambios fuera de las carpetas de la tabla: pedirlos antes a Benjamín.
- Si necesitas cambiar un contrato de `model/compartido`, **no lo cambies**: abre un comentario en este issue y avisa a Benjamín.

## Fuera de alcance
- Gráficos avanzados; exportar PDF/Excel.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-04-cu19-cu20-reportes-supervision`
