# [T100-06][Java+Flutter][CU-04] Solicitar cita eligiendo especialidad y día

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Matías |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-06-cu04-solicitar-cita` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-04 |
| Requisitos | RF-04 · RU-05 · RNF-08 |
| Depende de / Bloquea | T100-00, T100-05 / Bloquea: T100-07 |

## Objetivo
El paciente elige **especialidad** (3 opciones) y **día**; el sistema encola la solicitud. Verá «cita asignada» o «NO HAY CUPOS — quedó en espera para el siguiente día hábil».

## Reglas de negocio que debes cumplir
- Solo día hábil y dentro de los **15 días hábiles** siguientes.
- Una cita por paciente por día (también con solicitud activa ese día).
- No elige médico, hora ni consultorio.
- `ordenLlegada` = secuencia creciente (`nextIdentity`); `prioridad = 1`.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-04_SolicitarCitaMedica (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/SolicitudService.java` | `solicitar(long pacienteId, TipoEspecialidad esp, LocalDate dia)` y `solicitarReprogramada(...)` (prioridad 2, usado por T100-07) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/citas/ResultadoSolicitud.java` | record(SolicitudDTO solicitud) o excepciones: `DIA_NO_HABIL`, `FUERA_DE_RANGO`, `YA_TIENE_CITA_ESE_DIA`, `ESPECIALIDAD_INVALIDA` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/SolicitudController.java` | POST `/api/v1/solicitudes`, GET `/api/v1/solicitudes/{id}` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/citas/SolicitudServiceTest.java` | con `FakeSolicitudRepository`, `FakeCitaRepository`, `RelojFijo` |
| `04_imp/frontend/lib/views/paciente/solicitar_cita_view.dart` | paso 1: 3 tarjetas de especialidad; paso 2: calendario con días no hábiles deshabilitados; paso 3: confirmar |
| `04_imp/frontend/lib/views/paciente/resultado_solicitud_view.dart` | estados: «Cita asignada: ficha, médico, hora, consultorio» / «NO HAY CUPOS… en espera» / «SIN CUPO en 15 días hábiles» |
| `04_imp/frontend/lib/services/solicitud_service.dart, controllers/solicitud_controller.dart, models/solicitud/*.dart` | consultar el estado cada 3 s o por notificación |

## Contrato (copiar las firmas tal cual)
```java
public class SolicitudService {
    public SolicitudService(SolicitudRepository s, CitaRepository c, CalendarioHabil cal, Reloj reloj) {}
    public SolicitudDTO solicitar(long pacienteId, TipoEspecialidad esp, LocalDate dia);
    public SolicitudDTO solicitarReprogramada(long pacienteId, TipoEspecialidad esp, LocalDate dia);
}
```

## Paso a paso
1. Rama `feature/T100-06-cu04-solicitar`.
2. `CalendarioHabil` viene de T100-03; si aún no está integrado, usa `FakeCalendarioHabil` en tests y pide a Benjamín la interfaz.
3. Service + tests (un test por `[Na]` de `CU-04_*.puml`).
4. Controller (usa `UsuarioAutenticado.personaId` como pacienteId, nunca un id del body).
5. Pantallas Flutter con `FakeSolicitudService`.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `solicitar_ok_creaPendienteEnCola` |  |
| `solicitar_domingo_rechaza` | DIA_NO_HABIL |
| `solicitar_dia16Habil_rechaza` | FUERA_DE_RANGO |
| `solicitar_yaTieneCita_otraEspecialidad` | YA_TIENE_CITA_ESE_DIA |
| `solicitarReprogramada_prioridad2` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-06-cu04-solicitar-cita
# ... trabajar, commits pequeños: feat(T100-06): descripción
git push -u origin feature/T100-06-cu04-solicitar-cita
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
- La asignación (T100-03).
- Elegir médico u hora.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-06-cu04-solicitar-cita`
