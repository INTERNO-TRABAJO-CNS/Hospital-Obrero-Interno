# [S2-M04][Arquitectura MVC] Modelo de reprogramación, cancelación, atención y auditoría

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Matías |
| Rama (desde `develop`) | `feature/S2-M04-ciclo-vida-citas` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| CU | CU-10, CU-11, CU-12, CU-15, CU-18 |
| RF | RF-10, RF-11, RF-12, RF-15, RF-18 |
| HU | HU-10, HU-11, HU-12, HU-15, HU-18 |
| RNF | RNF-02, RNF-03, RNF-04 |
| Dependencias | Se inicia paralelo; consume el identificador/operaciones de S2-M03 y disponibilidad S2-M02. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M04` identifican **esta propuesta**, no números de Issues GitHub creados.

Diseñar las **operaciones sobre citas ya existentes**, separadas del alta de cita de S2-M03. S2-M04 **no define de nuevo CitaMedica**: trabaja sobre su identificador y operaciones acordadas.

### Cobertura que debe analizarse
- **CU-10:** reprogramación autorizada y revalidación de recursos.
- **CU-11:** cancelación con motivo e historial.
- **CU-12:** actualizar estado por médico o personal autorizado según fuente vigente.
- **CU-15:** registrar asistencia, tiempo de espera y atención dentro del alcance administrativo.
- **CU-18:** conservar y consultar acciones, responsable y momento.

### Actividades
- [ ] Extraer flujos principales y alternativas de los cinco CU.
- [ ] Definir operaciones `reprogramar`, `cancelar`, `actualizarEstado`, `registrarDatosAtencion`, `registrarCambio`, con quién puede invocarlas según requisitos **vigentes**.
- [ ] Diseñar el registro conceptual **HistorialCambio** (actor, acción, fecha/hora, objeto afectado y motivo cuando corresponda).
- [ ] Usar disponibilidad de S2-M02 y CitaMedica de S2-M03: indicar cuándo se vuelve a validar y cuáles cambios son transaccionales.
- [ ] Identificar discrepancia del RF-12 («inexistente» frente a «inasistencia») como **PENDIENTE**, sin corregir unilateralmente la documentación base.
- [ ] Identificar que la reasignación por inasistencia/cancelación solicitada recientemente corresponde a **S2-M07 / cambio futuro**, no a un CU aprobado para implementación en este sprint.
- [ ] Acordar con S2-M05 qué mensajes emite cada cambio de estado confirmado.

### Criterios de aceptación
- [ ] Se diferencian reprogramar, cancelar, actualizar estado y registrar atención.
- [ ] El historial registra responsable, acción, fecha/hora y motivo de cancelación.
- [ ] Ninguna reprogramación viola disponibilidad; no hay pérdida de la cita anterior al fallar.
- [ ] No se atribuye al médico o a Secretaría una facultad nueva sin control de cambios.
- [ ] Se entrega `03_des/02-arquitecturaGeneral/modelo/MDL-04_operaciones_historial.md` y `diagramas/MDL-04_operaciones_historial.puml`.

### Verificación de diseño
- [ ] Caso A: intento de reprogramación a un horario ocupado; la cita no cambia.
- [ ] Caso B: cancelación autorizada conserva el motivo, autor y fecha en historial.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M02](S2-M02_recursos-agenda.md) · [S2-M03](S2-M03_nucleo-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [S2-M07](S2-M07_cambio-reasignacion.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M04-ciclo-vida-citas` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
