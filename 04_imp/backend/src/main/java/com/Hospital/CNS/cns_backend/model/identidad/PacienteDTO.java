package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;

/**
 * Paciente registrado. {@code numeroAfiliado} es la afiliacion del titular (los beneficiarios usan la de su
 * titular y se distinguen por su CI). {@code fechaNacimiento} es obligatoria (decision del 10/11/2026: edad para
 * Pediatria &lt; 10 y adulto mayor &gt;= 60).
 */
public record PacienteDTO(long pacienteId, String numeroAfiliado, String ci, String nombres, String apellidos,
                          LocalDate fechaNacimiento, boolean titular) {

    public String nombreCompleto() {
        return ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
    }
}
