-- T100-06: persistencia de solicitudes de citas.

CREATE SCHEMA IF NOT EXISTS citas;

CREATE SEQUENCE IF NOT EXISTS citas.solicitud_id_seq;

CREATE TABLE IF NOT EXISTS citas.solicitud (
    id              bigint PRIMARY KEY,
    paciente_id     bigint NOT NULL REFERENCES identidad.paciente(id),
    especialidad    varchar(30) NOT NULL,
    dia_solicitado  date NOT NULL,
    prioridad       integer NOT NULL CHECK (prioridad IN (1, 2)),
    orden_llegada   bigint NOT NULL,
    estado          varchar(25) NOT NULL
        CHECK (estado IN ('PENDIENTE', 'EN_ESPERA', 'ASIGNADA', 'SIN_CUPO')),
    cita_id         bigint REFERENCES citas.cita(id)
);

CREATE INDEX IF NOT EXISTS ix_solicitud_procesamiento
    ON citas.solicitud (dia_solicitado, prioridad DESC, orden_llegada)
    WHERE estado IN ('PENDIENTE', 'EN_ESPERA');

CREATE INDEX IF NOT EXISTS ix_solicitud_paciente
    ON citas.solicitud (paciente_id, dia_solicitado);

CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitud_paciente_dia_activa
    ON citas.solicitud (paciente_id, dia_solicitado)
    WHERE estado IN ('PENDIENTE', 'EN_ESPERA');