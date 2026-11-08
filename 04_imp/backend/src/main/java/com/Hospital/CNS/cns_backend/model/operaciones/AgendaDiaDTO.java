package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalDate;
import java.util.List;

public record AgendaDiaDTO(
    LocalDate fecha, 
    int cantidad, 
    List<FichaAgendaDTO> fichas
) {}