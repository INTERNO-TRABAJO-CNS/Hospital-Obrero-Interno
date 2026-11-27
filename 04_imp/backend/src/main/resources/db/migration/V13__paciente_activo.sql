-- Gestionar > Pacientes (administrador): baja logica de pacientes. Un paciente dado de baja no aparece en su
-- grupo familiar y, si es titular, su cuenta deja de entrar (ver PgUsuarioRepository). Nada se borra.
ALTER TABLE identidad.paciente ADD COLUMN IF NOT EXISTS activo boolean NOT NULL DEFAULT true;
