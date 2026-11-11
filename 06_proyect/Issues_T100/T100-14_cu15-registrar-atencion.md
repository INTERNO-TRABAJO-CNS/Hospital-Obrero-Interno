# [T100-14][Java+Flutter][CU-15] Registrar receta y fin de la atención (médico)

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Abigail |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-14-cu15-registrar-atencion` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-15 |
| Requisitos | RF-15 · RU-09 |
| Depende de / Bloquea | T100-00 (usa Fakes de `HistorialRepository` y `FinalizacionCita`) / Bloquea: — |

## Objetivo
El médico ve los datos autocompletados de la cita en atención, escribe la medicación/receta, y pulsa «Finalizar atención»: se agrega la receta al historial y la cita pasa a ATENDIDA.

## Reglas de negocio que debes cumplir
- Solo `MEDICO` dueño de la cita y estado ASISTENCIA_CONFIRMADA.
- El médico **no** registra el inicio, solo el fin.
- Receta no vacía y ≤ 2000 caracteres.
- Orden: guardar receta → `FinalizacionCita.finalizar`; si finalizar falla, **no** dejar la receta (transacción o compensación).
- Datos autocompletados: afiliado, ficha, fecha, hora, consulta.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-15_RegistrarDatosAtencion.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/atencion/AtencionService.java` | `datosParaAtencion(long citaId, long medicoId)` y `registrar(long citaId, long medicoId, String medicacion)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/atencion/DatosAtencionDTO.java, ResultadoAtencion.java` | record autocompletado; enum REGISTRADA, RECETA_INVALIDA, NO_ES_DE_SU_AGENDA, ESTADO_NO_PERMITE |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/AtencionController.java` | GET `/api/v1/citas/{id}/atencion`, POST `/api/v1/citas/{id}/atencion` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/atencion/AtencionServiceTest.java` | con `FakeHistorialRepository`, `FakeFinalizacionCita`, `FakeCitaRepository` |
| `04_imp/frontend/lib/views/medico/atencion_view.dart` | celular y escritorio: tarjeta con datos, campo «Medicación», botón «Finalizar atención» |
| `04_imp/frontend/lib/services/atencion_service.dart, controllers/atencion_controller.dart, models/atencion/*.dart` | con `FakeAtencionService` |

## Contrato (copiar las firmas tal cual)
```java
public class AtencionService {
    public AtencionService(CitaRepository c, HistorialRepository h, FinalizacionCita fin, Reloj reloj) {}
    public DatosAtencionDTO datosParaAtencion(long citaId, long medicoId);
    public ResultadoAtencion registrar(long citaId, long medicoId, String medicacion);
}
```

## Paso a paso
1. Rama `feature/T100-14-cu15-atencion`.
2. Service + tests (`[1a]`, `[2a]` de `CU-15_*.puml`).
3. Controller y pantalla con Fakes.
4. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `registrar_ok_agregaRecetaYFinaliza` |  |
| `registrar_recetaVacia` |  |
| `registrar_citaNoConfirmada` |  |
| `registrar_citaAjena` |  |
| `registrar_siFinalizarFalla_noDejaReceta` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-14-cu15-registrar-atencion
# ... trabajar, commits pequeños: feat(T100-14): descripción
git push -u origin feature/T100-14-cu15-registrar-atencion
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
- Persistencia de recetas (la hace `PgHistorialRepository` de T100-12).
- Cambiar el estado directamente.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-14-cu15-registrar-atencion`
