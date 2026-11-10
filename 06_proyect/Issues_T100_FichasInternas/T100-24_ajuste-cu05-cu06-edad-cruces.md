# [T100-24][Java][Ajuste CU-05/06] Prioridad de adulto mayor en la cola común, edad de Pediatría y cruce de horario con fichas internas y cirugías

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-24-ajuste-cu05-cu06-edad-cruces` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-05, CU-06 |
| Requisitos | RF-05 · RF-06 · RNF-04 |
| Depende de / Bloquea | T100-02, T100-03, T100-17, T100-26 (columna `fecha_nacimiento`) / Bloquea: T100-20 y T100-23 contra la BD real (usan `PgOcupacionAgenda`) |

## Objetivo
Aplicar a las citas comunes las reglas nuevas que valen «en todo»: adulto mayor primero, Pediatría solo menores de 10 años y ningún cruce de horario del paciente con sus fichas internas o cirugías.

## Reglas de negocio que debes cumplir
- Orden de la cola común (T100-03): `prioridad` (1 antes que 2) → **adulto mayor (≥ 60)** → `ordenLlegada`.
- `DisponibilidadService.validar`: nuevos resultados `PACIENTE_OCUPADO` (cruce con ficha interna o cirugía) y `EDAD_NO_PEDIATRICA` (PEDIATRIA con edad ≥ 10, defensa además de la validación de CU-04).
- `AsignacionService` solo procesa especialidades `esBasica()`.
- `PgOcupacionAgenda`: vista `agenda.v_ocupacion(paciente_id, medico_id, inicio, fin)` = citas comunes no CANCELADAS + `fichas_internas.v_todas` ASIGNADA/ATENDIDA + `quirofano.cirugia` no CANCELADAS. Cruce = `inicio < finPedido AND fin > inicioPedido`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-05_AsignarCitaAutomaticamente/CU-05_AsignarCitaAutomaticamente.puml` *(lo crea T100-16)*
- `03_des/01-diagramaCompor/CU-06_ValidarDisponibilidadRecursos/CU-06_ValidarDisponibilidadRecursos.puml` *(lo crea T100-16)*
- Tarjetas `T100-02` y `T100-03`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/ResultadoDisponibilidad.java` | agregar `PACIENTE_OCUPADO`, `EDAD_NO_PEDIATRICA` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/DisponibilidadService.java` | recibe además `OcupacionAgenda` y `EdadPaciente` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgSolicitudRepository.java` | `siguientesParaProcesar`: `ORDER BY prioridad, adulto_mayor DESC, orden_llegada` (edad calculada con `fecha_nacimiento` de T100-26) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgOcupacionAgenda.java` | implementa `OcupacionAgenda` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__vista_ocupacion.sql` | vista `agenda.v_ocupacion` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/citas/DisponibilidadServiceTest.java, AsignacionServiceTest.java` | casos nuevos |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgOcupacionAgendaIT.java` | cruce entre las tres fuentes |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/compartido/FakeSolicitudRepository.java` | ordenar también por adulto mayor (usa `FakeEdadPaciente`) |

## Paso a paso
1. Rama `feature/T100-24-ajuste-cu05-cu06-edad-cruces` (después de integrar T100-02 y T100-03).
2. Resultados nuevos + tests.
3. Orden de la cola + tests.
4. Vista y `PgOcupacionAgenda` + IT.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `cola_adultoMayorAntesQueLlegadaAnterior` | ≥ 60 dentro de la misma prioridad |
| `cola_prioridad2DeAdultoMayor_vaDespuesDePrioridad1` | la prioridad por cancelar sigue primero |
| `validar_pacienteConCirugiaEseHorario_PACIENTE_OCUPADO` |  |
| `validar_pediatria10anios_EDAD_NO_PEDIATRICA` |  |
| `PgOcupacionAgendaIT_cruceCitaFichaCirugia` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-24-ajuste-cu05-cu06-edad-cruces
# ... trabajar, commits pequeños: feat(T100-24): descripción
git push -u origin feature/T100-24-ajuste-cu05-cu06-edad-cruces
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
- Pantallas del paciente (T100-26).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-24-ajuste-cu05-cu06-edad-cruces`
