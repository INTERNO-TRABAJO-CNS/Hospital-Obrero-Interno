-- S3-08 / PV-11 (decision del 13/11/2026): login con token guardado en la BD.
-- Se guarda solo el hash SHA-256 del token, nunca el token: si alguien lee la tabla no puede usarlo.
-- Idempotente (IF NOT EXISTS): se puede ejecutar mas de una vez.

CREATE TABLE IF NOT EXISTS identidad.sesion (
    token_hash char(64)  PRIMARY KEY,
    usuario_id bigint    NOT NULL REFERENCES identidad.usuario (id),
    creada_en  timestamp NOT NULL,
    vence_en   timestamp NOT NULL,
    cerrada_en timestamp
);

CREATE INDEX IF NOT EXISTS ix_sesion_usuario ON identidad.sesion (usuario_id);
CREATE INDEX IF NOT EXISTS ix_sesion_vence ON identidad.sesion (vence_en);
