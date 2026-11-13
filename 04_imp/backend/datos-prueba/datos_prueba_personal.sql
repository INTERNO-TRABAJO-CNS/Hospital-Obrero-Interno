-- Datos de PRUEBA para ver las pantallas del personal con el backend real (NO es una migracion: no lo ejecuta
-- Flyway y nunca debe correrse en produccion). Se puede ejecutar varias veces: no duplica nada.
--
--   psql -h localhost -U postgres -d CNS_prueba1 -f datos-prueba/datos_prueba_personal.sql
--
-- Crea consultorios, medicos (como en el Figma), horarios, pacientes beneficiarios de la tabla interna de
-- afiliados y la agenda de HOY del Dr. Ramos (Medico General) con los 4 estados de cita. No crea cuentas:
-- el administrador las crea desde la app (Catalogos > Cuentas) o con POST /api/v1/admin/usuarios.
-- Requiere las migraciones V1..V6 (el backend las aplica al arrancar).

DO $$
DECLARE
    c1 integer; c2 integer; c3 integer; c4 integer;
    ramos bigint; quispe bigint;
    p_marcos bigint; p_ana bigint; p_elena bigint; p_luis bigint; p_rosa bigint; p_jorge bigint;
    cita bigint;
BEGIN
    -- Consultorios -----------------------------------------------------------------------------------------
    PERFORM 1 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 1' AND activo;
    IF NOT FOUND THEN INSERT INTO recursos.consultorio (id, nombre) VALUES (nextval('recursos.consultorio_id_seq'), 'Consultorio 1'); END IF;
    PERFORM 1 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 2' AND activo;
    IF NOT FOUND THEN INSERT INTO recursos.consultorio (id, nombre) VALUES (nextval('recursos.consultorio_id_seq'), 'Consultorio 2'); END IF;
    PERFORM 1 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 3' AND activo;
    IF NOT FOUND THEN INSERT INTO recursos.consultorio (id, nombre) VALUES (nextval('recursos.consultorio_id_seq'), 'Consultorio 3'); END IF;
    PERFORM 1 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 4' AND activo;
    IF NOT FOUND THEN INSERT INTO recursos.consultorio (id, nombre) VALUES (nextval('recursos.consultorio_id_seq'), 'Consultorio 4'); END IF;
    SELECT id INTO c1 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 1' AND activo;
    SELECT id INTO c2 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 2' AND activo;
    SELECT id INTO c3 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 3' AND activo;
    SELECT id INTO c4 FROM recursos.consultorio WHERE lower(nombre) = 'consultorio 4' AND activo;

    -- Medicos (nombres del Figma) -------------------------------------------------------------------------
    INSERT INTO recursos.medico (id, nombres, apellidos, especialidad, duracion_cita_min, cupo_maximo_dia)
    SELECT nextval('recursos.medico_id_seq'), d.nombres, d.apellidos, d.especialidad, d.duracion, d.cupo
    FROM (VALUES
        ('Carlos', 'Ramos', 'CONSULTORIO_FAMILIAR', 40, 7),
        ('Lucia', 'Quispe', 'PEDIATRIA', 35, 6),
        ('Jorge', 'Mamani', 'ODONTOLOGIA', 50, 5),
        ('Raul', 'Vargas', 'GASTROENTEROLOGIA', 25, 6),
        ('Patricia', 'Arce', 'GASTROENTEROLOGIA', 25, 7),
        ('Daniela', 'Salinas', 'DERMATOLOGIA', 20, 7),
        ('Miguel', 'Paredes', 'CARDIOLOGIA', 30, 5),
        ('Hugo', 'Lima', 'TERAPIA_INTENSIVA', 45, 5)
    ) AS d (nombres, apellidos, especialidad, duracion, cupo)
    WHERE NOT EXISTS (SELECT 1 FROM recursos.medico m WHERE m.nombres = d.nombres AND m.apellidos = d.apellidos);
    SELECT id INTO ramos FROM recursos.medico WHERE nombres = 'Carlos' AND apellidos = 'Ramos' ORDER BY id LIMIT 1;
    SELECT id INTO quispe FROM recursos.medico WHERE nombres = 'Lucia' AND apellidos = 'Quispe' ORDER BY id LIMIT 1;

    -- Horarios: Dr. Ramos lunes a viernes 08:00-12:00 (Consultorio 1); Dra. Quispe 08:00-12:00 (Consultorio 2)
    INSERT INTO recursos.horario_medico (id, medico_id, consultorio_id, dia_semana, hora_inicio, hora_fin)
    SELECT nextval('recursos.horario_medico_id_seq'), h.medico, h.consultorio, h.dia, time '08:00', time '12:00'
    FROM (SELECT ramos AS medico, c1 AS consultorio, d AS dia FROM generate_series(1, 5) d
          UNION ALL SELECT quispe, c2, d FROM generate_series(1, 5) d) h
    WHERE NOT EXISTS (SELECT 1 FROM recursos.horario_medico x
                      WHERE x.medico_id = h.medico AND x.dia_semana = h.dia AND x.activo);

    -- Pacientes: beneficiarios de afiliados de la tabla interna (V2) --------------------------------------
    INSERT INTO identidad.paciente (id, numero_afiliado, ci, nombres, apellidos, fecha_nacimiento, es_titular)
    SELECT nextval('identidad.paciente_id_seq'), p.afiliado, p.ci, p.nombres, p.apellidos, p.nacimiento, false
    FROM (VALUES
        ('AF-10003', 'PR-0001', 'Marcos', 'Condori', date '1979-04-02'),
        ('AF-10004', 'PR-0002', 'Ana', 'Mamani', date '1994-08-15'),
        ('AF-10006', 'PR-0003', 'Elena', 'Copa', date '1957-02-10'),
        ('AF-10009', 'PR-0004', 'Luis', 'Quispe', date '1986-12-01'),
        ('AF-10010', 'PR-0005', 'Rosa', 'Choque', date '1972-06-21'),
        ('AF-10011', 'PR-0006', 'Jorge', 'Flores', date '1990-09-09')
    ) AS p (afiliado, ci, nombres, apellidos, nacimiento)
    WHERE NOT EXISTS (SELECT 1 FROM identidad.paciente x WHERE x.numero_afiliado = p.afiliado AND lower(x.ci) = lower(p.ci));
    INSERT INTO clinico.historial (paciente_id)
    SELECT id FROM identidad.paciente WHERE ci LIKE 'PR-%' ON CONFLICT (paciente_id) DO NOTHING;
    SELECT id INTO p_marcos FROM identidad.paciente WHERE ci = 'PR-0001';
    SELECT id INTO p_ana FROM identidad.paciente WHERE ci = 'PR-0002';
    SELECT id INTO p_elena FROM identidad.paciente WHERE ci = 'PR-0003';
    SELECT id INTO p_luis FROM identidad.paciente WHERE ci = 'PR-0004';
    SELECT id INTO p_rosa FROM identidad.paciente WHERE ci = 'PR-0005';
    SELECT id INTO p_jorge FROM identidad.paciente WHERE ci = 'PR-0006';

    -- Agenda de HOY del Dr. Ramos (como el Figma «WEB MEDICO AGENDA») -------------------------------------
    INSERT INTO citas.cita (id, paciente_id, medico_id, consultorio_id, especialidad, fecha, hora, numero_ficha, estado)
    SELECT nextval('citas.cita_id_seq'), a.paciente, ramos, c1, 'CONSULTORIO_FAMILIAR', current_date, a.hora, a.ficha, a.estado
    FROM (VALUES
        (p_marcos, time '08:00', 1, 'ATENDIDA'),
        (p_ana,    time '08:45', 2, 'ATENDIDA'),
        (p_elena,  time '09:30', 3, 'ASISTENCIA_CONFIRMADA'),
        (p_luis,   time '10:15', 4, 'PROGRAMADA'),
        (p_rosa,   time '11:00', 5, 'PROGRAMADA'),
        (p_jorge,  time '11:45', 6, 'CANCELADA')
    ) AS a (paciente, hora, ficha, estado)
    WHERE NOT EXISTS (SELECT 1 FROM citas.cita x WHERE x.medico_id = ramos AND x.fecha = current_date AND x.hora = a.hora);

    -- Una atencion anterior de Marcos Condori (para el historial) -----------------------------------------
    SELECT id INTO cita FROM citas.cita
    WHERE medico_id = ramos AND paciente_id = p_marcos AND fecha = current_date - 30 LIMIT 1;
    IF cita IS NULL THEN
        cita := nextval('citas.cita_id_seq');
        INSERT INTO citas.cita (id, paciente_id, medico_id, consultorio_id, especialidad, fecha, hora, numero_ficha, estado, hora_fin)
        VALUES (cita, p_marcos, ramos, c1, 'CONSULTORIO_FAMILIAR', current_date - 30, time '09:30', 3, 'ATENDIDA',
                (current_date - 30) + time '10:05');
        INSERT INTO clinico.receta (id, cita_id, paciente_id, medicacion, fecha_hora, numero_ficha)
        VALUES (nextval('clinico.receta_id_seq'), cita, p_marcos,
                'Losartan 50 mg, 1 comprimido cada 24 horas por 30 dias. Control de presion arterial en 1 mes.',
                (current_date - 30) + time '10:05', 3);
    END IF;
END $$;
