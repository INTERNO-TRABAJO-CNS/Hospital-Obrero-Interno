package com.Hospital.CNS.cns_backend.model.reportes;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;

/**
 * CU-19: cantidad de citas de un periodo en un estado.
 *
 * @param automaticas solo para CANCELADA: las que cancelo el sistema (no confirmo asistencia hasta T-30)
 */
public record ReporteEstadoDTO(EstadoCita estado, int cantidad, int automaticas) {
}
