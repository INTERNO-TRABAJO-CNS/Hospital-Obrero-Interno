CREATE TABLE IF NOT EXISTS notificacion (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    cita_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    creada_en TIMESTAMPTZ NOT NULL,
    aviso_enviado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_notificacion_paciente_creada
    ON notificacion (paciente_id, creada_en DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notificacion_aviso_cita
    ON notificacion (cita_id, tipo)
    WHERE tipo = 'AVISO_T45';