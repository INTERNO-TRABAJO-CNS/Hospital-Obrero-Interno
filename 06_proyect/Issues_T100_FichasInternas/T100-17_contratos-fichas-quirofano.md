# [T100-17][Java][Transversal] Contratos compartidos, especialidades internas y migración de fichas internas (20 tablas) y quirófanos

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-17-contratos-fichas-quirofano` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Nivel del modelo en V | Diseño detallado (se verifica con las pruebas unitarias de esta tarjeta y en T100-31) |
| Casos de uso | transversal (CU-21…CU-25) |
| Requisitos | RF-21…RF-25 · RNF-04 · RNF-09 |
| Depende de / Bloquea | T100-00, T100-01 / Bloquea: T100-19…T100-25, T100-30 |

## Objetivo
Igual que T100-00, pero para lo nuevo: dejar en `model/compartido` los enums, records, interfaces y *fakes* de fichas internas y quirófanos, y la migración de base de datos, para que cada tarjeta siguiente se programe **sin esperar a otra**.

## Reglas de negocio que debes cumplir
- `TipoEspecialidad` pasa de 3 a 13 valores. Las 3 **básicas** (las pide el paciente): CONSULTORIO_FAMILIAR (= Médico General), ODONTOLOGIA, PEDIATRIA. Las 10 **internas** (solo por derivación) con su duración de ficha en minutos: CARDIOLOGIA 20–40, NEFROLOGIA 20–40, NEUMOLOGIA 20–30, ENDOCRINOLOGIA 20–40, GASTROENTEROLOGIA 20–30, HEMATOLOGIA 20–40, INFECTOLOGIA 30–45, DERMATOLOGIA 15–30, MEDICINA_TRABAJO 20–40, TERAPIA_INTENSIVA 30–60.
- Solo derivan: CONSULTORIO_FAMILIAR y PEDIATRIA (nunca ODONTOLOGIA). TERAPIA_INTENSIVA: **solo urgencias** (reanimación), sin agenda.
- Operan en quirófano (duración de cirugía): GASTROENTEROLOGIA 30–120 min, HEMATOLOGIA 30–120, DERMATOLOGIA 15–120. Cardiología, Nefrología, Neumología y Endocrinología: `// PENDIENTE PV-20` (más de 12 h; se harán si hay tiempo). Infectología, Medicina del Trabajo y Terapia Intensiva **no** operan.
- Cupo diario de cada especialista: igual que en la consulta general (5 a 7 fichas, regla ya existente en `Medico`). Terapia Intensiva no usa cupo.
- Prioridad (una sola regla para todo): **urgencia > gravedad (ALTA > MEDIA > BAJA) > adulto mayor (≥ 60 años) > orden de llegada**.
- Edad: Pediatría solo **menores de 10 años**; adulto mayor = **60 años o más** (Ley 369 del Adulto Mayor).
- Fichas internas: **20 tablas** con la misma estructura en el esquema `fichas_internas` (10 especialidades × general y pediátrica), separadas de las citas comunes. Sin confirmación de asistencia. Estados: EN_ESPERA, ASIGNADA, ATENDIDA, CANCELADA (solo «no se presentó»; el horario **no** se reasigna).
- Quirófanos: **una sola tabla** `quirofano.cirugia`; cada quirófano pertenece a **una** especialidad (no se comparten). La BD impide cualquier choque con `EXCLUDE` (quirófano + limpieza, cirujano y paciente).
- Una ficha/cirugía nunca se borra.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/diagramasEstado/ED-08_FichaInterna/…`, `ED-09_Cirugia/…` y `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio/ARQ-03_ModeloDominio.puml` *(T100-16)*
- `06_proyect/Issues_T100/T100-00_base-contratos-flutter.md` (mismo estilo de contratos y fakes)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/TipoEspecialidad.java` | 13 valores con `duracionMin/Max`, `categoria` (BASICA/INTERNA) y rango de cirugía (0,0 si no opera). Métodos: `esBasica()`, `esInterna()`, `puedeDerivar()`, `soloUrgencia()`, `operaEnQuirofano()`, `cirugiaMinMinutos()`, `cirugiaMaxMinutos()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/Gravedad.java, EstadoFichaInterna.java, EstadoCirugia.java` | enums (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/PrioridadClinica.java, ComparadorPrioridad.java` | record + `Comparator` con la regla de prioridad |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/ReglasEdad.java, EdadPaciente.java` | constantes 10 y 60; interfaz que implementa Matías en T100-26 |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/FichaInternaDTO.java, FichaInternaRepository.java` | record e interfaz (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/QuirofanoDTO.java, CirugiaDTO.java, CirugiaRepository.java` | records e interfaz |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/OcupacionAgenda.java` | interfaz «¿el paciente / el médico está ocupado entre inicio y fin?» mirando citas comunes, 20 tablas y cirugías. El `Pg*` lo hace T100-24 |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/FichaInternaEventos.java, CambioFichaDTO.java, CambioFichaRepository.java` | eventos en tiempo real (WebSocket en T100-25) y auditoría |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgFichaInternaRepository.java` | elige la tabla con `especialidad` + `pediatrica` (nombre de tabla tomado de una lista fija, **nunca** del usuario); lecturas por la vista `v_todas` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgCirugiaRepository.java, PgCambioFichaRepository.java` | `store` devuelve `false` si salta el `EXCLUDE` (SQLState `23P01`) o el UNIQUE |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__fichas_internas_quirofano.sql` | ver «Esquema de base de datos» en el paso 4. Coordinar el número de versión con Benjamín al integrar |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/compartido/FakeFichaInternaRepository.java, FakeCirugiaRepository.java, FakeOcupacionAgenda.java, FakeEdadPaciente.java, FakeFichaInternaEventos.java, FakeCambioFichaRepository.java` | en memoria, con listas públicas; `FakeCirugiaRepository.store` rechaza los mismos choques que la BD |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/compartido/ComparadorPrioridadTest.java, ReglasEdadTest.java, TipoEspecialidadTest.java, FakeCirugiaRepositoryTest.java` | pruebas unitarias |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/infrastructure/pg/MigracionFichasInternasIT.java` | existen las 20 tablas y la vista; el `EXCLUDE` rechaza un choque |

## Contrato (copiar las firmas tal cual)
```java
// ---- model/compartido ----
public enum Gravedad { ALTA, MEDIA, BAJA }
public enum EstadoFichaInterna { EN_ESPERA, ASIGNADA, ATENDIDA, CANCELADA }
public enum EstadoCirugia { PROGRAMADA, REALIZADA, CANCELADA }

public record PrioridadClinica(boolean urgencia, Gravedad gravedad, boolean adultoMayor, long ordenLlegada) {}
public final class ComparadorPrioridad implements Comparator<PrioridadClinica> {
    // menor = atiende primero: urgencia -> ALTA,MEDIA,BAJA -> adultoMayor -> ordenLlegada
    public static final ComparadorPrioridad INSTANCIA = new ComparadorPrioridad();
}
public final class ReglasEdad {
    public static final int EDAD_LIMITE_PEDIATRIA = 10;   // admitido si edad < 10
    public static final int EDAD_ADULTO_MAYOR = 60;       // adulto mayor si edad >= 60
    public static boolean admitidoEnPediatria(int edad);
    public static boolean esAdultoMayor(int edad);
}
public interface EdadPaciente { int edadEn(long pacienteId, LocalDate fecha); }   // años cumplidos

public record FichaInternaDTO(long fichaId, TipoEspecialidad especialidad, boolean pediatrica,
        long pacienteId, String nombrePaciente, String numeroAfiliado,
        Long citaOrigenId /*null si fue urgencia sin cita*/, long medicoDerivanteId,
        Long medicoId, String nombreMedico, Integer consultorio,      // null mientras EN_ESPERA
        LocalDate fecha, LocalTime hora, int duracionMin,             // fecha/hora null mientras EN_ESPERA
        Gravedad gravedad, boolean urgencia, boolean adultoMayor, long ordenLlegada,
        String motivo, String observaciones, EstadoFichaInterna estado, LocalDateTime creadaEn) {
    public PrioridadClinica prioridad() { return new PrioridadClinica(urgencia, gravedad, adultoMayor, ordenLlegada); }
}
public interface FichaInternaRepository {
    long nextIdentity();                                   // secuencia global: el id es único en las 20 tablas
    boolean store(FichaInternaDTO f);                      // insert o update en SU tabla; false si viola UNIQUE
    Optional<FichaInternaDTO> porId(long fichaId);
    boolean citaYaDerivada(long citaOrigenId);
    boolean pacienteTieneFichaActiva(long pacienteId);     // EN_ESPERA o ASIGNADA en cualquier tabla
    List<FichaInternaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha);           // general + pediátrica, sin CANCELADA
    int contarDelMedicoEnFecha(long medicoId, LocalDate fecha);                       // sin CANCELADA
    List<FichaInternaDTO> asignadasDesde(TipoEspecialidad esp, LocalDateTime desde);  // candidatas a desplazar
    List<FichaInternaDTO> enEspera(TipoEspecialidad esp);                             // ordenadas por prioridad
    List<FichaInternaDTO> cola(TipoEspecialidad esp, boolean pediatrica, LocalDate desde); // EN_ESPERA + ASIGNADA
    List<FichaInternaDTO> deTabla(TipoEspecialidad esp, boolean pediatrica, LocalDate fecha);
    List<FichaInternaDTO> delPaciente(long pacienteId);
    boolean cambiarEstado(long fichaId, EstadoFichaInterna desde, EstadoFichaInterna hasta); // atómico
}

public record QuirofanoDTO(int quirofanoId, String nombre, TipoEspecialidad especialidad,
                           LocalTime apertura, LocalTime cierre, int limpiezaMin /*30..45*/, boolean activo) {}
public record CirugiaDTO(long cirugiaId, long fichaInternaId, long pacienteId, String nombrePaciente,
        long cirujanoId, String nombreCirujano, int quirofanoId, TipoEspecialidad especialidad,
        LocalDateTime inicio, int duracionMin, int limpiezaMin,
        Gravedad gravedad, boolean urgencia, boolean adultoMayor, long ordenLlegada, EstadoCirugia estado) {
    public LocalDateTime fin() { return inicio.plusMinutes(duracionMin); }
    public LocalDateTime finConLimpieza() { return fin().plusMinutes(limpiezaMin); }
    public PrioridadClinica prioridad() { return new PrioridadClinica(urgencia, gravedad, adultoMayor, ordenLlegada); }
}
public interface CirugiaRepository {
    long nextIdentity();
    boolean store(CirugiaDTO c);                           // false si la BD rechaza por choque (EXCLUDE)
    Optional<CirugiaDTO> porId(long cirugiaId);
    List<CirugiaDTO> delQuirofanoEntre(int quirofanoId, LocalDateTime desde, LocalDateTime hasta);  // sin CANCELADA
    List<CirugiaDTO> delCirujanoEntre(long cirujanoId, LocalDateTime desde, LocalDateTime hasta);
    List<CirugiaDTO> delDia(LocalDate fecha);
    List<CirugiaDTO> delPaciente(long pacienteId);
    boolean cambiarEstado(long cirugiaId, EstadoCirugia desde, EstadoCirugia hasta);
    boolean reprogramar(long cirugiaId, LocalDateTime nuevoInicio, int quirofanoId);  // solo PROGRAMADA
}
public interface OcupacionAgenda {
    boolean pacienteOcupado(long pacienteId, LocalDateTime inicio, LocalDateTime fin);  // citas + fichas + cirugías
    boolean medicoOcupado(long medicoId, LocalDateTime inicio, LocalDateTime fin);
}
public interface FichaInternaEventos { void fichaCambio(FichaInternaDTO f); void cirugiaCambio(CirugiaDTO c); }
public record CambioFichaDTO(long cambioId, String objeto /*FICHA|CIRUGIA*/, long objetoId, String accion,
                             String usuario, LocalDateTime fechaHora, String motivo) {}
public interface CambioFichaRepository { void registrar(CambioFichaDTO c); List<CambioFichaDTO> de(String objeto, long objetoId); }
```

## Paso a paso
1. Rama `feature/T100-17-contratos-fichas-quirofano`.
2. Ampliar `TipoEspecialidad` **sin cambiar** los 3 valores ni los métodos existentes (el catálogo de T100-01 sigue funcionando). Correr `./mvnw test`: lo que ya existía debe seguir en verde.
3. Crear enums, records e interfaces tal cual el contrato, y sus `Fake*` en `src/test`.
4. Migración: (a) insertar las 10 especialidades en `recursos.especialidad`; (b) `CREATE SCHEMA fichas_internas`, secuencia `fichas_internas.ficha_id_seq` y las 20 tablas `fichas_internas.<especialidad>` y `fichas_internas.<especialidad>_pediatria` con columnas `id, paciente_id, cita_origen_id, medico_derivante_id, medico_id, consultorio_id, fecha, hora, duracion_min, gravedad, urgencia, adulto_mayor, orden_llegada, motivo, observaciones, estado, creada_en`, `CHECK` de estados y gravedad y `CHECK ((estado = 'EN_ESPERA') = (fecha IS NULL))` (se pueden crear con un bloque `DO` que recorra la lista fija); (c) tabla `fichas_internas.derivacion(cita_origen_id PK, ficha_id)` = una sola derivación por cita; (d) vista `fichas_internas.v_todas` (`UNION ALL` de las 20 con columnas `especialidad` y `pediatrica`); (e) `CREATE EXTENSION IF NOT EXISTS btree_gist`, esquema `quirofano`, tabla `quirofano.quirofano(id, nombre, especialidad, apertura, cierre, limpieza_min CHECK 30–45, activo, UNIQUE(id, especialidad))` y `quirofano.cirugia` con FK compuesta `(quirofano_id, especialidad)`, columnas generadas `fin` y `fin_con_limpieza` y tres `EXCLUDE USING gist … WHERE (estado <> 'CANCELADA')`: `(quirofano_id =, tsrange(inicio, fin_con_limpieza) &&)`, `(cirujano_id =, tsrange(inicio, fin) &&)`, `(paciente_id =, tsrange(inicio, fin) &&)`; (f) `fichas_internas.cambio` para auditoría.
5. `PgFichaInternaRepository`, `PgCirugiaRepository`, `PgCambioFichaRepository`.
6. Pruebas y PR a T100. Avisar al grupo: desde aquí T100-19…30 pueden empezar.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `ComparadorPrioridadTest` | urgencia gana a ALTA; ALTA > MEDIA > BAJA; con igual gravedad gana ≥ 60; luego orden de llegada |
| `ReglasEdadTest` | 9 → pediatría sí, 10 → no; 59 → no adulto mayor, 60 → sí |
| `TipoEspecialidadTest` | solo CF y PED derivan; TI solo urgencia; operan solo GASTRO, HEMATO y DERMA; las 3 básicas no cambian |
| `FakeCirugiaRepositoryTest` | choque de quirófano dentro de la limpieza → false; mismo cirujano o paciente solapado → false; cirugía CANCELADA no choca |
| `MigracionFichasInternasIT` | 20 tablas + `v_todas`; segunda derivación de la misma cita → violación; `EXCLUDE` → SQLState 23P01 |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-17-contratos-fichas-quirofano
# ... trabajar, commits pequeños: feat(T100-17): descripción
git push -u origin feature/T100-17-contratos-fichas-quirofano
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
- Lógica de asignación (T100-20) y de quirófano (T100-23).
- Implementación `Pg` de `OcupacionAgenda` (T100-24) y de `EdadPaciente` (T100-26).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-17-contratos-fichas-quirofano`
