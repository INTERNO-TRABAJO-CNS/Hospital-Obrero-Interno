package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de horarios. La implementacion PostgreSQL es PgHorarioRepository. */
public interface HorarioRepository {
    long nextIdentity();

    /** Inserta o actualiza (por horarioId). */
    void store(HorarioDTO horario);

    Optional<HorarioDTO> porId(long horarioId);

    /** Todos los horarios (activos y dados de baja). */
    List<HorarioDTO> listar();

    /** Horarios de un medico. */
    List<HorarioDTO> delMedico(long medicoId);

    /** Horarios que usan un consultorio. */
    List<HorarioDTO> delConsultorio(int consultorioId);
}
