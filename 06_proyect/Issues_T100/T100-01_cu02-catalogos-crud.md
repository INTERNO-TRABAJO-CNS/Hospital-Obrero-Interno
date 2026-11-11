# [T100-01][Java+Flutter][CU-02] Administrar catálogos: médicos, consultorios, horarios, cupos y días no hábiles

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-01-cu02-catalogos-crud` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-02 |
| Requisitos | RF-02 · RU-12 · RU-13 · RNF-01 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-02, T100-03 |

## Objetivo
El administrador crea, modifica y da de baja médicos (con su especialidad, duración de ficha y cupo 5–7), consultorios, horarios y días no hábiles. La asignación automática lee estos datos.

## Reglas de negocio que debes cumplir
- Cupo diario por médico: **mínimo meta 5, máximo 7** (no se aceptan valores fuera de 5–7).
- Duración de ficha dentro del rango de la especialidad (General 30–45, Odontología 45–60, Pediatría 30–45).
- No se aplica un cambio si afecta citas PROGRAMADAS (`contarProgramadasDelMedico/EnConsultorio` > 0) → error `CAMBIO_AFECTA_CITAS`.
- Baja **lógica** (campo `activo=false`); nada se borra físicamente.
- Solo rol `ADMINISTRADOR` (RNF-01). Usuarios y roles ya están en S3-09: **no duplicar**, solo reutilizar.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-02_GestionarUsuariosRolesPermisos.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `n2XbqM0jcK8ow40Nsx8uoe` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/Medico.java` | entidad: id, nombres, apellidos, `TipoEspecialidad`, `duracionCitaMin`, `cupoMaximoDia`, `activo`; método `isValid()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/Consultorio.java, HorarioMedico.java, DiaNoHabil.java` | entidades con `isValid()` (inicio<fin, fecha futura) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/MedicoDTO.java, ConsultorioDTO.java, HorarioDTO.java, DiaNoHabilDTO.java` | records |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/MedicoRepository.java, ConsultorioRepository.java, HorarioRepository.java, DiaNoHabilRepository.java` | interfaces: `nextIdentity`, `store`, `porId`, `listar`, `existe…` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/recursos/CatalogoService.java` | casos de uso (firmas abajo); recibe repos + `CitaRepository` por constructor |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgMedicoRepository.java (y los otros 3 Pg*)` | JDBC con `PreparedStatement`, `insert … on conflict (id) do update` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__recursos_catalogos.sql` | tablas `recursos.medico`, `recursos.consultorio`, `recursos.horario_medico`, `recursos.dia_no_habil` y semilla: 3 especialidades. Usar el siguiente número libre de Flyway. |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/AdminCatalogoController.java` | REST bajo `/api/v1/admin/{medicos|consultorios|horarios|dias-no-habiles}` (POST, PUT /{id}, DELETE /{id} = baja lógica, GET) |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/recursos/CatalogoServiceTest.java` | con `Fake*Repository` (créalos en `src/test/.../model/recursos/`) |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/controller/AdminCatalogoControllerTest.java` | MockMvc con Service simulado |
| `04_imp/frontend/lib/models/catalogo/*.dart, services/catalogo_service.dart, controllers/catalogo_controller.dart` | DTO, llamadas REST y estado |
| `04_imp/frontend/lib/views/admin/catalogo_medicos_view.dart, catalogo_consultorios_view.dart, catalogo_horarios_view.dart, catalogo_dias_view.dart` | pantallas de **computadora** (tabla + formulario); usar `FakeCatalogoService` hasta tener backend |

## Contrato (copiar las firmas tal cual)
```java
public class CatalogoService {
    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas) {}
    public MedicoDTO crearMedico(MedicoDTO datos);          // VALIDACION_CUPO, VALIDACION_DURACION
    public MedicoDTO modificarMedico(long id, MedicoDTO d); // CAMBIO_AFECTA_CITAS
    public void darDeBajaMedico(long id);                   // CAMBIO_AFECTA_CITAS
    public ConsultorioDTO crearConsultorio(ConsultorioDTO d);
    public void darDeBajaConsultorio(int id);
    public HorarioDTO definirHorario(HorarioDTO d);        // HORARIO_SOLAPADO
    public DiaNoHabilDTO registrarDiaNoHabil(DiaNoHabilDTO d);
    public List<MedicoDTO> listarMedicos(TipoEspecialidad filtro);
}
```
Todos lanzan `DominioException(codigo)` en los casos de error.

## Paso a paso
1. Crear la rama `feature/T100-01-cu02-catalogos` desde T100 (ya con T100-00 integrado).
2. Escribir primero las entidades con `isValid()` y las **interfaces** Repository; después `CatalogoService` y sus tests con Fakes.
3. Cada alternativa del diagrama `03_des/01-diagramaCompor/CU-02_*.puml` es un test: `[1a]` sin rol admin → 403; `[3a]` cupo/duración inválidos → 422; `[3b]` afecta citas → 409.
4. Escribir `Pg*Repository` y la migración. Probar contra PostgreSQL real.
5. Escribir `AdminCatalogoController` (solo traduce Service → HTTP).
6. Escribir las pantallas Flutter de escritorio y probar con `FakeCatalogoService`.
7. PR a T100; el revisor ejecuta los endpoints y las pantallas.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `crearMedico_cupo8_rechaza` | cupo fuera de 5–7 → `VALIDACION_CUPO` |
| `crearMedico_duracionFueraDeRango` | General con 50 min → `VALIDACION_DURACION` |
| `modificarMedico_conCitasProgramadas` | → `CAMBIO_AFECTA_CITAS` |
| `definirHorario_solapado` | → `HORARIO_SOLAPADO` |
| `controller_sinRolAdmin_403` | MockMvc con `X-Rol: PACIENTE` |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-01-cu02-catalogos-crud
# ... trabajar, commits pequeños: feat(T100-01): descripción
git push -u origin feature/T100-01-cu02-catalogos-crud
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
- Usuarios y roles (S3-09).
- Calendario de feriados oficiales: solo carga manual de días no hábiles.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-01-cu02-catalogos-crud`
