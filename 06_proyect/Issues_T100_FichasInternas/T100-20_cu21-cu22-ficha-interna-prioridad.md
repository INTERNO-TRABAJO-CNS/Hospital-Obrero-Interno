# [T100-20][Java][CU-21/22] Generar ficha interna y asignarla por cola de prioridad (urgencia, desplazamiento, en espera y Terapia Intensiva)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-20-cu21-cu22-ficha-interna-prioridad` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 8 |
| Nivel del modelo en V | Diseño detallado → unitarias (+ integración en T100-31) |
| Casos de uso | CU-21, CU-22 |
| Requisitos | RF-21 · RF-22 · RNF-03 · RNF-04 |
| Depende de / Bloquea | T100-17, T100-02 (bloqueo de agenda), T100-03 (`CalendarioHabil`) / Bloquea: T100-21, T100-22 |

## Objetivo
Durante la consulta, el médico general o el pediatra pide una ficha interna para el paciente. El paciente no hace nada. El sistema la guarda en la tabla de la especialidad y le asigna médico, día y hora según la prioridad. Responde el **ID del ticket** con fecha, hora, médico y consultorio, o el motivo del rechazo («ID / no» de la pizarra).

## Reglas de negocio que debes cumplir
- **Quién:** solo `MEDICO` de CONSULTORIO_FAMILIAR o PEDIATRIA (`MEDICO_NO_DERIVA`, 403). Destino: solo especialidades internas (`ESPECIALIDAD_NO_INTERNA`).
- **Desde una cita:** la cita es del médico, de **hoy** y está ASISTENCIA_CONFIRMADA o ATENDIDA (`CITA_NO_VALIDA`). **Una sola derivación por cita** (`CITA_YA_DERIVADA`, 409).
- **Urgencia:** el médico general puede derivar sin cita, buscando al paciente por su número de afiliado (`citaOrigenId = null`).
- Terapia Intensiva solo con urgencia (`TERAPIA_SOLO_URGENCIA`). Pediatría: el paciente debe tener menos de 10 años (`EDAD_NO_PEDIATRICA`). Motivo obligatorio, de 1 a 500 caracteres (`MOTIVO_INVALIDO`).
- Un paciente solo puede tener **una** ficha interna activa (EN_ESPERA o ASIGNADA) → `PACIENTE_CON_FICHA_ACTIVA` (`// PENDIENTE PV-21`).
- Tabla: la del destino; es **pediátrica** si quien deriva es de PEDIATRIA. `adultoMayor = edad ≥ 60` a la fecha de hoy. `ordenLlegada = nextIdentity()`.
- **Prioridad:** `ComparadorPrioridad` (urgencia > gravedad > ≥ 60 > llegada). La gravedad solo la ve el médico general: nunca va en el ticket ni en la notificación.
- **Asignación normal:** días hábiles desde mañana, hasta 15 días hábiles. En cada día, médicos de la especialidad (primero el que tiene menos fichas; empate → menor id), franjas dentro de su horario con la duración de su ficha. Se exige: cupo < `cupoMaximoDia` (contando general + pediátrica), médico libre y paciente libre (`OcupacionAgenda`: cualquier cruce cuenta) y bloqueo `AgendaRepository.intentarBloquear(medicoId, fecha)`.
- **Sin hueco en 15 días hábiles:** si hay una ficha ASIGNADA de menor prioridad (no urgente, aún no empezada), la nueva toma su lugar y la desplazada pasa a **EN_ESPERA**. Si no hay ninguna de menor prioridad, la nueva queda EN_ESPERA.
- **Urgencia (no TI):** hueco **hoy** desde ahora. Si no hay, desplaza a la ficha de hoy de menor prioridad que todavía no empezó. La desplazada busca el siguiente hueco libre (sin desplazar a nadie más); si no lo hay, pasa a EN_ESPERA.
- **Terapia Intensiva:** sin agenda ni cupo. Se asigna **ya**, al médico de TI que se libere antes; hora = máx(ahora, fin de su reanimación actual) y duración = la de su ficha (30–60).
- **EN_ESPERA:** `procesarEnEspera()` corre al inicio de cada día hábil (06:00) y, en orden de prioridad, busca hueco libre **sin desplazar** a nadie.
- Guardar ficha + derivación + `CambioFicha` en **una transacción**. Después del commit: `NotificacionPublicador.publicar` (tipos `FICHA_INTERNA_ASIGNADA`, `FICHA_INTERNA_EN_ESPERA`, `FICHA_INTERNA_REPROGRAMADA`; `citaId = null`) y `FichaInternaEventos.fichaCambio`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-21_GenerarFichaInterna/CU-21_GenerarFichaInterna.puml` *(lo crea T100-16)*
- `03_des/01-diagramaCompor/CU-22_AsignarFichaInternaPorPrioridad/CU-22_AsignarFichaInternaPorPrioridad.puml` *(lo crea T100-16)*
- `03_des/diagramasEstado/ED-08_FichaInterna/ED-08_FichaInterna.puml`
- `06_proyect/Issues_T100/T100-03_cu05-cu07-asignacion-registro.md` (mismo patrón de búsqueda)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/fichasinternas/SolicitudFichaInterna.java, TicketFichaInternaDTO.java` | records (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/fichasinternas/FichaInternaService.java` | CU-21: `generar(...)` (valida, crea y llama al asignador) y `ticket(fichaId, u)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/fichasinternas/AsignadorFichaInterna.java, ResultadoAsignacionFicha.java` | CU-22: `asignar(FichaInternaDTO)` y `procesarEnEspera()`; enum ASIGNADA, ASIGNADA_DESPLAZANDO, EN_ESPERA |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/planificador/ProcesadorFichasEnEspera.java` | `@Scheduled(cron = "0 0 6 * * MON-FRI")` → `procesarEnEspera()` (no hace nada si el día no es hábil) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/FichaInternaController.java` | `POST /api/v1/medico/fichas-internas` → 201 `TicketFichaInternaDTO`; `GET /api/v1/fichas-internas/{id}` → fecha, hora, médico, especialidad, consultorio, estado (médico que derivó, especialista asignado, paciente dueño o administrador) |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/fichasinternas/FichaInternaServiceTest.java, AsignadorFichaInternaTest.java` | con los Fakes de T100-00 y T100-17 y `RelojFijo` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/controller/FichaInternaControllerTest.java` | MockMvc: 201, 403, 409, 422 |

## Contrato (copiar las firmas tal cual)
```java
public record SolicitudFichaInterna(Long citaOrigenId, Long pacienteId /*solo si urgencia sin cita*/,
        TipoEspecialidad especialidad, Gravedad gravedad, boolean urgencia, String motivo) {}
public record TicketFichaInternaDTO(long fichaId, EstadoFichaInterna estado, TipoEspecialidad especialidad,
        String nombreMedico, Integer consultorio, LocalDate fecha, LocalTime hora) {}   // SIN gravedad

public class FichaInternaService {
    public FichaInternaService(FichaInternaRepository fichas, CitaRepository citas, MedicoRepository medicos,
            EdadPaciente edades, AsignadorFichaInterna asignador, CambioFichaRepository cambios, Reloj reloj) {}
    public TicketFichaInternaDTO generar(SolicitudFichaInterna s, UsuarioAutenticado medico);  // DominioException con los códigos
    public TicketFichaInternaDTO ticket(long fichaId, UsuarioAutenticado u);
}
public class AsignadorFichaInterna {
    public AsignadorFichaInterna(FichaInternaRepository fichas, MedicoRepository medicos, HorarioRepository horarios,
            OcupacionAgenda ocupacion, AgendaRepository agenda, CalendarioHabil cal, CambioFichaRepository cambios,
            NotificacionPublicador notif, FichaInternaEventos eventos, Reloj reloj) {}
    public ResultadoAsignacionFicha asignar(FichaInternaDTO fichaEnEspera);
    public int procesarEnEspera();
}
```

## Paso a paso
1. Rama `feature/T100-20-cu21-cu22-ficha-interna-prioridad`.
2. Validaciones de `generar` (un test por cada código de error).
3. Asignación normal → urgencia → desplazamiento → Terapia Intensiva → `procesarEnEspera`, cada una con sus tests.
4. `BLOQUEADA_POR_OTRO` o `store == false` ⇒ probar la siguiente combinación (no fallar).
5. Controller y planificador.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `generar_odontologo_MEDICO_NO_DERIVA` | solo CF y PED |
| `generar_citaAjena_o_deAyer_CITA_NO_VALIDA` | cita del médico y de hoy |
| `generar_dosVecesMismaCita_CITA_YA_DERIVADA` | una por cita |
| `generar_pediatra_paciente10anios_EDAD_NO_PEDIATRICA` | menores de 10 |
| `generar_terapiaSinUrgencia_rechaza` | TI solo urgencia |
| `asigna_primerHuecoLibre_respetaCupo7_generalMasPediatrica` | cupo compartido por las dos tablas |
| `asigna_pacienteConCitaComunCruzada_saltaEseHorario` | cualquier cruce cuenta |
| `sinHueco15dias_desplazaMenorPrioridad_desplazadaEnEspera` | desplazamiento |
| `sinHueco_yTodasMayorPrioridad_quedaEnEspera` | no desplaza a una de mayor prioridad |
| `urgencia_hoyDesplazaNoUrgenteNoEmpezada` | urgencia |
| `terapiaIntensiva_asignaAhora_medicoQueSeLiberaAntes` | TI |
| `procesarEnEspera_ordenPrioridad_sinDesplazar` | cola EN_ESPERA |
| `ticket_noIncluyeGravedad` | privacidad de la prioridad |
| `notifica_soloDespuesDelCommit` | RNF-03 |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-20-cu21-cu22-ficha-interna-prioridad
# ... trabajar, commits pequeños: feat(T100-20): descripción
git push -u origin feature/T100-20-cu21-cu22-ficha-interna-prioridad
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
- Pantallas (T100-21).
- Cirugías (T100-23).
- Texto e impresión del ticket (T100-30).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-20-cu21-cu22-ficha-interna-prioridad`
