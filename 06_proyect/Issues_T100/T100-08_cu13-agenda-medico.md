# [T100-08][Java+Flutter][CU-13] Agenda médica: citas del día y selector de otras fechas

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Alejandro |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-08-cu13-agenda-medico` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-13 |
| Requisitos | RF-13 · RU-08 |
| Depende de / Bloquea | T100-00 / Bloquea: — |

## Objetivo
El médico autenticado ve **solo sus** citas de hoy (cantidad, número de ficha y hora) y puede elegir otra fecha. Solo lectura.

## Reglas de negocio que debes cumplir
- Solo rol `MEDICO`; el `medicoId` sale del usuario autenticado, nunca del request.
- Por defecto: fecha = hoy (`Reloj`).
- Se ordena por hora; las CANCELADAS se muestran marcadas.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-13_ConsultarAgendaMedica (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/AgendaMedicaService.java` | `agendaDelMedico(long medicoId, LocalDate fecha)` → `AgendaDiaDTO` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/AgendaDiaDTO.java, FichaAgendaDTO.java` | records |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/AgendaMedicaController.java` | GET `/api/v1/medico/agenda?fecha=YYYY-MM-DD` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/operaciones/AgendaMedicaServiceTest.java` | con `FakeCitaRepository`, `RelojFijo` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/controller/AgendaMedicaControllerTest.java` | MockMvc: 403 si el rol no es MEDICO |
| `04_imp/frontend/lib/views/medico/agenda_view.dart` | **celular y escritorio**: encabezado «N citas hoy», lista (ficha · hora · paciente), botón «Otras fechas» |
| `04_imp/frontend/lib/services/agenda_service.dart, controllers/agenda_controller.dart, models/agenda/*.dart` | con `FakeAgendaService` |

## Contrato (copiar las firmas tal cual)
```java
public class AgendaMedicaService {
    public AgendaMedicaService(CitaRepository citas, Reloj reloj) {}
    public AgendaDiaDTO agendaDelMedico(long medicoId, LocalDate fechaONull);
}
public record AgendaDiaDTO(LocalDate fecha, int cantidad, List<FichaAgendaDTO> fichas) {}
public record FichaAgendaDTO(long citaId, int numeroFicha, LocalTime hora, String nombrePaciente, EstadoCita estado) {}
```

## Paso a paso
1. Rama `feature/T100-08-cu13-agenda`.
2. Service + test (un test por `[Na]` de `CU-13_*.puml`).
3. Controller delgado.
4. Pantalla Flutter con `LayoutBuilder`: en celular lista; en escritorio lista + detalle.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `agenda_hoy_soloDelMedico` |  |
| `agenda_sinCitas_cantidad0` | [3a] |
| `agenda_otraFecha` |  |
| `controller_rolPaciente_403` | [1a] |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-08-cu13-agenda-medico
# ... trabajar, commits pequeños: feat(T100-08): descripción
git push -u origin feature/T100-08-cu13-agenda-medico
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
- Ver historial del paciente (T100-12).
- Modificar citas.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-08-cu13-agenda-medico`
