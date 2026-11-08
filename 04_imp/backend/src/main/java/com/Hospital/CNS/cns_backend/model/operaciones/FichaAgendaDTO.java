package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalTime;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;

public record FichaAgendaDTO(
    long citaId, 
    int numeroFicha, 
    LocalTime hora, 
    String nombrePaciente, 
    EstadoCita estado
) {}