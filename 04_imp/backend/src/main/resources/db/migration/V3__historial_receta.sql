-- T100-12 (CU-14) y T100-14 (CU-15): historial clinico y recetas.
-- Idempotente (IF NOT EXISTS): se puede ejecutar mas de una vez. Va despues de V2 (identidad.paciente).
-- Solo PgHistorialRepository conoce estas tablas.

CREATE SCHEMA IF NOT EXISTS clinico;

-- Un historial por paciente; se crea en su primer registro (T100-05) o con su primera receta.
CREATE TABLE IF NOT EXISTS clinico.historial (
    paciente_id bigint    PRIMARY KEY REFERENCES identidad.paciente (id),
    creado_en   timestamp NOT NULL DEFAULT now()
);

CREATE SEQUENCE IF NOT EXISTS clinico.receta_id_seq;

-- cita_id todavia sin FK: la tabla de citas la crea T100-03 (agregar la FK en esa migracion).
-- La receta no se edita; solo se quita si la cita no se pudo finalizar (compensacion de CU-15).
CREATE TABLE IF NOT EXISTS clinico.receta (
    id           bigint        PRIMARY KEY,
    cita_id      bigint        NOT NULL,
    paciente_id  bigint        NOT NULL REFERENCES clinico.historial (paciente_id),
    medicacion   varchar(2000) NOT NULL CHECK (length(btrim(medicacion)) BETWEEN 1 AND 2000),
    fecha_hora   timestamp     NOT NULL,
    numero_ficha integer       NOT NULL CHECK (numero_ficha > 0)
);

CREATE INDEX IF NOT EXISTS ix_receta_paciente ON clinico.receta (paciente_id, fecha_hora);
CREATE INDEX IF NOT EXISTS ix_receta_cita ON clinico.receta (cita_id);
