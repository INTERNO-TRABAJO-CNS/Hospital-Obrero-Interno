package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.List;
import java.util.Optional;

/** Pacientes (titulares y beneficiarios). Implementacion: PgPacienteRepository. */
public interface PacienteRepository {
    long nextIdentity();

    /** false si viola UNIQUE(numero_afiliado, ci) o el unico titular por afiliacion: nunca se duplica. */
    boolean store(PacienteDTO paciente);

    Optional<PacienteDTO> porId(long pacienteId);

    Optional<PacienteDTO> porAfiliadoYCi(String numeroAfiliado, String ci);

    /** Titular y beneficiarios ACTIVOS de una afiliacion (los dados de baja no cuentan), titular primero. */
    List<PacienteDTO> deLaAfiliacion(String numeroAfiliado);
}
