# ARQ-12 — Trazabilidad del Sprint 2

**Fuente:** RF-n ↔ HU-n ↔ CU-n según la matriz elaborada. Los RNF transversales deben revisarse en todas las capas.

| Dueño (CU) | Persona | CU | RF/HU | RNF principales | Producto |
|---|---|---|---|---|---|
| Núcleo de citas y recursos | Benjamín | 02,05,06,07,19,20 | 02,05,06,07,19,20 | 03,04,06,08 | Catálogos/CRUD, validación, asignación, registro de cita, reportes |
| Identidad, solicitud y paciente | Matías | 01,03,04,08,10 | 01,03,04,08,10 | 01,02,05,07 | Login, pacientes, solicitar, mis citas, reprogramar |
| Operación del día y TV | Alejandro | 09,12,13 | 09,12,13 | 07,08 | Listado de fichas (+TV), confirmar asistencia, agenda médico |
| Cancelación e historial | Alex | 11,14,18 | 11,14,18 | 01,03 | Cancelar (+automática), historial paciente, historial de cambios |
| Atención y comunicación | Abigail | 15,16,17 | 15,16,17 | 02,08 | Receta/fin, notificaciones, aviso T-45 |

> Modificado 04/11/2026: sustituye la tabla MDL-01…MDL-05 del 26/10 (reparto anterior por submodelo). Los submodelos MDL-0x siguen siendo documentos de diseño históricos.

**CU-01 a CU-20 cubiertos exactamente una vez como responsabilidad de diseño principal.** Los casos CU-05/06/07/16/17 incluyen procesos automáticos en servicios Java: no requieren una vista Flutter específica.

### Verificación mínima propuesta
| Prueba diseño | CU/RNF | Resultado esperado |
|---|---|---|
| Dos pacientes eligen último cupo simultáneamente | 05/06/07 + RNF-04 | Una sola cita confirmada; la otra pasa a EN_ESPERA (siguiente día hábil) |
| Paciente solicita cita de otro mediante ID modificado | 08 + RNF-01/02 | Denegado en backend |
| No hay cupos hoy, sí mañana hábil | 04/05/06 | NO HAY CUPOS; solicitud EN_ESPERA y asignada el siguiente día hábil |
| No hay cupos en 15 días hábiles | 04/05 | SIN_CUPO e informar, sin cita ficticia |
| Paciente sin confirmar a T-30 | 11/12/18 | Cancelación automática; CANCELADA con motivo y usuario «Sistema» |
| Paciente con una cita el día pide otra (otra especialidad) | 04/06 | Rechazada: una cita por día |
| Médico consulta información de paciente ajeno a su agenda | 14 + RNF-02 | Denegado |
| Cancelación informada antes de persistir | 16 + RNF-03 | No; evento solo posterior a commit |

**Diferido:** la reasignación automática de cupos liberados no se implementa por ahora (DC-24).
