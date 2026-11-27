package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {
    long nextIdentity();

    void store(SolicitudDTO s);

    Optional<SolicitudDTO> porId(long solicitudId);

    /** Todas las solicitudes del paciente, de la mas reciente a la mas antigua (Mis citas, CU-04). */
    List<SolicitudDTO> porPaciente(long pacienteId);

    /** PENDIENTE o EN_ESPERA. */
    boolean pacienteTieneSolicitudActivaEnDia(long pacienteId, LocalDate dia);

    /** Orden: prioridad, adulto mayor (60 o mas) primero, ordenLlegada (T100-24). */
    List<SolicitudDTO> siguientesParaProcesar(LocalDate hasta, int max);
}
