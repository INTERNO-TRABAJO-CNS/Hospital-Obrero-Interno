-- T100-01 (CU-02): catalogos del modulo recursos.
-- Idempotente (IF NOT EXISTS / ON CONFLICT): se puede ejecutar mas de una vez.
-- Solo las clases de infrastructure.pg conocen estas tablas.

CREATE SCHEMA IF NOT EXISTS recursos;

-- Las 3 especialidades salen de TipoEspecialidad (model.compartido). Los rangos de duracion NO se
-- duplican aqui: los valida el model. La tabla solo da integridad referencial al medico.
CREATE TABLE IF NOT EXISTS recursos.especialidad (
    codigo varchar(30) PRIMARY KEY
);

INSERT INTO recursos.especialidad (codigo) VALUES
    ('CONSULTORIO_FAMILIAR'),
    ('ODONTOLOGIA'),
    ('PEDIATRIA')
ON CONFLICT (codigo) DO NOTHING;

CREATE SEQUENCE IF NOT EXISTS recursos.medico_id_seq;
CREATE SEQUENCE IF NOT EXISTS recursos.consultorio_id_seq;
CREATE SEQUENCE IF NOT EXISTS recursos.horario_medico_id_seq;
CREATE SEQUENCE IF NOT EXISTS recursos.dia_no_habil_id_seq;

CREATE TABLE IF NOT EXISTS recursos.medico (
    id                bigint       PRIMARY KEY,
    nombres           varchar(100) NOT NULL,
    apellidos         varchar(100) NOT NULL,
    especialidad      varchar(30)  NOT NULL REFERENCES recursos.especialidad (codigo),
    duracion_cita_min integer      NOT NULL CHECK (duracion_cita_min > 0),
    cupo_maximo_dia   integer      NOT NULL CHECK (cupo_maximo_dia BETWEEN 5 AND 7),
    activo            boolean      NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS recursos.consultorio (
    id     integer      PRIMARY KEY,
    nombre varchar(80)  NOT NULL,
    activo boolean      NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_consultorio_nombre_activo
    ON recursos.consultorio (lower(nombre)) WHERE activo;

-- dia_semana usa la numeracion ISO de java.time.DayOfWeek: 1 = lunes ... 7 = domingo.
CREATE TABLE IF NOT EXISTS recursos.horario_medico (
    id             bigint   PRIMARY KEY,
    medico_id      bigint   NOT NULL REFERENCES recursos.medico (id),
    consultorio_id integer  NOT NULL REFERENCES recursos.consultorio (id),
    dia_semana     smallint NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio    time     NOT NULL,
    hora_fin       time     NOT NULL,
    activo         boolean  NOT NULL DEFAULT true,
    CHECK (hora_inicio < hora_fin)
);

CREATE INDEX IF NOT EXISTS ix_horario_medico_medico ON recursos.horario_medico (medico_id);
CREATE INDEX IF NOT EXISTS ix_horario_medico_consultorio ON recursos.horario_medico (consultorio_id);

CREATE TABLE IF NOT EXISTS recursos.dia_no_habil (
    id          bigint       PRIMARY KEY,
    fecha       date         NOT NULL,
    descripcion varchar(200) NOT NULL,
    activo      boolean      NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_dia_no_habil_fecha_activo
    ON recursos.dia_no_habil (fecha) WHERE activo;
