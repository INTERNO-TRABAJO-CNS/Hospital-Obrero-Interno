-- T100-17 (decision del 10/11/2026): TipoEspecialidad pasa de 3 a 13 valores.
-- V1 solo cargo las 3 basicas; sin estas filas la FK de recursos.medico rechaza a cualquier especialista interno.
-- El cupo diario sigue siendo de 5 a 7 para todos (asi lo muestra el Figma del catalogo de medicos).
-- Idempotente (ON CONFLICT): se puede ejecutar mas de una vez.

INSERT INTO recursos.especialidad (codigo) VALUES
    ('CARDIOLOGIA'),
    ('NEFROLOGIA'),
    ('NEUMOLOGIA'),
    ('ENDOCRINOLOGIA'),
    ('GASTROENTEROLOGIA'),
    ('HEMATOLOGIA'),
    ('INFECTOLOGIA'),
    ('DERMATOLOGIA'),
    ('MEDICINA_TRABAJO'),
    ('TERAPIA_INTENSIVA')
ON CONFLICT (codigo) DO NOTHING;
