package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AtencionDTO(LocalDate fecha, LocalTime hora, String especialidad, String medico,
                          List<RecetaDTO> recetas) {
}
