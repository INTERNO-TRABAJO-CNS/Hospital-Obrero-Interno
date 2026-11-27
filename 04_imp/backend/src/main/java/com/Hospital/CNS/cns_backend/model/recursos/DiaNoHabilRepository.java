package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de dias no habiles. La implementacion PostgreSQL es PgDiaNoHabilRepository. */
public interface DiaNoHabilRepository {
    long nextIdentity();

    /** Inserta o actualiza (por diaNoHabilId). */
    void store(DiaNoHabilDTO dia);

    Optional<DiaNoHabilDTO> porId(long diaNoHabilId);

    /** Todos los dias no habiles, ordenados por fecha. */
    List<DiaNoHabilDTO> listar();

    /** true si esa fecha ya esta registrada como dia no habil activo. */
    boolean existeActivoEnFecha(LocalDate fecha);
}
