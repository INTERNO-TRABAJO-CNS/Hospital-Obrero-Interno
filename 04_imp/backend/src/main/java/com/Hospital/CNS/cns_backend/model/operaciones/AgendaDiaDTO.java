package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalDate;
import java.util.List;

/** CU-13: agenda de un dia del medico. {@code cantidad} = numero de fichas listadas (incluye CANCELADAS). */
public record AgendaDiaDTO(LocalDate fecha, int cantidad, List<FichaAgendaDTO> fichas) {
}
