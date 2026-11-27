-- T100-03 (CU-05/CU-07), parte de persistencia: tabla de citas para PgCitaRepository.
-- El algoritmo de asignacion (AsignacionService) y la tabla de solicitudes siguen en T100-03.
-- Idempotente (IF NOT EXISTS): se puede ejecutar mas de una vez. Va despues de V1 (recursos) y V2 (identidad).

CREATE SCHEMA IF NOT EXISTS citas;

CREATE SEQUENCE IF NOT EXISTS citas.cita_id_seq;

-- Una cita nunca se borra: se pasa a CANCELADA. La especialidad se guarda en la cita para que el historial
-- no cambie si mas adelante cambia la del medico.
CREATE TABLE IF NOT EXISTS citas.cita (
    id             bigint      PRIMARY KEY,
    paciente_id    bigint      NOT NULL REFERENCES identidad.paciente (id),
    medico_id      bigint      NOT NULL REFERENCES recursos.medico (id),
    consultorio_id integer     NOT NULL REFERENCES recursos.consultorio (id),
    especialidad   varchar(30) NOT NULL REFERENCES recursos.especialidad (codigo),
    fecha          date        NOT NULL,
    hora           time        NOT NULL,
    numero_ficha   integer     NOT NULL CHECK (numero_ficha > 0),
    estado         varchar(25) NOT NULL
        CHECK (estado IN ('PROGRAMADA', 'ASISTENCIA_CONFIRMADA', 'ATENDIDA', 'CANCELADA')),
    hora_fin       timestamp,
    creado_en      timestamp   NOT NULL DEFAULT now()
);

-- Un medico no puede tener dos citas vivas a la misma hora (las CANCELADAS no cuentan).
CREATE UNIQUE INDEX IF NOT EXISTS ux_cita_medico_fecha_hora
    ON citas.cita (medico_id, fecha, hora) WHERE estado <> 'CANCELADA';

CREATE INDEX IF NOT EXISTS ix_cita_fecha ON citas.cita (fecha);
CREATE INDEX IF NOT EXISTS ix_cita_paciente ON citas.cita (paciente_id, fecha);
CREATE INDEX IF NOT EXISTS ix_cita_consultorio ON citas.cita (consultorio_id) WHERE estado = 'PROGRAMADA';

-- La receta ya guardaba su cita (V3) sin FK porque la tabla no existia. NOT VALID: no revisa filas viejas
-- de pruebas, pero toda receta nueva debe apuntar a una cita real.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_receta_cita') THEN
        ALTER TABLE clinico.receta
            ADD CONSTRAINT fk_receta_cita FOREIGN KEY (cita_id) REFERENCES citas.cita (id) NOT VALID;
    END IF;
END $$;
