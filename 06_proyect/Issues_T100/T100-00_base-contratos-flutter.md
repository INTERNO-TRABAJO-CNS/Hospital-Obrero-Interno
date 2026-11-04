# [T100-00][Java+Flutter] Preparar la rama T100, contratos compartidos y base Flutter responsive

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-00-base-contratos-flutter` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | transversal |
| Requisitos | RNF-07 · RNF-08 · RNF-03 |
| Depende de / Bloquea | Ninguna / Bloquea: todas las demás T100 |

## Objetivo
Dejar la rama `feature/T100-algoritmos-ejecucion` lista para que los otros 4 trabajen **sin depender uno del otro**: contratos compartidos (enums, DTO e interfaces) con sus *fakes*, manejo de errores y proyecto Flutter con diseño adaptable a celular, computadora y TV.

## Reglas de negocio que debes cumplir
- Los 4 estados de cita, los 4 estados de solicitud, los 4 roles y las 3 especialidades salen **solo** de `model/compartido` (nadie los redefine).
- Cada persona programa contra **interfaces** y las prueba con `Fake*` (en memoria). Los `Pg*` reales se escriben en la tarjeta de su dueño.
- Una cita **nunca se borra**: se pasa a `CANCELADA`.

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `git (sin carpeta)` | Antes de nada: `git switch feature/T100-algoritmos-ejecucion` → `git merge origin/develop` (la rama T100 es más vieja que `develop`). Resolver conflictos y `git push`. |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/EstadoCita.java` | enum PROGRAMADA, ASISTENCIA_CONFIRMADA, ATENDIDA, CANCELADA |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/EstadoSolicitud.java` | enum PENDIENTE, EN_ESPERA, ASIGNADA, SIN_CUPO |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/RolUsuario.java` | enum PACIENTE, MEDICO, ENFERMERIA, ADMINISTRADOR |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/TipoEspecialidad.java` | enum con duración mín/máx: CONSULTORIO_FAMILIAR(30,45), ODONTOLOGIA(45,60), PEDIATRIA(30,45) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/DominioException.java` | `RuntimeException` con campo `codigo` (String) para que el `@RestControllerAdvice` lo traduzca a HTTP |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/*DTO.java` | records: `CitaDTO`, `SolicitudDTO`, `NotificacionDTO`, `CambioCitaDTO`, `RecetaDTO`, `AtencionDTO`, `HistorialDTO` (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/compartido/*.java (interfaces)` | `CitaRepository`, `SolicitudRepository`, `HistorialRepository`, `CambioCitaRepository`, `NotificacionPublicador`, `AgendaEventos`, `CancelacionCita`, `FinalizacionCita`, `Reloj` y `RelojSistema` (firmas abajo) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/UsuarioAutenticado.java + ResolutorUsuario.java` | record `UsuarioAutenticado(long usuarioId, RolUsuario rol, long personaId)` y un resolutor TEMPORAL que lo lee de los headers `X-Usuario-Id`, `X-Rol`, `X-Persona-Id` (marcar `// TEMPORAL: lo reemplaza Spring Security de S3-08`) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/ManejadorErrores.java` | `@RestControllerAdvice`: `DominioException` → JSON `{codigo, mensaje}` con 400/403/404/409/422 según el código. Si S3-01 ya lo creó, **reutilizarlo**. |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/compartido/Fake*.java` | `FakeCitaRepository`, `FakeSolicitudRepository`, `FakeHistorialRepository`, `FakeCambioCitaRepository`, `FakeNotificacionPublicador`, `FakeAgendaEventos`, `FakeCancelacionCita`, `FakeFinalizacionCita`, `RelojFijo` (todos en memoria, con listas públicas para que los tests verifiquen) |
| `04_imp/frontend/ (todo el proyecto)` | `flutter create --org com.hospital --project-name cns_app 04_imp/frontend` (si aún no existe) |
| `04_imp/frontend/lib/core/responsive.dart` | `enum Dispositivo { celular, escritorio, tv }` + `Dispositivo dispositivoDe(double ancho)`: <600 celular, 600–1599 escritorio, ≥1600 o ruta `/tv` TV |
| `04_imp/frontend/lib/core/api_client.dart` | cliente HTTP único: URL base configurable (`--dart-define=API_URL`), headers `X-Usuario-Id/X-Rol/X-Persona-Id` temporales, `Future<Map> get/post`, errores → `ApiException(codigo, mensaje)` |
| `04_imp/frontend/lib/core/app_router.dart + core/app_theme.dart` | rutas con nombre (`/login`, `/paciente`, `/medico`, `/enfermeria`, `/admin`, `/tv`) y tema claro/oscuro |
| `04_imp/frontend/lib/views/shell/app_shell.dart` | marco adaptable: `NavigationBar` en celular, `NavigationRail` en escritorio, **sin menú** en TV |
| `04_imp/frontend/test/core/responsive_test.dart` | prueba de los 3 puntos de corte |

## Contrato (copiar las firmas tal cual)
```java
// ---- model/compartido (paquete com.Hospital.CNS.cns_backend.model.compartido) ----
public record CitaDTO(long citaId, long pacienteId, String nombrePaciente, String numeroAfiliado,
                      long medicoId, String nombreMedico, TipoEspecialidad especialidad, int consultorio,
                      LocalDate fecha, LocalTime hora, int numeroFicha, EstadoCita estado) {}
public record SolicitudDTO(long solicitudId, long pacienteId, TipoEspecialidad especialidad,
                           LocalDate diaSolicitado, int prioridad /*1 normal, 2 tras cancelar/reprogramar*/,
                           long ordenLlegada, EstadoSolicitud estado, Long citaId /*null hasta ASIGNADA*/) {}
public record NotificacionDTO(long notificacionId, long pacienteId, Long citaId, String tipo, String mensaje, LocalDateTime fechaHora) {}
public record CambioCitaDTO(long cambioId, long citaId, String accion, String usuario, LocalDateTime fechaHora, String motivo) {}
public record RecetaDTO(long recetaId, long citaId, long pacienteId, String medicacion, LocalDateTime fechaHora, int numeroFicha) {}
public record AtencionDTO(LocalDate fecha, LocalTime hora, String especialidad, String medico, List<RecetaDTO> recetas) {}
public record HistorialDTO(long pacienteId, String nombrePaciente, List<AtencionDTO> atenciones) {}

public interface CitaRepository {
    long nextIdentity();
    boolean store(CitaDTO cita);                                   // false si viola UNIQUE(medico,fecha,hora)
    Optional<CitaDTO> porId(long citaId);
    List<CitaDTO> delDia(LocalDate fecha);                          // todas las del día, cualquier estado
    List<CitaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha);
    List<CitaDTO> delPaciente(long pacienteId);
    int contarDelMedicoEnFecha(long medicoId, LocalDate fecha);     // sin CANCELADA
    boolean pacienteTieneCitaEnFecha(long pacienteId, LocalDate fecha); // sin CANCELADA
    boolean medicoTienePacienteEnAgenda(long medicoId, long pacienteId);
    int contarProgramadasDelMedico(long medicoId);
    int contarProgramadasEnConsultorio(int consultorioId);
    List<CitaDTO> programadasHasta(LocalDateTime limite);           // PROGRAMADA con fecha+hora <= limite
    boolean cambiarEstado(long citaId, EstadoCita desde, EstadoCita hasta); // atómico: UPDATE ... WHERE estado = desde
    boolean registrarFin(long citaId, LocalDateTime horaFin);       // ASISTENCIA_CONFIRMADA -> ATENDIDA
}
public interface SolicitudRepository {
    long nextIdentity();
    void store(SolicitudDTO s);
    Optional<SolicitudDTO> porId(long solicitudId);
    boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia); // PENDIENTE o EN_ESPERA
    List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max);       // orden: prioridad, ordenLlegada
}
public interface HistorialRepository {
    void crearSiNoExiste(long pacienteId);
    Optional<HistorialDTO> porPaciente(long pacienteId);
    RecetaDTO agregarReceta(RecetaDTO receta);
}
public interface CambioCitaRepository {
    void registrar(CambioCitaDTO cambio);
    List<CambioCitaDTO> deCita(long citaId);
    List<CambioCitaDTO> buscar(Long citaId, String usuario, LocalDate desde, LocalDate hasta);
}
public interface NotificacionPublicador { void publicar(NotificacionDTO n); }
public interface AgendaEventos { void citaCambio(CitaDTO cita); }      // WebSocket /ws/supervision y /ws/fichas
public enum ResultadoCancelacion { CANCELADA, NO_ENCONTRADA, NO_ES_DEL_PACIENTE, ESTADO_NO_PERMITE }
public interface CancelacionCita { ResultadoCancelacion cancelarPorPaciente(long citaId, long pacienteId); }
public enum ResultadoFinalizacion { FINALIZADA, NO_ENCONTRADA, NO_ES_DE_SU_AGENDA, ESTADO_NO_PERMITE }
public interface FinalizacionCita { ResultadoFinalizacion finalizar(long citaId, long medicoId); }
public interface Reloj { LocalDateTime ahora(); }
```

## Paso a paso
1. Sincronizar la rama T100 con `develop` (ver primera fila de la tabla) y confirmar que `./mvnw test` compila.
2. Crear `model/compartido` con enums, `DominioException`, records e interfaces tal cual el contrato. **No cambiar firmas** sin avisar al equipo: de ellas dependen las otras 15 tarjetas.
3. Crear en `src/test/.../model/compartido` los `Fake*` (listas/mapas en memoria; `FakeCitaRepository.store` rechaza dos citas con mismo médico+fecha+hora si no están CANCELADA; `cambiarEstado` solo cambia si el estado actual es `desde`).
4. Crear `UsuarioAutenticado`, `ResolutorUsuario` y `ManejadorErrores` en `controller/`.
5. Crear el proyecto Flutter y los 4 archivos de `lib/core` + `views/shell/app_shell.dart`. Verificar con `flutter run -d chrome`, `-d windows` y reduciendo la ventana a 400 px (celular) y ampliando a 1920 px (TV).
6. Abrir PR a `feature/T100-algoritmos-ejecucion`, pedir revisión y avisar al grupo cuando esté integrado: **desde ese momento los demás crean su rama**.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `RelojFijoTest / FakeCitaRepositoryTest` | UNIQUE simulado y `cambiarEstado` condicional |
| `ResponsiveTest (Flutter)` | 599→celular, 600→escritorio, 1600→tv |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-00-base-contratos-flutter
# ... trabajar, commits pequeños: feat(T100-00): descripción
git push -u origin feature/T100-00-base-contratos-flutter
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
- Ninguna lógica de negocio (asignación, cancelación…): eso es de cada tarjeta.
- Seguridad real (Spring Security) y base de datos: ya están en S3-02/S3-08.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-00-base-contratos-flutter`
