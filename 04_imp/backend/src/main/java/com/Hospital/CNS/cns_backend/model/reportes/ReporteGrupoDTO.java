package com.Hospital.CNS.cns_backend.model.reportes;

/**
 * CU-19: cantidad de un periodo agrupada por especialidad (solicitudes) o por consultorio (fichas atendidas).
 *
 * @param grupo codigo de la especialidad o nombre del consultorio
 */
public record ReporteGrupoDTO(String grupo, int cantidad) {
}
