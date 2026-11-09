package com.Hospital.CNS.cns_backend.model.recursos;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de consultorios. La implementacion PostgreSQL es PgConsultorioRepository. */
public interface ConsultorioRepository {
    int nextIdentity();

    /** Inserta o actualiza (por consultorioId). La baja logica se guarda con activo=false. */
    void store(ConsultorioDTO consultorio);

    Optional<ConsultorioDTO> porId(int consultorioId);

    /** Todos los consultorios (activos y dados de baja). */
    List<ConsultorioDTO> listar();

    /** true si ya hay un consultorio ACTIVO con ese nombre (sin distinguir mayusculas). */
    boolean existeActivoConNombre(String nombre);
}
