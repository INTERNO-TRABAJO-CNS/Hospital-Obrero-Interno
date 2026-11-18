-- T100-02 (CU-06, RNF-04): validar disponibilidad sin doble asignacion.
-- Idempotente (IF NOT EXISTS): se puede ejecutar mas de una vez. Va despues de V5 (citas).

-- Respaldo final: un medico no puede tener dos citas vivas a la misma hora (las CANCELADAS no cuentan).
-- Ya lo creaba V5; se deja aqui explicito porque CU-06 depende de el.
CREATE UNIQUE INDEX IF NOT EXISTS ux_cita_medico_fecha_hora
    ON citas.cita (medico_id, fecha, hora) WHERE estado <> 'CANCELADA';

-- Agenda de un medico en un dia: una fila por (medico, fecha) que se bloquea con
-- SELECT ... FOR UPDATE SKIP LOCKED mientras una solicitud valida y registra su cita.
-- La fila se crea la primera vez que alguien pide esa agenda; el bloqueo dura hasta el fin de la transaccion.
CREATE TABLE IF NOT EXISTS citas.agenda_dia (
    medico_id bigint    NOT NULL REFERENCES recursos.medico (id),
    fecha     date      NOT NULL,
    creado_en timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (medico_id, fecha)
);
