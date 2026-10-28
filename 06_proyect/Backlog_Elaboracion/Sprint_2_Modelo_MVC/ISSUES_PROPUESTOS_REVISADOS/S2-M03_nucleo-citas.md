# [S2-M03][Arquitectura MVC] Modelo central Java de solicitudes y asignación

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Benjamín |
| Rama (desde `develop`) | `feature/S2-M03-nucleo-citas` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| CU | CU-04, CU-05, CU-07 |
| RF | RF-04, RF-05, RF-07 |
| HU | HU-04, HU-05, HU-07 |
| RNF | RNF-03, RNF-04, RNF-06 |
| Dependencias | Se inicia paralelo; pacta interfaces con S2-M01 y S2-M02. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M03` identifican **esta propuesta**, no números de Issues GitHub creados.

Diseñar el **núcleo del negocio en Java**, sin código de implementación: desde solicitar cita hasta registrarla correctamente. Esta pieza **es dueña de las entidades SolicitudCita y CitaMedica**; los demás módulos las consultan o invocan operaciones, no las redefinen.

### Cobertura que debe analizarse
- **CU-04:** el paciente solicita por especialidad, **sin elegir** médico, fecha, hora o consultorio.
- **CU-05:** el sistema selecciona automáticamente recursos compatibles disponibles.
- **CU-07:** el sistema registra la cita confirmada sin colisión y prepara notificación.

### Actividades
- [ ] Estudiar los tres CU, sus escenarios alternativos y los RNF-03/04.
- [ ] Modelar **SolicitudCita, CitaMedica** y relaciones con Paciente/Especialidad/Agenda/Consultorio, consumiendo referencias de S2-M01 y S2-M02.
- [ ] Definir estados y transiciones confirmadas por los documentos; **no inventar prioridades de elección ni estados**.
- [ ] Trazar el flujo CU-04 → CU-05 → CU-06 (proveído por S2-M02) → CU-07, con fallos (sin disponibilidad, recurso ocupado durante proceso, datos insuficientes).
- [ ] Definir la responsabilidad de Java en selección, validación, confirmación, transacción y garantía de unicidad; REST solo recibe solicitudes.
- [ ] Identificar los eventos **después** de la confirmación para notificaciones (S2-M05) y operaciones posteriores (S2-M04).

### Criterios de aceptación
- [ ] Se diferencia claramente **solicitud registrada** de **cita confirmada**.
- [ ] Ningún paciente elige manualmente el recurso, médico o horario en CU-04.
- [ ] El diseño exige revalidación y operación atómica para evitar doble reserva.
- [ ] Se representa qué sucede cuando no existe disponibilidad, sin inventar cola o prioridad no aprobada.
- [ ] Se entrega `03_des/02-arquitecturaGeneral/modelo/MDL-03_solicitud_asignacion_cita.md` y `diagramas/MDL-03_solicitud_asignacion_cita.puml`.

### Verificación de diseño
- [ ] Caso A: no hay consultorio libre; no se crea cita confirmada inválida.
- [ ] Caso B: dos solicitudes disputan el último cupo; solo una obtiene confirmación.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M01](S2-M01_identidad-pacientes.md) · [S2-M02](S2-M02_recursos-agenda.md) · [S2-M04](S2-M04_ciclo-vida-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M03-nucleo-citas` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
