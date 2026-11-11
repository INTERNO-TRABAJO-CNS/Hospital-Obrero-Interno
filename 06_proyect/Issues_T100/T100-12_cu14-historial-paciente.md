# [T100-12][Java+Flutter][CU-14] Consultar historial clínico del paciente (médico)

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Alex |
| Revisor (aprueba) | Abigail |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-12-cu14-historial-paciente` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-14 |
| Requisitos | RF-14 · RU-11 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-14 |

## Objetivo
El médico abre el historial de un paciente **de su agenda**: nombre, citas pasadas (fecha y hora), atenciones y recetas anteriores. Solo lectura.

## Reglas de negocio que debes cumplir
- Solo `MEDICO` y solo pacientes con cita en su agenda (`medicoTienePacienteEnAgenda`), si no: `PACIENTE_FUERA_DE_AGENDA` (403).
- Sin modificar nada.
- Datos que el rol no puede ver no se devuelven (`[3a]`).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-14_ConsultarDatosPaciente.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/historial/HistorialService.java` | `verHistorial(long medicoId, long pacienteId)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgHistorialRepository.java` | implementa `HistorialRepository` de T100-00 (incluye `agregarReceta` que usa T100-14) |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__historial_receta.sql` | tablas `clinico.historial` y `clinico.receta` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/HistorialController.java` | GET `/api/v1/medico/pacientes/{pacienteId}/historial` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/historial/HistorialServiceTest.java` | con `FakeHistorialRepository`, `FakeCitaRepository` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgHistorialRepositoryIT.java` |  |
| `04_imp/frontend/lib/views/medico/historial_paciente_view.dart` | celular y escritorio: nombre, línea de tiempo de atenciones, recetas |
| `04_imp/frontend/lib/services/historial_service.dart, controllers/historial_controller.dart, models/historial/*.dart` | con `FakeHistorialService` |

## Contrato (copiar las firmas tal cual)
```java
public class HistorialService {
    public HistorialService(HistorialRepository h, CitaRepository c) {}
    public HistorialDTO verHistorial(long medicoId, long pacienteId);
}
```

## Paso a paso
1. Rama `feature/T100-12-cu14-historial`.
2. Service + tests (`[1a]`, `[3a]` de `CU-14_*.puml`).
3. Migración + `PgHistorialRepository` + IT.
4. Controller y pantalla.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `verHistorial_pacienteEnAgenda_ok` |  |
| `verHistorial_pacienteAjeno_rechaza` |  |
| `verHistorial_ordenaPorFecha` |  |
| `IT_agregarRecetaYLeer` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-12-cu14-historial-paciente
# ... trabajar, commits pequeños: feat(T100-12): descripción
git push -u origin feature/T100-12-cu14-historial-paciente
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
- Editar el historial (lo hace T100-14).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-12-cu14-historial-paciente`
