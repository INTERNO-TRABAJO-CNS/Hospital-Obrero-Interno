# [S2-DOC-02][Docs][CU-06/CU-13/CU-14] Cerrar MDL-02 (recursos y agenda) y subir el DER real al repo

| Campo | Valor |
|---|---|
| Milestone | Sprint 2 - Arquitectura MVC (vence 2026-10-30) |
| Responsable | Alex (usuario de GitHub por confirmar) |
| Revisor | Abigail |
| Rama (desde `develop`, actualizada: `git merge develop`) | `feature/S2-M02-recursos-agenda (crearla; no existe)` |
| Labels | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| Puntos | 3 |
| Origen | issue propuesto `S2-M02` (esta tarjeta es su **cierre**, no un duplicado) |

## Objetivo
Entregar el submodelo de recursos/agenda **y** el DER/SQL que todo el equipo dice tener. Sin el DER no se puede validar el dominio (`07_base_de_datos.md`, PV-10) ni empezar S3-02.

## Estado actual (medido en el repo el 2026-10-29)

- El repo no muestra commits tuyos (0 de 52 en todas las ramas). Si trabajaste en otra rama sin subir o con otro correo de git, **súbelo hoy**.
- `modelo/MDL-02_recursos_agenda.md` es el borrador de ~1.3 KB del integrador y `diagramas/MDL-02_Recursos.puml` mide 403 bytes.
- `07_base_de_datos.md` afirma que el DER «ya está diseñado», pero **no hay DER ni SQL en el repo**.

## Tareas

- [ ] **Subir el DER y el script SQL reales** a `03_des/03-baseDatos/` (imagen/PDF + fuente editable). Si solo vive en una herramienta (draw.io, Visual Paradigm…), exportar ambas cosas. **Si no existe, decirlo hoy en el grupo**: es lo más urgente del proyecto.
- [ ] Completar atributos de `Medico`, `Especialidad`, `Consultorio`, `AgendaMedica`, `FranjaDisponible` (columna, tipo, nulabilidad) alineados al DER.
- [ ] Agregar la sección «Clases MVC» por CU (tabla de abajo).
- [ ] Pendientes PV-02 (quién crea agendas), PV-03 (duración de cita/horizonte), PV-05 (día hábil): **proponer un valor marcado como propuesta** y llevarlo a la reunión; no se bloquea el sprint por ello.
- [ ] Completar `diagramas/MDL-02_Recursos.puml`.
- [ ] Abrir PR `feature/S2-M02-recursos-agenda` → `develop` (desde `develop` actualizado).

## Casos de uso de este módulo: qué hay que dejar escrito por cada uno

Para **cada** CU, en el `MDL-0X` agregar la sección **«Clases MVC»** con la tabla de abajo (nombres propuestos, ajustables) y el mapeo de cada flujo `[Na]` a un resultado.

### CU-06 — Validar disponibilidad de recursos  (RF-06, Must)

> **HU-06: Como personal de enfermería / admisión, quiero que el sistema valide la disponibilidad de los recursos para evitar citas duplicadas o superpuestas.**  
> **CA:** Dado que existe una posible asignación, cuando alguno de sus recursos ya se encuentre ocupado, entonces el sistema no deberá confirmar esa combinación y deberá considerar otra disponibilidad existente.

- Diagrama: `03_des/01-diagramaCompor/CU-06_ValidarDisponibilidadRecursos.puml`
- Flujos alternativos que el diseño debe resolver: `[2a]` Marca la combinación como no disponible; `[3a]` Marca la combinación como no disponible; `[4a]` Permite una única confirmación, rechaza la duplicada

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | DisponibilidadService (buscarPrimeraCombinacion, validarCombinacionVigente), RecursoCandidato, MedicoRepository, ConsultorioRepository, AgendaRepository |
| Infrastructure (`infrastructure/pg`) | PgMedicoRepository, PgConsultorioRepository, PgAgendaRepository |
| Controller (`controller`) | sin controller: proceso interno |

### CU-13 — Consultar agenda médica  (RF-13, Must)

> **HU-13: Como médico, quiero consultar mi agenda diaria y semanal para conocer las citas que debo atender.**  
> **CA:** Dado que el médico tiene citas asignadas, cuando consulta su agenda diaria o semanal, entonces el sistema deberá mostrar las citas comprendidas en el periodo seleccionado.

- Diagrama: `03_des/01-diagramaCompor/CU-13_ConsultarAgendaMedica (1).puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Impide acceso a agenda médica restringida; `[3a]` Informa que no hay citas programadas

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | AgendaService.agendaDelMedico, CitaResumenDTO |
| Infrastructure (`infrastructure/pg`) | PgAgendaRepository / PgCitaRepository.search |
| Controller (`controller`) | AgendaController  GET /api/v1/medicos/me/agenda |

### CU-14 — Consultar información permitida del paciente  (RF-14, Should)

> **HU-14: Como médico, quiero consultar la información permitida del paciente asignado para contar con los datos necesarios durante la atención.**  
> **CA:** Dado que el médico tiene una cita asignada, cuando accede a sus datos, entonces el sistema deberá mostrar únicamente la información del paciente permitida para su rol.

- Diagrama: `03_des/01-diagramaCompor/CU-14_ConsultarDatosPaciente.puml`
- Flujos alternativos que el diseño debe resolver: `[1a]` Impide consultar datos del paciente; `[3a]` Oculta datos restringidos

| Capa | Clases propuestas |
|---|---|
| Model (entidad, DTO, Repository-interfaz, Service) | PacienteService.datosPermitidos(citaId, medicoId), PacienteParaMedicoDTO |
| Infrastructure (`infrastructure/pg`) | PgPacienteRepository |
| Controller (`controller`) | CitaController  GET /api/v1/citas/{id}/paciente-autorizado |

Rutas tomadas del borrador `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md` (ARQ-06, no aprobado). Si una decisión está marcada PENDIENTE (PV-xx), **proponer y marcar como propuesta**: no bloquear la entrega.

## Definition of Done

- [ ] `.md` y `.puml` del módulo completos, en la ruta correcta, y coherentes con el DER real.
- [ ] Sección «Clases MVC» por cada CU del módulo.
- [ ] PR a `develop` con la rama al día (`git merge develop` antes de abrir el PR), revisado y aprobado por otra persona.
- [ ] Entregado **antes del 2026-10-30**; si no se logra, se avisa hoy y se registra como arrastre al Sprint 3 (no se entrega callado fuera de fecha).

## Registro
Issue `#____` · PR `#____`
