package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;

/** Fila de la tabla interna de afiliados (identidad.afiliado). Solo el titular es afiliado. */
public record AfiliadoDTO(String numeroAfiliado, String ci, String nombres, String apellidos, LocalDate fechaNacimiento,
                          boolean activo) {
}
