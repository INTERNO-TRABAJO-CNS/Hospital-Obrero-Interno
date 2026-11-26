-- T100-13 (CU-18): la auditoria pertenece al esquema de operaciones.
-- V10 ya creo citas.cambio_cita; moverla conserva sus registros existentes.
CREATE SCHEMA IF NOT EXISTS operaciones;

DO $$
BEGIN
    IF to_regclass('citas.cambio_cita') IS NOT NULL
            AND to_regclass('operaciones.cambio_cita') IS NULL THEN
        ALTER TABLE citas.cambio_cita SET SCHEMA operaciones;
    END IF;

    IF to_regclass('citas.cambio_cita_id_seq') IS NOT NULL
            AND to_regclass('operaciones.cambio_cita_id_seq') IS NULL THEN
        ALTER SEQUENCE citas.cambio_cita_id_seq SET SCHEMA operaciones;
    END IF;

    IF to_regclass('operaciones.cambio_cita') IS NOT NULL
            AND to_regclass('operaciones.cambio_cita_id_seq') IS NOT NULL THEN
        ALTER SEQUENCE operaciones.cambio_cita_id_seq OWNED BY operaciones.cambio_cita.id;
    END IF;
END $$;
