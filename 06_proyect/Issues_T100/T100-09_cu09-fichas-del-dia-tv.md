# [T100-09][Java+Flutter][CU-09] Listado de fichas del día para enfermería y pantalla de TV (solo lectura)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Alejandro |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-09-cu09-fichas-del-dia-tv` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-09 |
| Requisitos | RF-09 · RU-06 · RNF-07 |
| Depende de / Bloquea | T100-00 / Bloquea: — |

## Objetivo
Enfermería consulta las fichas **del día actual** por paciente, médico, estado o consultorio. La **TV** muestra únicamente el listado de fichas del día, sin controles.

## Reglas de negocio que debes cumplir
- La fecha es siempre **hoy**; si llega otra fecha → `SOLO_FICHAS_DEL_DIA` (422).
- Criterios: PACIENTE, MEDICO, ESTADO, CONSULTORIO.
- La TV es de solo lectura: sin filtros ni botones; letra grande; se refresca sola cada 10 s (luego por WebSocket `/ws/fichas`).
- La autenticación de la TV queda `// PENDIENTE PV-16` (por ahora endpoint solo lectura).

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-09_ConsultarCitasRegistradas (1).puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/FichasDelDiaService.java` | `buscar(CriterioFicha criterio, String valor)`, `listadoParaTv()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/operaciones/CriterioFicha.java` | enum PACIENTE, MEDICO, ESTADO, CONSULTORIO |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/FichasController.java` | GET `/api/v1/fichas/hoy?criterio=&valor=` (rol ENFERMERIA) y GET `/api/v1/tv/fichas` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/operaciones/FichasDelDiaServiceTest.java` |  |
| `04_imp/frontend/lib/views/enfermeria/fichas_dia_view.dart` | escritorio: buscador + tabla |
| `04_imp/frontend/lib/views/tv/tv_fichas_view.dart` | **TV** (≥1600 px, ruta `/tv`): tabla grande: ficha · hora · especialidad · médico · consultorio · estado; sin menú |
| `04_imp/frontend/lib/services/fichas_service.dart, controllers/fichas_controller.dart, models/fichas/*.dart` | Timer de 10 s en el controller de la TV |

## Contrato (copiar las firmas tal cual)
```java
public class FichasDelDiaService {
    public FichasDelDiaService(CitaRepository citas, Reloj reloj) {}
    public List<CitaDTO> buscar(CriterioFicha criterio, String valor);
    public List<CitaDTO> listadoParaTv();
}
```

## Paso a paso
1. Rama `feature/T100-09-cu09-fichas-tv`.
2. Service + tests: `[2a]` criterio inválido, `[2b]` fecha distinta de hoy, `[3a]` sin resultados.
3. Controller.
4. Pantalla de enfermería y pantalla TV; probar en navegador a 1920×1080.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `buscar_porMedico` |  |
| `buscar_fechaDistintaDeHoy_rechaza` |  |
| `buscar_sinCoincidencias_vacio` |  |
| `listadoTv_soloDeHoy` |  |
| `widget_tv_sinBotones` | Flutter |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-09-cu09-fichas-del-dia-tv
# ... trabajar, commits pequeños: feat(T100-09): descripción
git push -u origin feature/T100-09-cu09-fichas-del-dia-tv
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
- Filtros u operaciones en la TV.
- Login de la TV.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-09-cu09-fichas-del-dia-tv`
