# [S2-M02][Arquitectura MVC] Modelo de recursos médicos, disponibilidad y agenda

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Alex |
| Rama (desde `develop`) | `feature/S2-M02-recursos-agenda` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| CU | CU-06, CU-13, CU-14 |
| RF | RF-06, RF-13, RF-14 |
| HU | HU-06, HU-13, HU-14 |
| RNF | RNF-02, RNF-03, RNF-04 |
| Dependencias | Sin bloqueo inicial; acuerda interfaz con S2-M03. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M02` identifican **esta propuesta**, no números de Issues GitHub creados.

Definir la fuente única de disponibilidad y su relación con la agenda médica. **Este módulo no asigna citas por su cuenta**; le facilita validaciones al núcleo de S2-M03.

### Cobertura que debe analizarse
- **CU-06:** validar médico, horario y consultorio sin solapamiento, incluso con concurrencia.
- **CU-13:** médico consulta agenda diaria y semanal.
- **CU-14:** médico consulta información permitida del paciente asociado a una cita.

### Actividades
- [ ] Identificar entidades **Medico, Especialidad, Horario/Turno, Agenda, Consultorio** y posibles cupos, justificando sus límites desde los CU.
- [ ] Definir operaciones `consultarDisponibilidad`, `validarRecursos`, `consultarAgenda` y `obtenerInformacionPermitida` en lenguaje de diseño, sin código.
- [ ] Registrar precondiciones para impedir choques de médico, consultorio y horas, incluyendo dos solicitudes concurrentes.
- [ ] Definir qué módulo es dueño del **recurso/agenda** y qué interfaz consume S2-M03, sin duplicar `CitaMedica`.
- [ ] Documentar vistas mínimas del médico y filtros de autorización para acceder a un paciente.
- [ ] Entregar un diagrama parcial UML y escenarios de verificación.

### Criterios de aceptación
- [ ] Especialidad, horario, médico y consultorio tienen un significado no contradictorio.
- [ ] Se puede representar claramente que una combinación incompatible **no** se confirma.
- [ ] Las consultas de agenda respetan período y rol; CU-14 filtra por relación con cita asignada.
- [ ] Se entrega `03_des/02-arquitecturaGeneral/modelo/MDL-02_recursos_agenda.md` y `diagramas/MDL-02_recursos_agenda.puml`.
- [ ] Se acuerda con S2-M03 el contrato conceptual de disponibilidad y con S2-M05 los datos visibles en agenda.

### Verificación de diseño
- [ ] Caso A: dos solicitudes simultáneas para un mismo consultorio y horario; solo una podría confirmarse.
- [ ] Caso B: médico intenta consultar un paciente ajeno a sus citas y el diseño prohíbe el acceso.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M03](S2-M03_nucleo-citas.md) · [S2-M04](S2-M04_ciclo-vida-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M02-recursos-agenda` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
