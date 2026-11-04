# [T100-11][Java+Flutter][CU-11] Cancelar cita (paciente) y cancelación automática a T-30

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Alex |
| Revisor (aprueba) | Abigail |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-11-cu11-cancelar-cita-auto` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-11 |
| Requisitos | RF-11 · RU-09 · RU-10 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-07 |

## Objetivo
El paciente cancela su cita PROGRAMADA; el sistema cancela solo las citas PROGRAMADAS que a 30 minutos de su hora no tienen asistencia confirmada. La cita **no se borra**: queda CANCELADA con motivo y usuario.

## Reglas de negocio que debes cumplir
- Paciente: solo la propia y solo si está PROGRAMADA (no tras ASISTENCIA_CONFIRMADA — `// PENDIENTE PV-15`).
- Automática: cada minuto, `programadasHasta(ahora + 30 min)` → cancelar con motivo «No confirmó asistencia», usuario «Sistema».
- Cada cancelación: `CambioCitaRepository.registrar`, `NotificacionPublicador.publicar`, `AgendaEventos.citaCambio`.
- El cupo liberado **no** se reasigna automáticamente (por ahora).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-11_CancelarCita (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/CancelacionService.java` | implementa `CancelacionCita`; además `cancelarAutomaticas()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/planificador/CancelacionAutomatica.java` | `@Scheduled(fixedDelay = 60000)` que llama `cancelarAutomaticas()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/CancelacionController.java` | POST `/api/v1/citas/{id}/cancelar` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/operaciones/CancelacionServiceTest.java` | con `RelojFijo` y Fakes |
| `04_imp/frontend/lib/views/citas/dialogo_cancelar.dart` | diálogo de confirmación reutilizable (lo usa Matías en «Mis citas») |
| `04_imp/frontend/lib/services/cancelacion_service.dart` |  |

## Contrato (copiar las firmas tal cual)
```java
public class CancelacionService implements CancelacionCita {
    public CancelacionService(CitaRepository c, CambioCitaRepository cambios, NotificacionPublicador n, AgendaEventos e, Reloj reloj) {}
    @Override public ResultadoCancelacion cancelarPorPaciente(long citaId, long pacienteId);
    public int cancelarAutomaticas();   // devuelve cuántas canceló
}
```

## Paso a paso
1. Rama `feature/T100-11-cu11-cancelar`.
2. Service + tests: `[1a]` no es PROGRAMADA, `[2a]` ajena, `A1` automática.
3. Planificador y controller.
4. Diálogo Flutter.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `cancelarPorPaciente_ok_conservaCita` | estado CANCELADA y no se borra |
| `cancelarPorPaciente_ajena` |  |
| `cancelarPorPaciente_confirmada_rechaza` |  |
| `automatica_aT30_sinConfirmar_cancela` |  |
| `automatica_confirmada_noCancela` |  |
| `automatica_registraUsuarioSistema` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-11-cu11-cancelar-cita-auto
# ... trabajar, commits pequeños: feat(T100-11): descripción
git push -u origin feature/T100-11-cu11-cancelar-cita-auto
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
- Reasignar el cupo liberado.
- Reprogramar (T100-07).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-11-cu11-cancelar-cita-auto`
