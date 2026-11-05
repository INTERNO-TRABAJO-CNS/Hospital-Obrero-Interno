# MDL-04 — Llegada, reprogramación, cancelación, atención e historial (Matías)
CU-10/11/12/15/18 ↔ RF/HU-10/11/12/15/18 · RNF-01/03/04. **Borrador, no código.**

## Entidades propias propuestas
`RegistroLlegada`, `CambioCita`, `DatosAtencion` y evento/auditoría de operaciones. **No duplicar `CitaMedica`** de M03.

## Operaciones
- Secretaría registra llegada del paciente para cita propia a su atención; hora y usuario quedan auditados.
- Reprogramar y revalidar de nuevo disponibilidad de M02; persistir a través del contrato de M03. Registrar el cambio; la obligatoriedad de un motivo para reprogramar está PENDIENTE DE VALIDACIÓN.
- Cancelar con motivo (CU-11), estado `CANCELADA`; si se declara inasistencia, personal de Secretaría autoriza tras tolerancia de +10min.
- Registrar asistencia, espera y tiempo de atención conforme CU-15.
- Historial: anterior/nuevo, responsable, motivo, instante.

## Estado
Solo PROGRAMADA→ATENDIDA y PROGRAMADA→CANCELADA; reprogramación conserva PROGRAMADA. Inasistencia no se agrega como cuarto estado.

## Verificación
Denegar cancelación sin motivo; denegar usuario sin permiso; bloquear transición de cancelada a atendida; registrar llegada tardía según política (límite exacto pendiente); mantener historial al cambiar horario.

## Pendiente
Reglas exactas de llegada y tolerancia, manejo de asistencia de médico vs Secretaría, transiciones excepcionales. El médico NO reasigna cupos, según propuesta de cambio.
