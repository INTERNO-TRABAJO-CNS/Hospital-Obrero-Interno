# [T100-07][Java+Flutter][CU-08/10] Mis citas y reprogramar (= cancelar + nueva solicitud)

| Campo | Valor |
|---|---|
| Milestone | Sprint 6 - Validacion y entrega (vence 2026-11-28) |
| Responsable | Matías |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-07-cu08-cu10-mis-citas-reprogramar` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-08, CU-10 |
| Requisitos | RF-08 · RF-10 · RU-08 |
| Depende de / Bloquea | T100-06; usa el puerto `CancelacionCita` (implementa T100-11) / Bloquea: — |

## Objetivo
El paciente ve solo sus citas y puede reprogramar una cita PROGRAMADA: el sistema la cancela y crea una nueva solicitud para el **siguiente día hábil** con prioridad menor.

## Reglas de negocio que debes cumplir
- Solo citas propias (RNF-01).
- Solo se reprograma si está PROGRAMADA.
- Advertir y pedir confirmación antes de cancelar.
- Usar `CancelacionCita.cancelarPorPaciente` (no tocar el estado directo).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-08_ConsultarCitasPaciente.puml`, `03_des/01-diagramaCompor/CU-10_ReprogramarCita (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/ConsultaCitasPacienteService.java` | `misCitas(long pacienteId)`, `detalle(long citaId, long pacienteId)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/ReprogramacionService.java` | `reprogramar(long citaId, long pacienteId)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/CitasPacienteController.java` | GET `/api/v1/citas/mias`, GET `/api/v1/citas/{id}`, POST `/api/v1/citas/{id}/reprogramar` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/citas/ReprogramacionServiceTest.java` | con `FakeCancelacionCita`, `FakeSolicitudRepository` |
| `04_imp/frontend/lib/views/paciente/mis_citas_view.dart, detalle_cita_view.dart` | lista y detalle; botones Cancelar/Reprogramar |
| `04_imp/frontend/lib/services/citas_paciente_service.dart, controllers/mis_citas_controller.dart` | usa el diálogo `dialogo_cancelar.dart` de T100-11 |

## Contrato (copiar las firmas tal cual)
```java
public class ReprogramacionService {
    public ReprogramacionService(CitaRepository c, CancelacionCita cancelacion, SolicitudService solicitudes, CalendarioHabil cal, Reloj reloj) {}
    public SolicitudDTO reprogramar(long citaId, long pacienteId); // ESTADO_NO_PERMITE, NO_ES_DEL_PACIENTE
}
```

## Paso a paso
1. Rama `feature/T100-07-cu08-cu10`.
2. Services y tests con Fakes.
3. Controller y pantallas.
4. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `misCitas_soloLasPropias` |  |
| `reprogramar_cancelaYCreaSolicitudPrioridad2` |  |
| `reprogramar_citaAtendida_rechaza` |  |
| `reprogramar_citaAjena_rechaza` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-07-cu08-cu10-mis-citas-reprogramar
# ... trabajar, commits pequeños: feat(T100-07): descripción
git push -u origin feature/T100-07-cu08-cu10-mis-citas-reprogramar
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
- Cambiar hora/médico directamente (no existe).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-07-cu08-cu10-mis-citas-reprogramar`
