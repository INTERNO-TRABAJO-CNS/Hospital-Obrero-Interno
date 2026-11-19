package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {

    long nextIdentity();

    void store(SolicitudDTO solicitud);

    Optional<SolicitudDTO> porId(long solicitudId);

    List<SolicitudDTO> porPaciente(long pacienteId);

    boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia);

    List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max);
}