# [T100-23][Java+Nuxt][CU-24] Programar cirugía en quirófano sin choques de horario (limpieza 30–45 min y urgencia que desplaza)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-23-cu24-programar-cirugia-quirofano` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 8 |
| Nivel del modelo en V | Diseño detallado → unitarias (+ concurrencia en T100-31) |
| Casos de uso | CU-24 |
| Requisitos | RF-24 · RNF-04 · RNF-09 |
| Depende de / Bloquea | T100-17, T100-19, T100-22 / Bloquea: T100-25 |

## Objetivo
Si el daño es grave, el especialista que atendió la ficha interna programa la cirugía: indica cuánto cree que durará (según qué tan problemático es el caso), la gravedad y si es urgente. El sistema **propone** quirófano y hora sin ningún choque, y el especialista **confirma**.

## Reglas de negocio que debes cumplir
- Solo el especialista que **atendió** la ficha (estado ATENDIDA); él será el cirujano.
- La especialidad debe `operaEnQuirofano()` (`ESPECIALIDAD_NO_OPERA`). La duración debe estar en el rango de cirugía de la especialidad (`DURACION_FUERA_DE_RANGO`).
- Solo quirófanos activos **de su especialidad** (no se comparten).
- **Cero choques:** quirófano `[inicio, fin + limpieza)` sin cruce con otra cirugía y su limpieza; cirujano `[inicio, fin)` libre de cirugías **y** de consultas (`OcupacionAgenda.medicoOcupado`); paciente libre (`OcupacionAgenda.pacienteOcupado`). Limpieza = la del quirófano (30–45 min).
- Normal: días hábiles desde mañana, hasta 15 días hábiles, dentro de `apertura` y `cierre` (`fin + limpieza ≤ cierre`). Urgencia: desde ahora, también fuera de horario (`// PENDIENTE PV-19`).
- Sin hueco: desplaza la cirugía PROGRAMADA de **menor prioridad** (no urgente, aún no empezada) si al quitarla entra la nueva. La desplazada se reprograma al siguiente hueco libre (búsqueda de hasta 60 días hábiles, sin desplazar a otra). Urgente sin hueco hoy: igual, sobre las de hoy.
- Dos pasos («no muy automático»): `proponer` **no** guarda nada; `confirmar` guarda en una transacción. Si la BD rechaza por el `EXCLUDE` → `CHOQUE_DE_HORARIO` (409) y la web pide proponer de nuevo.
- Realizada: PROGRAMADA → REALIZADA (solo el cirujano). No se presentó: → CANCELADA, sin reasignar el horario.
- Notificar `CIRUGIA_PROGRAMADA` / `CIRUGIA_REPROGRAMADA` después del commit.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-24_ProgramarCirugia/CU-24_ProgramarCirugia.puml` *(lo crea T100-16)*
- `03_des/diagramasEstado/ED-09_Cirugia/ED-09_Cirugia.puml`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/quirofano/ProgramacionCirugiaService.java` | `proponer`, `confirmar`, `marcarRealizada`, `marcarNoSePresento` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/quirofano/BuscadorHuecoQuirofano.java` | cálculo puro del primer hueco (sin BD), fácil de probar |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/quirofano/SolicitudCirugia.java, PropuestaCirugiaDTO.java` | records (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/CirugiaController.java` | `POST /api/v1/especialista/cirugias/propuesta`, `POST /api/v1/especialista/cirugias`, `GET /api/v1/especialista/cirugias?fecha=`, `POST …/{id}/realizada`, `POST …/{id}/no-se-presento` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/quirofano/BuscadorHuecoQuirofanoTest.java, ProgramacionCirugiaServiceTest.java` | con `FakeCirugiaRepository`, `FakeOcupacionAgenda`, `RelojFijo` |
| `04_imp/frontend-web/pages/especialista/cirugia/[fichaId].vue` | duración estimada, gravedad, urgencia → «Proponer» → muestra quirófano, inicio, fin y «limpieza hasta …» (y si desplaza a otra) → «Confirmar» |
| `04_imp/frontend-web/pages/especialista/cirugias.vue` | cirugías del día por quirófano (línea de tiempo con la limpieza en gris) |
| `04_imp/frontend-web/stores/cirugia.ts, services/cirugiaService.ts + fake, models/cirugia.ts, tests/cirugia.test.ts` |  |

## Contrato (copiar las firmas tal cual)
```java
public record SolicitudCirugia(long fichaInternaId, int duracionMin, Gravedad gravedad, boolean urgencia) {}
public record PropuestaCirugiaDTO(long fichaInternaId, int quirofanoId, String quirofano, LocalDateTime inicio,
        LocalDateTime fin, LocalDateTime finLimpieza, Long desplazaCirugiaId /*null si no desplaza*/) {}

public class ProgramacionCirugiaService {
    public ProgramacionCirugiaService(CirugiaRepository cirugias, FichaInternaRepository fichas, QuirofanoRepository quirofanos,
            OcupacionAgenda ocupacion, BuscadorHuecoQuirofano buscador, CalendarioHabil cal, EdadPaciente edades,
            CambioFichaRepository cambios, NotificacionPublicador notif, FichaInternaEventos eventos, Reloj reloj) {}
    public PropuestaCirugiaDTO proponer(SolicitudCirugia s, long cirujanoId);
    public CirugiaDTO confirmar(PropuestaCirugiaDTO p, SolicitudCirugia s, long cirujanoId);
    public void marcarRealizada(long cirugiaId, long cirujanoId);
    public void marcarNoSePresento(long cirugiaId, long cirujanoId);
}
```

## Paso a paso
1. Rama `feature/T100-23-cu24-programar-cirugia-quirofano`.
2. `BuscadorHuecoQuirofano` primero (lógica pura y muchos tests).
3. `proponer` → `confirmar` → desplazamiento → realizada / no se presentó.
4. Controller.
5. Pantallas Nuxt.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `hueco_respetaLimpiezaDelQuirofano` | la siguiente empieza ≥ fin + limpieza |
| `hueco_cirujanoConConsultaEseHorario_lo_salta` | cruce con su agenda |
| `hueco_pacienteConFichaOCita_lo_salta` | cruce del paciente |
| `hueco_soloQuirofanosDeSuEspecialidad` | no compartidos |
| `proponer_dermatologia130min_DURACION_FUERA_DE_RANGO` | 15–120 |
| `proponer_infectologia_ESPECIALIDAD_NO_OPERA` |  |
| `proponer_noGuardaNada` | dos pasos |
| `confirmar_bdRechaza_CHOQUE_DE_HORARIO` | `store == false` |
| `urgente_sinHuecoHoy_desplazaMenorPrioridad_yReprogramaDesplazada` |  |
| `noSePresento_noReasigna` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-23-cu24-programar-cirugia-quirofano
# ... trabajar, commits pequeños: feat(T100-23): descripción
git push -u origin feature/T100-23-cu24-programar-cirugia-quirofano
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
- **MVC también en la web:** `pages/` = vista, `stores/` (Pinia) = controlador de interfaz, `services/` = llamadas REST, `models/` = tipos. Una página **nunca** llama `fetch` directo: usa su *store* y el *store* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con Vitest (`npm run test`) para *stores* y *services*.
- La URL del backend sale de `NUXT_PUBLIC_API_URL`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- Cirugías de Cardiología, Nefrología, Neumología y Endocrinología (`PV-20`, si hay tiempo).
- Equipo, anestesia e insumos del quirófano.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-23-cu24-programar-cirugia-quirofano`
