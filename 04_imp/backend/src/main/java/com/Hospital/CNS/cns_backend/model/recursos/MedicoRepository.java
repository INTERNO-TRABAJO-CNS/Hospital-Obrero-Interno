package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Contrato de persistencia de medicos. La implementacion PostgreSQL es PgMedicoRepository. */
public interface MedicoRepository {
    long nextIdentity();

    /** Inserta o actualiza (por medicoId). La baja logica se guarda con activo=false. */
    void store(MedicoDTO medico);

    Optional<MedicoDTO> porId(long medicoId);

    /** Todos los medicos (activos y dados de baja); con filtro null devuelve todas las especialidades. */
    List<MedicoDTO> listar(TipoEspecialidad filtro);
}
