-- T100-19 / T100-23 (decision del 10/11/2026): quirofanos y cirugias.
-- Cada quirofano es de UNA especialidad (no se comparten); limpieza de 30 a 45 min despues de cada cirugia.
-- Una sola tabla de cirugias. La base impide cualquier choque con EXCLUDE (cero choques):
--   quirofano: [inicio, fin + limpieza) · cirujano: [inicio, fin) · paciente: [inicio, fin).
-- Las CANCELADAS no ocupan horario. Una cirugia nunca se borra.

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE SCHEMA IF NOT EXISTS quirofano;

CREATE SEQUENCE IF NOT EXISTS quirofano.quirofano_id_seq;
CREATE TABLE IF NOT EXISTS quirofano.quirofano (
    id           integer      PRIMARY KEY DEFAULT nextval('quirofano.quirofano_id_seq'),
    nombre       varchar(40)  NOT NULL CHECK (length(trim(nombre)) > 0),
    especialidad varchar(30)  NOT NULL REFERENCES recursos.especialidad (codigo)
        CHECK (especialidad IN ('GASTROENTEROLOGIA', 'HEMATOLOGIA', 'DERMATOLOGIA')),
    apertura     time         NOT NULL DEFAULT time '07:00',
    cierre       time         NOT NULL DEFAULT time '19:00',
    limpieza_min integer      NOT NULL CHECK (limpieza_min BETWEEN 30 AND 45),
    activo       boolean      NOT NULL DEFAULT true,
    CHECK (apertura < cierre),
    UNIQUE (id, especialidad)
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_quirofano_nombre_activo ON quirofano.quirofano (lower(nombre)) WHERE activo;

-- Numeros de cirugia como en el Figma ("Cirugia N° 58").
CREATE SEQUENCE IF NOT EXISTS quirofano.cirugia_id_seq START WITH 50;
CREATE TABLE IF NOT EXISTS quirofano.cirugia (
    id               bigint      PRIMARY KEY DEFAULT nextval('quirofano.cirugia_id_seq'),
    ficha_interna_id bigint      NOT NULL,
    paciente_id      bigint      NOT NULL REFERENCES identidad.paciente (id),
    cirujano_id      bigint      NOT NULL REFERENCES recursos.medico (id),
    quirofano_id     integer     NOT NULL,
    especialidad     varchar(30) NOT NULL,
    inicio           timestamp   NOT NULL,
    duracion_min     integer     NOT NULL CHECK (duracion_min BETWEEN 15 AND 720),
    limpieza_min     integer     NOT NULL CHECK (limpieza_min BETWEEN 30 AND 45),
    fin              timestamp   GENERATED ALWAYS AS (inicio + make_interval(mins => duracion_min)) STORED,
    fin_con_limpieza timestamp   GENERATED ALWAYS AS (inicio + make_interval(mins => duracion_min + limpieza_min)) STORED,
    gravedad         varchar(5)  NOT NULL CHECK (gravedad IN ('ALTA', 'MEDIA', 'BAJA')),
    urgencia         boolean     NOT NULL DEFAULT false,
    adulto_mayor     boolean     NOT NULL DEFAULT false,
    orden_llegada    bigint      NOT NULL,
    estado           varchar(10) NOT NULL CHECK (estado IN ('PROGRAMADA', 'REALIZADA', 'CANCELADA')),
    creada_en        timestamp   NOT NULL DEFAULT now(),
    -- El quirofano tiene que ser de la misma especialidad de la cirugia.
    FOREIGN KEY (quirofano_id, especialidad) REFERENCES quirofano.quirofano (id, especialidad),
    CONSTRAINT ex_cirugia_quirofano EXCLUDE USING gist
        (quirofano_id WITH =, tsrange(inicio, fin_con_limpieza) WITH &&) WHERE (estado <> 'CANCELADA'),
    CONSTRAINT ex_cirugia_cirujano EXCLUDE USING gist
        (cirujano_id WITH =, tsrange(inicio, fin) WITH &&) WHERE (estado <> 'CANCELADA'),
    CONSTRAINT ex_cirugia_paciente EXCLUDE USING gist
        (paciente_id WITH =, tsrange(inicio, fin) WITH &&) WHERE (estado <> 'CANCELADA')
);
CREATE INDEX IF NOT EXISTS ix_cirugia_inicio ON quirofano.cirugia (inicio);
CREATE INDEX IF NOT EXISTS ix_cirugia_paciente ON quirofano.cirugia (paciente_id);
