# MDL-02 — Recursos médicos, disponibilidad y agenda (Alex)
CU-06/13/14 ↔ RF/HU-06/13/14 · RNF-02/03/04. **Borrador, no código.**

## Entidades propias propuestas
`Medico`, `Especialidad`, `Consultorio`, `AgendaMedica`, `FranjaDisponible` (intervalo inicio-fin, fuente de horario y disponibilidad). Contrastar con ERD ya elaborado.

## Contratos provistos
- Buscar **la primera combinación disponible** según especialidad y día hábil; si hoy no existe continuar hacia día siguiente disponible.
- Verificar que candidato no se superponga a recursos ocupados.
- Consultar agenda propia del médico diaria/semanal; información del paciente por cita, únicamente si autorizada.

## Consumidores
M03 realiza confirmación definitiva usando recursos validados. M04 vuelve a validar para reprogramar. M05 construye vistas de agenda desde datos autorizados. **M02 no crea `CitaMedica`** ni promete cupo libre sin revalidación al guardar.

## Riesgos/pruebas
Choque de consultorios y médico, múltiples médicos en misma hora, turnos feriados, duración de cita, agendas actualizadas entre consulta y confirmación.

## Pendiente
Quién administra agendas/consultorios y feriados; duración del turno; calendario hábil; máximo horizonte de búsqueda. No confundir **enfermería confirma llegada** con **enfermería administra horarios** sin validación.
