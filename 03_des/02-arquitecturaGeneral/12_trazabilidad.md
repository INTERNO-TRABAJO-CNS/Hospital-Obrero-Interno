# ARQ-12 — Trazabilidad del Sprint 2

**Fuente:** RF-n ↔ HU-n ↔ CU-n según la matriz elaborada. Los RNF transversales deben revisarse en todas las capas.

| Submodelo | Propietario | CU | RF/HU | RNF principales | Producto |
|---|---|---|---|---|---|
| MDL-01 | Alejandro | 01,02,03 | 01,02,03 | 01,02,05 | Cuenta / identidad / afiliación |
| MDL-02 | Alex | 06,13,14 | 06,13,14 | 02,03,04 | Agenda, recursos y disponibilidad |
| MDL-03 | Benjamín | 04,05,07 | 04,05,07 | 03,04,06 | Solicitud y cita confirmada |
| MDL-04 | Matías | 10,11,12,15,18 | 10,11,12,15,18 | 01,03,04 | Operaciones, atención, cambios |
| MDL-05 | Abigal | 08,09,16,17,19,20 | 08,09,16,17,19,20 | 01,02,05,06 | Lecturas, avisos, reportes |

**CU-01 a CU-20 cubiertos exactamente una vez como responsabilidad de diseño principal.** Los casos CU-05/06/07/16/17 incluyen procesos automáticos en servicios Java: no requieren una vista Flutter específica.

### Verificación mínima propuesta
| Prueba diseño | CU/RNF | Resultado esperado |
|---|---|---|
| Dos pacientes eligen último cupo simultáneamente | 05/06/07 + RNF-04 | Una sola cita confirmada; la otra busca siguiente opción |
| Paciente solicita cita de otro mediante ID modificado | 08 + RNF-01/02 | Denegado en backend |
| No hay cupos hoy, sí mañana hábil | 04/05/06 | Asignar primera hora válida siguiente, no elegir por paciente |
| No hay cupos dentro del horizonte configurado | 04/05 | Informar, sin cita ficticia |
| Enfermería registra cancelación por inasistencia con motivo | 11/12/18 | Estado CANCELADA y rastro de usuario/tiempo |
| Médico consulta información de paciente ajeno a su agenda | 14 + RNF-02 | Denegado |
| Cancelación informada antes de persistir | 16 + RNF-03 | No; evento solo posterior a commit |

**Cambio propuesto:** oferta de cupo liberado se documenta separadamente y no se cuenta como caso CU-21 aprobado.
