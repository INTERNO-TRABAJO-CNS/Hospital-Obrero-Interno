# MDL-03 — Solicitud, selección automática, confirmación (Benjamín)
CU-04/05/07 ↔ RF/HU-04/05/07 · RNF-03/04/06. **Borrador, no código.**

## Entidades propias propuestas
**Único dueño de `SolicitudCita` y `CitaMedica`**, sin copias en M02/M04. Cita referencia paciente de M01 y médico/agenda/consultorio de M02.

## Flujo acordado
1. Recepcionar `especialidadId` de paciente autenticado. No permitir elegir horario/médico/consultorio.
2. Registrar solicitud; consultar en M02 primera combinación compatible por fecha/hora cercana.
3. Evaluar validez: especialidad, agenda y consultorio.
4. Revalidar/guardar `PROGRAMADA` en transacción única; protección PostgreSQL contra doble reserva.
5. Emitir evento posterior a commit a M05; devolver confirmación REST.
6. Si conflicto: siguiente candidato; sin recursos: no crear cita.

## Contratos provistos
`solicitar`, `asignarProxima`, `confirmarAtomico`, `obtenerCita`, `aplicarCambioAtomico`; interfaces conceptuales del integrador, no Java final.

## Consumidores
M04 solo modifica mediante contrato de M03; M05 notifica/consulta; no establecer reglas de WS en el motor.

## Verificación
Dos solicitudes para último cupo: exactamente una confirmación; se descarta la candidata ocupada; pase al próximo día hábil; no hay disponibilidad: sin cita ficticia. Explicar límites de horizonte y desempate como pendientes.

## Futuro cambio
Cadena de ofertas de reasignación en Java, **solo al aprobar formalmente** alcance; no equiparar con asignación de solicitud inicial.
