# [S2-IMP-01][Java][CU-04] Esqueleto MVC del backend + caso de uso «molde»: listar especialidades

| Campo | Valor |
|---|---|
| Milestone | Sprint 2 - Arquitectura MVC (vence 2026-10-30) |
| Responsable | Benjamín (usuario de GitHub por confirmar) |
| Revisor | Alejandro (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S2-IMP-01-esqueleto-mvc` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:model` · `capa:infra` · `capa:controller` · `caso-uso:CU-04` |
| Puntos | 3 (primera mitad de CON-01 = 5 pts; la segunda es S3-01) |
| Depende de | Ninguna |
| Bloquea | Todo el Sprint 3 (S3-01 … S3-12): es el molde que las demás personas copian |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Hoy `04_imp/backend` es solo lo que genera Spring Initializr (1 clase `main` y 1 test). Esta tarjeta deja en `develop` **un caso de uso mínimo completo en las cuatro capas del profe** (Controller → Service → Repository-interfaz → `PgRepository`) para que cada compañero copie el patrón en lugar de inventar el suyo. Se usa «listar especialidades» porque es el primer paso de CU-04 (el paciente elige especialidad) y no depende de seguridad ni de otros módulos.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-04 (Must) | HU-04 | CU-04 — Solicitar cita médica | — | CON-01 (parte 1) | `03_des/01-diagramaCompor/CU-04_SolicitarCitaMedica (1).puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-04: Como paciente, quiero solicitar una cita según la especialidad que necesito para obtener atención médica.**  
> **CA (RF-04):** Dado que el paciente está habilitado para solicitar una atención, cuando selecciona la especialidad requerida y confirma la solicitud, entonces el sistema deberá registrar la solicitud sin exigirle seleccionar manualmente médico, fecha, hora o consultorio.

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-04 — Solicitar cita médica** · fuente: `03_des/01-diagramaCompor/CU-04_SolicitarCitaMedica (1).puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `EspecialidadController` (GET) → `EspecialidadService.search` → `EspecialidadRepository.search`. Es el paso «la app muestra especialidades o atenciones»; la pantalla Flutter llega en Sprint 4. | `EspecialidadServiceTest`, `EspecialidadControllerTest` |
| `[2a]` Solicita seleccionar una especialidad válida | Fuera de esta tarjeta: comprobar que la especialidad elegida existe se hace en `CitaService.solicitar` (CON-13, Sprint 4) | — |
| `[4a]` No confirma cita con recursos inexistentes u ocupados | Fuera de esta tarjeta: es CU-05 (asignación), Sprint 4 | — |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// model/recursos/Especialidad.java            (entidad: sin anotaciones de framework)
public class Especialidad {
    public Especialidad(int id, String nombre)
    public boolean isValid()                    // nombre no vacío
}
// model/recursos/EspecialidadDTO.java
public record EspecialidadDTO(int id, String nombre) {}
// model/recursos/EspecialidadRepository.java  (INTERFAZ, la define el modelo)
public interface EspecialidadRepository {
    int nextIdentity();
    boolean store(Especialidad e);
    Especialidad getById(int id);
    List<EspecialidadDTO> search(String filtro);          // lectura → DTO directo
}
// model/recursos/EspecialidadService.java
public class EspecialidadService {
    public EspecialidadService(EspecialidadRepository repo)
    public int registrar(String nombre)                   // id, o -1 si la entidad no es válida
    public List<EspecialidadDTO> search(String filtro)
}
// infrastructure/pg/PgEspecialidadRepository.java  implements EspecialidadRepository   (JDBC + PreparedStatement)
// controller/EspecialidadController.java           GET /api/v1/especialidades?filtro=   → 200 List<EspecialidadDTO>
```

- Después del merge, **Alex** es dueño de `Especialidad` (MDL-02) y la extiende; Benjamín no vuelve a tocarla.

## Tareas

- [ ] Renombrar el paquete `com.Hospital.CNS.cns_backend` → `com.hospital.cns` (los paquetes Java van en minúscula) y ajustar `groupId`/`artifactId` en `pom.xml`.
- [ ] Revisar dependencias y **anotar la decisión en el PR**: `starter-webflux` junto con `starter-webmvc` mezcla dos stacks web (Spring usa MVC); quitar `webflux` salvo justificación. Confirmar con el equipo JDBC (como en clase) vs JPA: si es JDBC, reemplazar `starter-data-jpa` por `starter-jdbc` (Hibernate no se usaría).
- [ ] Crear `04_imp/docker-compose.yml` con PostgreSQL de desarrollo y `application-dev.properties` que lea usuario/clave de variables de entorno; `.env` en `.gitignore`. (La versión de PostgreSQL no está decidida: usar una estable y dejarlo anotado.)
- [ ] Migración Flyway `V1__recursos_especialidad.sql`: schema `recursos`, tabla `especialidad(id int pk, nombre text not null unique)`, secuencia `recursos.especialidad_id_seq` y 3–5 filas de prueba (nombres genéricos). Alex continúa desde `V2` en S3-02.
- [ ] Crear los paquetes `controller/`, `model/recursos/`, `infrastructure/pg/` y escribir el molde del contrato de arriba, con comentarios cortos que digan **por qué** cada clase está en esa capa.
- [ ] Spring Security: con `starter-security` en el classpath **todo endpoint responde 401**. Agregar un `SecurityFilterChain` solo para el perfil `dev` que permita `/api/v1/**` hasta que entre S3-08, y dejarlo documentado.
- [ ] Pruebas: `EspecialidadServiceTest` con `FakeEspecialidadRepository` (sin BD), `EspecialidadControllerTest` (MockMvc, Service simulado) y `PgEspecialidadRepositoryIT` contra PostgreSQL real.
- [ ] Actualizar `04_imp/README.md`: requisitos (JDK 21, Docker o PostgreSQL local), cómo levantar la BD, correr (`./mvnw spring-boot:run`), probar (`./mvnw test`) y un `curl` de ejemplo.
- [ ] Prueba de entrega: **otra persona** clona `develop` en una carpeta nueva y levanta el backend siguiendo solo el README. Si no puede, la tarjeta no está lista.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-01A` — Arrancar el backend con configuración de pruebas y comprobar endpoint de salud y pruebas JUnit.
- [ ] `CP-CNS-01B` — Enviar datos inválidos y verificar error definido sin fuga de datos.
- [ ] `curl http://localhost:8080/api/v1/especialidades` devuelve JSON con las filas de prueba.
- [ ] `./mvnw test` en verde desde un clon limpio.

## Fuera de alcance (no hacer en esta tarjeta)

- Seguridad real (S3-08) y manejo global de errores (S3-01).
- Otras entidades del módulo `recursos` (Médico, Consultorio, Agenda): Alex, Sprint 4.
- WebSocket y reglas de asignación.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.
- [ ] Quien revisa **levantó el backend** con el README (no solo leyó el código).

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
