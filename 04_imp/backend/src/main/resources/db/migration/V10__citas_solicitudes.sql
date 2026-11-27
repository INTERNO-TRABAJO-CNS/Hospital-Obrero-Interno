-- T100-03 (CU-05 / CU-07): cola de solicitudes y auditoria de cambios de citas.
-- La tabla citas.cita ya existe (V5) con su UNIQUE parcial (medico, fecha, hora) reafirmado en V9.
-- Idempotente (IF NOT EXISTS): se puede ejecutar mas de una vez. Va despues de V5 y V9.

CREATE SEQUENCE IF NOT EXISTS citas.solicitud_id_seq;
CREATE SEQUENCE IF NOT EXISTS citas.solicitud_orden_seq;

-- Solicitud de cita (CU-04): el paciente elige especialidad y dia; CU-05 le asigna medico, hora y consultorio.
-- prioridad: 1 normal, 2 tras cancelar o reprogramar. cita_id queda en null hasta que la solicitud pasa a ASIGNADA.
CREATE TABLE IF NOT EXISTS citas.solicitud (
    id             bigint      PRIMARY KEY,
    paciente_id    bigint      NOT NULL REFERENCES identidad.paciente (id),
    especialidad   varchar(30) NOT NULL REFERENCES recursos.especialidad (codigo),
    dia_solicitado date        NOT NULL,
    prioridad      smallint    NOT NULL CHECK (prioridad IN (1, 2)),
    orden_llegada  bigint      NOT NULL DEFAULT nextval('citas.solicitud_orden_seq'),
    estado         varchar(10) NOT NULL CHECK (estado IN ('PENDIENTE', 'EN_ESPERA', 'ASIGNADA', 'SIN_CUPO')),
    cita_id        bigint      REFERENCES citas.cita (id),
    creado_en      timestamp   NOT NULL DEFAULT now(),
    actualizado_en timestamp   NOT NULL DEFAULT now(),
    CHECK ((estado = 'ASIGNADA') = (cita_id IS NOT NULL))
);

-- La cola: solo las activas, en el orden en que se procesan.
CREATE INDEX IF NOT EXISTS ix_solicitud_cola
    ON citas.solicitud (prioridad, orden_llegada) WHERE estado IN ('PENDIENTE', 'EN_ESPERA');
CREATE INDEX IF NOT EXISTS ix_solicitud_paciente_dia ON citas.solicitud (paciente_id, dia_solicitado);
-- Una cita sale de una sola solicitud.
CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitud_cita ON citas.solicitud (cita_id) WHERE cita_id IS NOT NULL;

-- Auditoria de las citas (quien hizo que y cuando). Nunca se borra.
CREATE SEQUENCE IF NOT EXISTS citas.cambio_cita_id_seq;
CREATE TABLE IF NOT EXISTS citas.cambio_cita (
    id         bigint       PRIMARY KEY DEFAULT nextval('citas.cambio_cita_id_seq'),
    cita_id    bigint       NOT NULL REFERENCES citas.cita (id),
    accion     varchar(40)  NOT NULL,
    usuario    varchar(60)  NOT NULL,
    fecha_hora timestamp    NOT NULL DEFAULT now(),
    motivo     varchar(500)
);
CREATE INDEX IF NOT EXISTS ix_cambio_cita_cita ON citas.cambio_cita (cita_id);
CREATE INDEX IF NOT EXISTS ix_cambio_cita_fecha ON citas.cambio_cita (fecha_hora);
