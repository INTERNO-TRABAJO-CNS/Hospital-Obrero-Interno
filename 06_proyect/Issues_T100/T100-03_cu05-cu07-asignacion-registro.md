# [T100-03][Java][CU-05/07] Asignar cita automáticamente por cola y registrar la cita confirmada

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-03-cu05-cu07-asignacion-registro` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 8 |
| Casos de uso | CU-05, CU-07 |
| Requisitos | RF-05 · RF-07 · RU-05 · RNF-03 · RNF-04 |
| Depende de / Bloquea | T100-00, T100-02 / Bloquea: T100-06, T100-09, T100-10 |

## Objetivo
El sistema toma las solicitudes por orden de llegada, busca médico/hora/consultorio, y crea la cita PROGRAMADA con número de ficha. Si no hay cupo: NO HAY CUPOS → EN_ESPERA al siguiente día hábil, hasta 15 días hábiles → SIN_CUPO.

## Reglas de negocio que debes cumplir
- Cola: primero `prioridad=1` (quien no canceló), después `prioridad=2`; dentro de cada una, por `ordenLlegada`.
- Búsqueda de **15 días hábiles como máximo** (sin fines de semana ni `DiaNoHabil`).
- Hora: franjas consecutivas dentro del horario del médico, de `duracionCitaMin` cada una.
- Médico elegido: el de la especialidad con **menos fichas ese día**; empate → menor `medicoId` (`// PENDIENTE PV-13`).
- Número de ficha = correlativo por médico y día (`// PENDIENTE PV-14`).
- Registrar la cita, marcar la solicitud ASIGNADA y escribir `CambioCita` en **una transacción**; después del commit, `NotificacionPublicador.publicar` y `AgendaEventos.citaCambio`.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-05_AsignarCitaAutomaticamente (1).puml`, `03_des/01-diagramaCompor/CU-07_RegistrarCitaConfirmada (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/CalendarioHabil.java` | `LocalDate siguienteDiaHabil(LocalDate)`, `boolean esHabil(LocalDate)`, `LocalDate sumarDiasHabiles(LocalDate, int)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/AsignacionService.java` | `procesarSiguiente()` y `procesarCola(int max)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/CitaService.java` | `registrar(Asignacion a)` (CU-07): crea `CitaDTO` PROGRAMADA, marca solicitud, registra cambio, publica eventos |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/Asignacion.java, ResultadoAsignacion.java` | record de la asignación elegida; enum ASIGNADA, EN_ESPERA, SIN_CUPO, NADA_PENDIENTE |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgCitaRepository.java, PgSolicitudRepository.java` | implementan las interfaces de T100-00; `store` usa transacción y devuelve false si salta el UNIQUE |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/planificador/ProcesadorCola.java` | `@Scheduled(fixedDelay = 5000)` que llama `procesarCola(20)` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__citas_solicitudes.sql` | tablas de cita y solicitud con sus índices |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/citas/AsignacionServiceTest.java, CitaServiceTest.java, CalendarioHabilTest.java` | con Fakes de T100-00 y `FakeAgendaRepository` |

## Contrato (copiar las firmas tal cual)
```java
public class AsignacionService {
    public AsignacionService(SolicitudRepository s, CitaRepository c, DisponibilidadService d,
                             MedicoRepository m, HorarioRepository h, CalendarioHabil cal, CitaService registro, Reloj reloj) {}
    public ResultadoAsignacion procesarSiguiente();
}
public class CitaService {
    public CitaService(CitaRepository c, SolicitudRepository s, CambioCitaRepository cambios,
                       NotificacionPublicador notif, AgendaEventos eventos, Reloj reloj) {}
    public CitaDTO registrar(Asignacion a);   // lanza DominioException("ASIGNACION_INVALIDA") si el UNIQUE rechaza
}
```

## Paso a paso
1. Rama `feature/T100-03-cu05-cu07-asignacion`.
2. `CalendarioHabil` primero (test: viernes → lunes; día no hábil intermedio).
3. `AsignacionService.procesarSiguiente()`: (1) toma la primera solicitud de `siguientesParaProcesar`; (2) para cada día hábil desde `diaSolicitado` (máx 15): por cada médico activo de la especialidad, por cada franja libre → `DisponibilidadService.validar`; si `DISPONIBLE` → `CitaService.registrar` y devuelve ASIGNADA; si el día no tiene cupo, pasa al siguiente; (3) si ya hay 15 días sin cupo → solicitud `SIN_CUPO`; si no, `EN_ESPERA` con `diaSolicitado` = el siguiente día hábil.
4. `BLOQUEADA_POR_OTRO` o `ASIGNACION_INVALIDA` ⇒ seguir con la siguiente combinación (no fallar).
5. Un test por cada flujo `[Na]` de `CU-05_*.puml` y `CU-07_*.puml`.
6. `Pg*`, migración y `ProcesadorCola`.
7. Prueba de integración: 10 solicitudes simultáneas por 3 huecos → exactamente 3 citas, sin duplicados.
8. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `asigna_enOrdenDeLlegada` |  |
| `prioridad2_vaDespuesDePrioridad1` |  |
| `sinCupoHoy_pasaAEnEsperaSiguienteHabil` | NO HAY CUPOS |
| `15DiasSinCupo_sinCupo` |  |
| `pacienteConCitaEseDia_noAsigna` |  |
| `citaService_marcaSolicitudAsignada` |  |
| `IT_concurrencia_10solicitudes_3huecos` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-03-cu05-cu07-asignacion-registro
# ... trabajar, commits pequeños: feat(T100-03): descripción
git push -u origin feature/T100-03-cu05-cu07-asignacion-registro
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
- Reasignar cupos liberados por cancelaciones (decisión 04/11/2026: más adelante).
- Mostrar el resultado al paciente: lo hace T100-06.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-03-cu05-cu07-asignacion-registro`
