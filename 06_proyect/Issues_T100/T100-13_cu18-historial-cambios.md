# [T100-13][Java+Flutter][CU-18] Consultar historial de cambios de citas (auditoría)

| Campo | Valor |
|---|---|
| Milestone | Sprint 6 - Validacion y entrega (vence 2026-11-28) |
| Responsable | Alex |
| Revisor (aprueba) | Abigail |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-13-cu18-historial-cambios` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Casos de uso | CU-18 |
| Requisitos | RF-18 · RU-11 |
| Depende de / Bloquea | T100-00 / Bloquea: — |

## Objetivo
Enfermería y el administrador consultan quién cambió qué cita, cuándo y por qué (confirmación, cancelación por paciente o por el sistema, atención).

## Reglas de negocio que debes cumplir
- Solo `ENFERMERIA` y `ADMINISTRADOR`.
- Filtros: cita, usuario, rango de fechas.
- Solo lectura; los registros nunca se modifican ni se borran.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-18_ConsultarHistorialCambios.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/historial/HistorialCambiosService.java` | `consultar(Long citaId, String usuario, LocalDate desde, LocalDate hasta)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgCambioCitaRepository.java` | implementa `CambioCitaRepository` de T100-00 (los demás ya llaman `registrar`) |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__cambio_cita.sql` | tabla `operaciones.cambio_cita` (solo `insert`) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/CambiosController.java` | GET `/api/v1/admin/cambios` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/historial/HistorialCambiosServiceTest.java` |  |
| `04_imp/frontend/lib/views/admin/historial_cambios_view.dart` | escritorio: filtros + tabla (enfermería y administrador) |
| `04_imp/frontend/lib/services/cambios_service.dart, controllers/cambios_controller.dart, models/cambios/*.dart` |  |

## Contrato (copiar las firmas tal cual)
```java
public class HistorialCambiosService {
    public HistorialCambiosService(CambioCitaRepository r) {}
    public List<CambioCitaDTO> consultar(Long citaId, String usuario, LocalDate desde, LocalDate hasta);
}
```

## Paso a paso
1. Rama `feature/T100-13-cu18-cambios`.
2. Service + tests.
3. Migración + `Pg` + controller.
4. Pantalla.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `consultar_porCita` |  |
| `consultar_rangoFechas` |  |
| `consultar_rolPacienteOMedico_403` |  |
| `registrar_noPermiteBorrar` | la interfaz no expone delete |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-13-cu18-historial-cambios
# ... trabajar, commits pequeños: feat(T100-13): descripción
git push -u origin feature/T100-13-cu18-historial-cambios
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
- Exportar a archivo.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-13-cu18-historial-cambios`
