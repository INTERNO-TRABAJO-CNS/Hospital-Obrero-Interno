# [T100-10][Java+Flutter][CU-12] Confirmar asistencia (enfermería) y registrar fin de atención (médico)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Alejandro |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-10-cu12-confirmar-asistencia-fin` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-12 |
| Requisitos | RF-12 · RU-09 · RU-10 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-14 |

## Objetivo
Enfermería confirma que el paciente llegó (PROGRAMADA → ASISTENCIA_CONFIRMADA) hasta 30 min antes de la cita. El médico registra el fin (ASISTENCIA_CONFIRMADA → ATENDIDA) a través del puerto `FinalizacionCita`.

## Reglas de negocio que debes cumplir
- Confirmar: solo rol ENFERMERIA, cita **de hoy**, `ahora <= hora − 30 min`, estado PROGRAMADA.
- Pasado T-30 la cita ya fue cancelada automáticamente (T100-11): responder `FUERA_DE_PLAZO`.
- Finalizar: solo MEDICO dueño de la cita, estado ASISTENCIA_CONFIRMADA. El médico **no** inicia la cita.
- Cada cambio: `CambioCitaRepository.registrar` + `AgendaEventos.citaCambio`.
- Usar `CitaRepository.cambiarEstado(desde, hasta)` (atómico).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-12_ActualizarEstadoCita.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/ActualizacionEstadoService.java` | implementa `FinalizacionCita`; método `confirmarAsistencia(long citaId, UsuarioAutenticado u)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/ResultadoConfirmacion.java` | enum CONFIRMADA, NO_ENCONTRADA, ROL_NO_AUTORIZADO, NO_ES_DE_HOY, FUERA_DE_PLAZO, ESTADO_INVALIDO |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/EstadoCitaController.java` | POST `/api/v1/citas/{id}/confirmar-asistencia`, POST `/api/v1/citas/{id}/finalizar` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/operaciones/ActualizacionEstadoServiceTest.java` | con `RelojFijo` y Fakes |
| `04_imp/frontend/lib/views/enfermeria/confirmar_asistencia_view.dart` | escritorio: lista de fichas del día PROGRAMADAS con botón «Confirmó llegada»; cuenta regresiva a T-30 |
| `04_imp/frontend/lib/services/estado_cita_service.dart, controllers/confirmar_asistencia_controller.dart` |  |

## Contrato (copiar las firmas tal cual)
```java
public class ActualizacionEstadoService implements FinalizacionCita {
    public ActualizacionEstadoService(CitaRepository c, CambioCitaRepository cambios, AgendaEventos eventos, Reloj reloj) {}
    public ResultadoConfirmacion confirmarAsistencia(long citaId, UsuarioAutenticado u);
    @Override public ResultadoFinalizacion finalizar(long citaId, long medicoId);
}
```

## Paso a paso
1. Rama `feature/T100-10-cu12-estado`.
2. Service + tests con `RelojFijo` (hora fija para probar T-30 exacto y T-29).
3. Un test por `[Na]` de `CU-12_*.puml`.
4. Controller, pantalla Flutter.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `confirmar_aT31min_ok` |  |
| `confirmar_aT29min_fueraDePlazo` |  |
| `confirmar_citaDeOtroDia_rechaza` |  |
| `confirmar_rolMedico_rechaza` |  |
| `finalizar_desdeConfirmada_atendida` |  |
| `finalizar_desdeProgramada_rechaza` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-10-cu12-confirmar-asistencia-fin
# ... trabajar, commits pequeños: feat(T100-10): descripción
git push -u origin feature/T100-10-cu12-confirmar-asistencia-fin
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
- Cancelación automática (T100-11).
- Receta (T100-14).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-10-cu12-confirmar-asistencia-fin`
