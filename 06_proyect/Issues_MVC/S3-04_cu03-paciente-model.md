# [S3-04][Java][CU-03] Paciente: entidad, DTO, Repository (interfaz) y Service con pruebas sin BD

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alejandro (`Alejandro5989`) |
| Revisor | Matías (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-04-cu03-paciente-model` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:model` · `caso-uso:CU-03` |
| Puntos | 2 (de CON-06 = 5) |
| Depende de | S2-IMP-01 (molde) |
| Bloquea | S3-05, S3-06 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Toda la **lógica** de registrar y buscar pacientes, sin tocar BD ni HTTP: se prueba con un repositorio falso en memoria. Es la mitad «Model» de la pizarra del profe (Service + Entidad + DTO + Repository-interfaz).

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
| Camino principal | `PacienteService.registrar(PacienteDTO)`: busca por número de asegurado → no existe → `repo.nextIdentity()` → `new Paciente(...)` → `isValid()` → `repo.store()` → `CREADO` | `PacienteServiceTest.registra_paciente_nuevo` |
| `[1a]` Recupera y muestra el paciente existente, no duplica | Si ya existe: devuelve `YA_EXISTIA` con el DTO existente y **no llama** a `store` (no duplica) | `registrar_existente_no_duplica` (CP-CNS-06A) |
| `[3a]` No registra, solicita completar o corregir datos | `Paciente.isValid()` falso → `DATOS_INVALIDOS`, no se llama a `store` | `registrar_datos_invalidos_no_guarda` |

## Contrato de clases (qué debe existir al terminar)

Propuesta de firmas: quien implementa puede ajustar nombres/parámetros, **no** las reglas de capa de la guía. Sin implementar aquí.

```java
// model/identidad/Paciente.java         (entidad sin anotaciones)
public class Paciente {
    public Paciente(int id, String numeroAseguradoCNS, String nombres, String apellidos, String ci /* opcional */)
    public boolean isValid()      // numeroAseguradoCNS, nombres y apellidos obligatorios; CI NO reemplaza la afiliación (DC-06)
}
// model/identidad/PacienteDTO.java
public record PacienteDTO(int id, String numeroAseguradoCNS, String nombres, String apellidos, String ci) {}
// model/identidad/RegistroPacienteResultado.java
public record RegistroPacienteResultado(Estado estado, PacienteDTO paciente) { public enum Estado { CREADO, YA_EXISTIA, DATOS_INVALIDOS } }
// model/identidad/PacienteRepository.java   (INTERFAZ)
public interface PacienteRepository {
    int nextIdentity();
    boolean store(Paciente p);
    Paciente getById(int id);
    PacienteDTO getByNumeroAsegurado(String numeroAsegurado);   // null si no existe
    List<PacienteDTO> search(String filtro);
}
// model/identidad/PacienteService.java
public class PacienteService {
    public PacienteService(PacienteRepository repo)
    public RegistroPacienteResultado registrar(PacienteDTO datos)
    public List<PacienteDTO> buscar(String filtro)
}
```

- Atributos extra (fecha de nacimiento, teléfono…) **solo** si están en el DER/MDL-01; no inventar campos ni formatos de número de asegurado.
- Concurrencia: si `store` devuelve `false` porque otro hilo insertó el mismo número entre la búsqueda y el guardado, el Service vuelve a consultar y devuelve `YA_EXISTIA` (CP-CNS-06B, parte Service).

## Tareas

- [ ] Escribir entidad, DTO, resultado, interfaz y Service según el contrato.
- [ ] `FakePacienteRepository` en `src/test` (HashMap) que pueda simular el choque concurrente.
- [ ] Pruebas unitarias: nuevo, existente, inválido, búsqueda y choque concurrente (`store` falla → `YA_EXISTIA`).
- [ ] Tabla «Clases MVC» de CU-03 en `MDL-01` actualizada con lo realmente construido.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-06A` — Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes.
- [ ] `CP-CNS-06B` — Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente.
- [ ] `registrar_con_choque_concurrente_devuelve_YA_EXISTIA` (parte Service de CP-CNS-06B).

## Fuera de alcance (no hacer en esta tarjeta)

- JDBC/SQL (S3-05).
- HTTP (S3-06).
- Auto-registro por el propio paciente (DC-07): pendiente de trazabilidad.

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
