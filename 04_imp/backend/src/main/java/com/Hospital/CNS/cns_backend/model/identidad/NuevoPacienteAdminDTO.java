package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;

/**
 * Datos que envia el administrador para crear o modificar un paciente. Al crear un titular, si se da
 * {@code usuario} tambien se crea su cuenta (con {@code clave}). Al modificar solo se usan nombres, apellidos y
 * fecha de nacimiento: la afiliacion y el CI identifican al paciente y no cambian.
 */
public record NuevoPacienteAdminDTO(boolean titular, String numeroAfiliado, String ci, String nombres,
                                    String apellidos, LocalDate fechaNacimiento, String usuario, String clave) {
}
