package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;

/** Fecha en la que no se asignan citas (carga manual, sin calendario oficial). */
public record DiaNoHabilDTO(long diaNoHabilId, LocalDate fecha, String descripcion, boolean activo) {
}
