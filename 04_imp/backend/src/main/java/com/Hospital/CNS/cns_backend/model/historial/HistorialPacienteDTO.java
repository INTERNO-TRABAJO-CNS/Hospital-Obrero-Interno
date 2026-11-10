package com.Hospital.CNS.cns_backend.model.historial;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.AtencionDTO;

/**
 * CU-14: lo que el medico ve del paciente. Solo datos permitidos para el rol MEDICO: nombre, citas
 * pasadas (fecha y hora), atenciones y recetas. No incluye CI ni datos de contacto ([3a]).
 * Ambas listas van de la mas reciente a la mas antigua.
 */
public record HistorialPacienteDTO(long pacienteId, String nombrePaciente, List<CitaPasadaDTO> citasPasadas,
                                   List<AtencionDTO> atenciones) {
}
