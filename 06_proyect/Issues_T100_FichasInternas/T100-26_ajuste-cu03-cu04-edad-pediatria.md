# [T100-26][Java+Flutter][Ajuste CU-03/04] Fecha de nacimiento del paciente, Pediatría solo menores de 10 años y solo especialidades básicas

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Matías |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-26-ajuste-cu03-cu04-edad-pediatria` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-03, CU-04 |
| Requisitos | RF-03 · RF-04 |
| Depende de / Bloquea | T100-05, T100-06, T100-17 / Bloquea: T100-24 (orden por edad en BD) |

## Objetivo
El sistema conoce la edad de cada paciente para aplicar dos reglas nuevas: Pediatría solo para menores de 10 años y prioridad para adultos mayores (60 años o más). Además, el paciente solo puede pedir las 3 especialidades básicas.

## Reglas de negocio que debes cumplir
- Paciente (titular y beneficiario) con `fechaNacimiento` obligatoria y no futura.
- Implementar `EdadPaciente.edadEn(pacienteId, fecha)` = años cumplidos (`Period.between`).
- CU-04: especialidades permitidas = `esBasica()` (CONSULTORIO_FAMILIAR, ODONTOLOGIA, PEDIATRIA); otra → `ESPECIALIDAD_INVALIDA`.
- CU-04: PEDIATRIA con edad ≥ 10 → `EDAD_NO_PEDIATRICA` (422).
- Semilla de afiliados con edades variadas: al menos 3 menores de 10 y 3 de 60 o más.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes/CU-03_RegistrarConsultarPacientes.puml` *(lo crea T100-16)*
- `03_des/01-diagramaCompor/CU-04_SolicitarCitaMedica/CU-04_SolicitarCitaMedica.puml` *(lo crea T100-16)*
- Tarjetas `T100-05` y `T100-06`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/identidad/PacienteDTO.java, Paciente.java, PacienteService.java` | campo `fechaNacimiento` y su validación |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/identidad/EdadPacienteService.java` | implementa `EdadPaciente` (model/compartido) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgPacienteRepository.java` | leer/guardar `fecha_nacimiento` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__paciente_fecha_nacimiento.sql` | `ALTER TABLE … ADD COLUMN fecha_nacimiento date` + datos de semilla + `NOT NULL` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/SolicitudService.java` | las dos reglas de CU-04 |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/identidad/EdadPacienteServiceTest.java, model/citas/SolicitudServiceTest.java` | casos nuevos |
| `04_imp/frontend/lib/views/auth/registro_titular_view.dart (y beneficiario)` | selector de fecha de nacimiento |
| `04_imp/frontend/lib/views/paciente/solicitar_cita_view.dart` | solo 3 tarjetas; Pediatría deshabilitada con el texto «Solo menores de 10 años» si el paciente tiene 10 o más |

## Paso a paso
1. Rama `feature/T100-26-ajuste-cu03-cu04-edad-pediatria`.
2. Migración y modelo.
3. `EdadPacienteService` + tests.
4. Reglas de CU-04 + tests.
5. Pantallas Flutter.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `edad_cumpleHoy_cuentaElAnio` |  |
| `solicitar_pediatria_9anios_ok / 10anios_EDAD_NO_PEDIATRICA` |  |
| `solicitar_cardiologia_ESPECIALIDAD_INVALIDA` | las internas solo por derivación |
| `registro_fechaFutura_rechaza` |  |
| `solicitar_cita_view_test: pediatriaDeshabilitadaMayor10` | Flutter |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-26-ajuste-cu03-cu04-edad-pediatria
# ... trabajar, commits pequeños: feat(T100-26): descripción
git push -u origin feature/T100-26-ajuste-cu03-cu04-edad-pediatria
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
- Orden de la cola por edad (T100-24).
- Fichas internas (las genera el médico).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-26-ajuste-cu03-cu04-edad-pediatria`
