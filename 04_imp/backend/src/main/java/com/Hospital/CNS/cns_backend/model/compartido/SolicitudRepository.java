package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {
    long nextIdentity();

    void store(SolicitudDTO s);

    Optional<SolicitudDTO> porId(long solicitudId);

    /** PENDIENTE o EN_ESPERA. */
    boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia);

    /** Orden: prioridad, ordenLlegada. */
    List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max);
}
