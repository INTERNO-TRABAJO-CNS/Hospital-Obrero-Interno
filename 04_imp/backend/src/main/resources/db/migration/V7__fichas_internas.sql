-- T100-17 / T100-20 (decision del 10/11/2026): fichas internas en 20 tablas (10 especialidades internas x
-- tabla general y pediatrica), separadas de las citas comunes. Sin confirmacion de asistencia; estados
-- EN_ESPERA, ASIGNADA, ATENDIDA y CANCELADA (solo "no se presento"). Una ficha nunca se borra.
-- Ademas: una sola derivacion por cita, auditoria de cambios y los avisos al paciente (decision del 15/11/2026).

CREATE SCHEMA IF NOT EXISTS fichas_internas;

-- Un solo numero de ticket para las 20 tablas (el "Ticket N° 1024" del Figma).
CREATE SEQUENCE IF NOT EXISTS fichas_internas.ficha_id_seq START WITH 1001;

DO $$
DECLARE
    esp text;
    tabla text;
BEGIN
    FOREACH esp IN ARRAY ARRAY['cardiologia', 'nefrologia', 'neumologia', 'endocrinologia', 'gastroenterologia',
                               'hematologia', 'infectologia', 'dermatologia', 'medicina_trabajo', 'terapia_intensiva']
    LOOP
        FOREACH tabla IN ARRAY ARRAY[esp, esp || '_pediatria']
        LOOP
            EXECUTE format($f$
                CREATE TABLE IF NOT EXISTS fichas_internas.%I (
                    id                  bigint        PRIMARY KEY DEFAULT nextval('fichas_internas.ficha_id_seq'),
                    paciente_id         bigint        NOT NULL REFERENCES identidad.paciente (id),
                    cita_origen_id      bigint        REFERENCES citas.cita (id),
                    medico_derivante_id bigint        NOT NULL REFERENCES recursos.medico (id),
                    medico_id           bigint        REFERENCES recursos.medico (id),
                    consultorio_id      integer       REFERENCES recursos.consultorio (id),
                    fecha               date,
                    hora                time,
                    duracion_min        integer       NOT NULL CHECK (duracion_min > 0),
                    gravedad            varchar(5)    NOT NULL CHECK (gravedad IN ('ALTA', 'MEDIA', 'BAJA')),
                    urgencia            boolean       NOT NULL DEFAULT false,
                    adulto_mayor        boolean       NOT NULL DEFAULT false,
                    orden_llegada       bigint        NOT NULL,
                    motivo              varchar(500)  NOT NULL CHECK (length(trim(motivo)) > 0),
                    observaciones       varchar(2000),
                    estado              varchar(10)   NOT NULL
                        CHECK (estado IN ('EN_ESPERA', 'ASIGNADA', 'ATENDIDA', 'CANCELADA')),
                    creada_en           timestamp     NOT NULL DEFAULT now(),
                    CHECK ((estado = 'EN_ESPERA') = (fecha IS NULL)),
                    CHECK ((fecha IS NULL) = (hora IS NULL) AND (fecha IS NULL) = (medico_id IS NULL))
                )$f$, tabla);
            EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON fichas_internas.%I (medico_id, fecha)',
                           'ix_' || tabla || '_medico_fecha', tabla);
            EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON fichas_internas.%I (paciente_id)',
                           'ix_' || tabla || '_paciente', tabla);
        END LOOP;
    END LOOP;
END $$;

-- Una sola derivacion por cita: la PK la hace cumplir aunque dos medicos lo intenten a la vez.
CREATE TABLE IF NOT EXISTS fichas_internas.derivacion (
    cita_origen_id bigint PRIMARY KEY REFERENCES citas.cita (id),
    ficha_id       bigint NOT NULL UNIQUE
);

-- Las 20 tablas juntas (solo lectura): todas las consultas del sistema pasan por aqui.
CREATE OR REPLACE VIEW fichas_internas.v_todas AS
          SELECT 'CARDIOLOGIA'::varchar(30) AS especialidad, false AS pediatrica, f.* FROM fichas_internas.cardiologia f
UNION ALL SELECT 'CARDIOLOGIA', true, f.* FROM fichas_internas.cardiologia_pediatria f
UNION ALL SELECT 'NEFROLOGIA', false, f.* FROM fichas_internas.nefrologia f
UNION ALL SELECT 'NEFROLOGIA', true, f.* FROM fichas_internas.nefrologia_pediatria f
UNION ALL SELECT 'NEUMOLOGIA', false, f.* FROM fichas_internas.neumologia f
UNION ALL SELECT 'NEUMOLOGIA', true, f.* FROM fichas_internas.neumologia_pediatria f
UNION ALL SELECT 'ENDOCRINOLOGIA', false, f.* FROM fichas_internas.endocrinologia f
UNION ALL SELECT 'ENDOCRINOLOGIA', true, f.* FROM fichas_internas.endocrinologia_pediatria f
UNION ALL SELECT 'GASTROENTEROLOGIA', false, f.* FROM fichas_internas.gastroenterologia f
UNION ALL SELECT 'GASTROENTEROLOGIA', true, f.* FROM fichas_internas.gastroenterologia_pediatria f
UNION ALL SELECT 'HEMATOLOGIA', false, f.* FROM fichas_internas.hematologia f
UNION ALL SELECT 'HEMATOLOGIA', true, f.* FROM fichas_internas.hematologia_pediatria f
UNION ALL SELECT 'INFECTOLOGIA', false, f.* FROM fichas_internas.infectologia f
UNION ALL SELECT 'INFECTOLOGIA', true, f.* FROM fichas_internas.infectologia_pediatria f
UNION ALL SELECT 'DERMATOLOGIA', false, f.* FROM fichas_internas.dermatologia f
UNION ALL SELECT 'DERMATOLOGIA', true, f.* FROM fichas_internas.dermatologia_pediatria f
UNION ALL SELECT 'MEDICINA_TRABAJO', false, f.* FROM fichas_internas.medicina_trabajo f
UNION ALL SELECT 'MEDICINA_TRABAJO', true, f.* FROM fichas_internas.medicina_trabajo_pediatria f
UNION ALL SELECT 'TERAPIA_INTENSIVA', false, f.* FROM fichas_internas.terapia_intensiva f
UNION ALL SELECT 'TERAPIA_INTENSIVA', true, f.* FROM fichas_internas.terapia_intensiva_pediatria f;

-- Auditoria: quien hizo que y cuando (fichas y, desde la parte de quirofanos, cirugias).
CREATE SEQUENCE IF NOT EXISTS fichas_internas.cambio_id_seq;
CREATE TABLE IF NOT EXISTS fichas_internas.cambio (
    id         bigint       PRIMARY KEY DEFAULT nextval('fichas_internas.cambio_id_seq'),
    objeto     varchar(10)  NOT NULL CHECK (objeto IN ('FICHA', 'CIRUGIA')),
    objeto_id  bigint       NOT NULL,
    accion     varchar(30)  NOT NULL,
    usuario    varchar(120) NOT NULL,
    fecha_hora timestamp    NOT NULL DEFAULT now(),
    motivo     varchar(500)
);
CREATE INDEX IF NOT EXISTS ix_cambio_objeto ON fichas_internas.cambio (objeto, objeto_id);

-- Avisos al paciente (decision del 15/11/2026): se guardan aqui; la pantalla que los muestra es de T100-30.
CREATE SCHEMA IF NOT EXISTS notificacion;
CREATE SEQUENCE IF NOT EXISTS notificacion.notificacion_id_seq;
CREATE TABLE IF NOT EXISTS notificacion.notificacion (
    id          bigint       PRIMARY KEY DEFAULT nextval('notificacion.notificacion_id_seq'),
    paciente_id bigint       NOT NULL REFERENCES identidad.paciente (id),
    cita_id     bigint,
    ficha_id    bigint,
    cirugia_id  bigint,
    tipo        varchar(40)  NOT NULL,
    mensaje     varchar(500) NOT NULL,
    fecha_hora  timestamp    NOT NULL DEFAULT now(),
    leida       boolean      NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS ix_notificacion_paciente ON notificacion.notificacion (paciente_id, fecha_hora DESC);
