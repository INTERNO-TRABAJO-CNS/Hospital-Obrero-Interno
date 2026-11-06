package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.List;

public record HistorialDTO(long pacienteId, String nombrePaciente, List<AtencionDTO> atenciones) {
}
