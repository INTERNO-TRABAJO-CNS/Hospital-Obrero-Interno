-- T100-05 (CU-01 y CU-03): tabla interna de afiliados, pacientes y cuentas de acceso.
-- Incluye la fecha de nacimiento del paciente (decision del 10/11/2026: Pediatria < 10 anios, adulto mayor >= 60).
-- Idempotente (IF NOT EXISTS / ON CONFLICT): se puede ejecutar mas de una vez.
-- Solo las clases de infrastructure.pg conocen estas tablas.

CREATE SCHEMA IF NOT EXISTS identidad;

-- Tabla interna de afiliados de la CNS (sin integracion externa). Solo el titular es afiliado.
CREATE TABLE IF NOT EXISTS identidad.afiliado (
    numero_afiliado  varchar(20)  PRIMARY KEY,
    ci               varchar(20)  NOT NULL UNIQUE,
    nombres          varchar(100) NOT NULL,
    apellidos        varchar(100) NOT NULL,
    fecha_nacimiento date         NOT NULL,
    activo           boolean      NOT NULL DEFAULT true
);

CREATE SEQUENCE IF NOT EXISTS identidad.paciente_id_seq;

-- Titulares y beneficiarios. El beneficiario usa la afiliacion del titular y se distingue por su CI.
CREATE TABLE IF NOT EXISTS identidad.paciente (
    id               bigint       PRIMARY KEY,
    numero_afiliado  varchar(20)  NOT NULL REFERENCES identidad.afiliado (numero_afiliado),
    ci               varchar(20)  NOT NULL,
    nombres          varchar(100) NOT NULL,
    apellidos        varchar(100) NOT NULL,
    fecha_nacimiento date         NOT NULL,  -- "no futura" la valida PacienteService
    es_titular       boolean      NOT NULL,
    creado_en        timestamp    NOT NULL DEFAULT now()
);

-- Nunca se duplica un paciente: una persona (CI) por afiliacion y un solo titular por afiliacion.
CREATE UNIQUE INDEX IF NOT EXISTS ux_paciente_afiliado_ci ON identidad.paciente (numero_afiliado, lower(ci));
CREATE UNIQUE INDEX IF NOT EXISTS ux_paciente_titular ON identidad.paciente (numero_afiliado) WHERE es_titular;

CREATE SEQUENCE IF NOT EXISTS identidad.usuario_id_seq;

-- Cuentas de acceso. persona_id = paciente.id (PACIENTE) o medico.id (MEDICO). La clave se guarda cifrada (BCrypt).
CREATE TABLE IF NOT EXISTS identidad.usuario (
    id            bigint       PRIMARY KEY,
    usuario       varchar(60)  NOT NULL,
    clave_cifrada varchar(100) NOT NULL,
    rol           varchar(20)  NOT NULL CHECK (rol IN ('PACIENTE', 'MEDICO', 'ENFERMERIA', 'ADMINISTRADOR')),
    persona_id    bigint       NOT NULL,
    nombre        varchar(200) NOT NULL DEFAULT '',
    activo        boolean      NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_nombre ON identidad.usuario (lower(usuario));

-- 20 afiliados de prueba con edades variadas: 3 menores de 10 anios y 3 de 60 o mas (T100-26).
INSERT INTO identidad.afiliado (numero_afiliado, ci, nombres, apellidos, fecha_nacimiento) VALUES
    ('AF-10001', '4512301', 'Juan Carlos', 'Choque Mamani',   '1985-03-14'),
    ('AF-10002', '5123402', 'Ana María',   'Quispe Flores',   '1990-07-22'),
    ('AF-10003', '3987603', 'Luis Alberto','Condori Ticona',  '1978-11-02'),
    ('AF-10004', '6234504', 'Rosa',        'Mamani Apaza',    '1995-01-30'),
    ('AF-10005', '2876505', 'Mario',       'Huanca Limachi',  '1962-05-18'),
    ('AF-10006', '2345606', 'Elena',       'Vargas Copa',     '1958-09-09'),
    ('AF-10007', '1987607', 'Pedro',       'Laura Calle',     '1950-12-25'),
    ('AF-10008', '7345608', 'Lucía',       'Flores Nina',     '2000-04-11'),
    ('AF-10009', '6987609', 'Jorge',       'Poma Cruz',       '1988-08-03'),
    ('AF-10010', '5678610', 'Carmen',      'Alanoca Rojas',   '1975-02-27'),
    ('AF-10011', '4789611', 'Miguel',      'Ticona Yujra',    '1983-06-15'),
    ('AF-10012', '8123612', 'Sofía',       'Choque Ramos',    '2018-10-05'),
    ('AF-10013', '8234613', 'Diego',       'Mamani Quispe',   '2019-03-21'),
    ('AF-10014', '8345614', 'Valentina',   'Cruz Apaza',      '2020-12-01'),
    ('AF-10015', '5890615', 'Fernando',    'Gutiérrez Paz',   '1992-09-30'),
    ('AF-10016', '6012616', 'Patricia',    'Rojas Villca',    '1987-01-19'),
    ('AF-10017', '4123617', 'Ricardo',     'Chura Mendoza',   '1970-07-07'),
    ('AF-10018', '7234618', 'Gabriela',    'Nina Torrez',     '1998-05-25'),
    ('AF-10019', '3456619', 'Hugo',        'Callisaya Poma',  '1966-10-12'),
    ('AF-10020', '6567620', 'Daniela',     'Apaza Cori',      '1993-03-08')
ON CONFLICT (numero_afiliado) DO NOTHING;
