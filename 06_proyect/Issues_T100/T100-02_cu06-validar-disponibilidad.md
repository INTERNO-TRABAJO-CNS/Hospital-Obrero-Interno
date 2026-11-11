# [T100-02][Java][CU-06] Validar disponibilidad de médico, consultorio, cupo y paciente (con bloqueo PostgreSQL)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-02-cu06-validar-disponibilidad` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-06 |
| Requisitos | RF-06 · RU-07 · RNF-04 |
| Depende de / Bloquea | T100-00, T100-01 / Bloquea: T100-03 |

## Objetivo
Dada una combinación candidata (médico, fecha, hora, consultorio, paciente) el sistema responde si es válida, y PostgreSQL garantiza que dos solicitudes simultáneas no se lleven el mismo hueco.

## Reglas de negocio que debes cumplir
- Médico y horario libres; consultorio libre.
- El médico tiene **menos de 7** citas ese día (sin contar CANCELADA).
- El paciente **no** tiene otra cita ese día (ni en otra especialidad).
- Sin solapamiento de intervalos (duración de la especialidad).
- Concurrencia: `SELECT … FOR UPDATE SKIP LOCKED` sobre la agenda del médico + `UNIQUE(medico_id, fecha, hora_inicio)` parcial (`estado <> 'CANCELADA'`).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-06_ValidarDisponibilidadRecursos.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/CombinacionCandidata.java` | record: pacienteId, medicoId, consultorioId, fecha, hora, duracionMin |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/ResultadoDisponibilidad.java` | enum DISPONIBLE, MEDICO_OCUPADO, CONSULTORIO_OCUPADO, MEDICO_COMPLETO, PACIENTE_YA_TIENE_CITA, SOLAPADA, BLOQUEADA_POR_OTRO |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/AgendaRepository.java` | interfaz: `boolean intentarBloquear(long medicoId, LocalDate fecha)` (false si otro proceso lo tiene), `List<CitaDTO> ocupacionDelMedico(...)`, `boolean consultorioOcupado(int consultorioId, LocalDate fecha, LocalTime inicio, LocalTime fin)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/DisponibilidadService.java` | `validar(CombinacionCandidata c)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgAgendaRepository.java` | bloqueo con `select … for update skip locked` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__cita_unica_medico_hora.sql` | índice único parcial sobre la tabla de citas |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/citas/DisponibilidadServiceTest.java` | con `FakeAgendaRepository` + `FakeCitaRepository` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgAgendaRepositoryIT.java` | prueba de integración: 2 hilos intentan bloquear el mismo médico/día; solo uno lo logra |

## Contrato (copiar las firmas tal cual)
```java
public class DisponibilidadService {
    public DisponibilidadService(AgendaRepository agenda, CitaRepository citas) {}
    public ResultadoDisponibilidad validar(CombinacionCandidata c);
}
```

## Paso a paso
1. Rama `feature/T100-02-cu06-disponibilidad`.
2. Orden de comprobación en `validar`: paciente (una cita/día) → médico completo (<7) → médico/horario libre → consultorio libre → solapamiento → bloqueo. Devolver el **primer** motivo que falle.
3. Un test por cada `[Na]` de `CU-06_*.puml`.
4. Migración + `PgAgendaRepository` + prueba de integración con dos hilos.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `validar_disponible` | camino principal |
| `validar_medicoCompleto7` | 7 citas → MEDICO_COMPLETO |
| `validar_pacienteYaTieneCita` | otra especialidad mismo día → PACIENTE_YA_TIENE_CITA |
| `validar_consultorioOcupado` |  |
| `validar_solapada` |  |
| `IT_dosHilosUnSoloGanador` | concurrencia real en PostgreSQL |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-02-cu06-validar-disponibilidad
# ... trabajar, commits pequeños: feat(T100-02): descripción
git push -u origin feature/T100-02-cu06-validar-disponibilidad
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
- Elegir cuál combinación probar: lo hace T100-03.
- Reasignación automática de cupos liberados (no se hace por ahora).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-02-cu06-validar-disponibilidad`
