# [S3-05][Java][CU-03] PgPacienteRepository: persistencia JDBC de pacientes con unicidad garantizada en BD

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alejandro (`Alejandro5989`) |
| Revisor | Matías (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-05-cu03-paciente-infra` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:infra` · `caso-uso:CU-03` |
| Puntos | 2 (de CON-06 = 5) |
| Depende de | S3-02 (tablas) · S3-04 (interfaz) |
| Bloquea | S3-06 (en integración real) |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
La mitad «infraestructura» de la pizarra: implementar `PacienteRepository` con JDBC. La **unicidad** del número de asegurado la garantiza PostgreSQL (`UNIQUE`), no solo un `if` en Java.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| RF-03 (Must) | HU-03 | CU-03 — Registrar y consultar pacientes | RNF-02 | CON-06 | `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml` |

## Criterio de aceptación (copiado de `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md`)

> **HU-03: Como personal de enfermería / admisión, quiero registrar o localizar al paciente para gestionar sus citas sin duplicar su información.**  
> **CA (RF-03):** Dado que se dispone de la identificación del paciente, cuando se intenta registrarlo, entonces el sistema deberá verificar si ya existe y evitar la creación de un registro duplicado.

**RNF aplicables:**

- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente

## Diagrama de comportamiento → dónde se implementa cada paso

**CU-03 — Registrar y consultar pacientes** · fuente: `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml`

| Paso del diagrama | Se implementa en | Prueba |
|---|---|---|
| Camino principal | `PgPacienteRepository.store` (`insert … on conflict(id) do update …`, como el ejemplo de clase) | `PgPacienteRepositoryIT.store_y_getById` |
| `[1a]` Recupera y muestra el paciente existente, no duplica | `getByNumeroAsegurado` devuelve el existente; `UNIQUE(numero_asegurado_cns)` impide el segundo registro | `dos_inserts_mismo_numero_dejan_una_fila` (CP-CNS-02B, CP-CNS-06B) |
| `[3a]` No registra, solicita completar o corregir datos | La BD rechaza `NOT NULL` como red de seguridad; la validación real es `isValid()` (S3-04) | `no_acepta_numero_nulo` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// infrastructure/pg/PgPacienteRepository.java
@Repository
class PgPacienteRepository implements PacienteRepository {
    PgPacienteRepository(DataSource ds)
    public int nextIdentity()                 // select nextval('identidad.paciente_id_seq')
    public boolean store(Paciente p)          // insert … on conflict(id) do update …; false si viola UNIQUE (SQLState 23505) o falla
    public Paciente getById(int id)
    public PacienteDTO getByNumeroAsegurado(String numero)
    public List<PacienteDTO> search(String filtro)   // where … ilike ?  → DTO directo
}
```

- Nombres de tablas/columnas: los de S3-02 (que salen del DER).
- Reglas 5 y 6 de la guía: `PreparedStatement` con `?`, `try-with-resources`, logger, sin reglas de negocio.

## Tareas

- [ ] Implementar la clase según el contrato.
- [ ] Prueba de integración con PostgreSQL real: guardar/leer, buscar y duplicado.
- [ ] Prueba de **concurrencia**: dos hilos (`ExecutorService` + `CountDownLatch`) insertan el mismo número a la vez → exactamente una fila (RNF-04 en espíritu; CP-CNS-06B).

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-06A` — Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes.
- [ ] `CP-CNS-06B` — Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente.
- [ ] `CP-CNS-02A` — Ejecutar migraciones sobre una base vacía y comprobar todas las claves referenciales.
- [ ] `CP-CNS-02B` — Intentar registrar una identificación de paciente duplicada y verificar que BD no crea un segundo registro.

## Fuera de alcance (no hacer en esta tarjeta)

- Reglas de negocio en SQL.
- JPA/Hibernate.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
