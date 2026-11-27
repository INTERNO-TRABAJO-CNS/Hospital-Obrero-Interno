package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;

/**
 * Fila de Gestionar &gt; Pacientes (administrador). {@code usuario} es el nombre de la cuenta del titular, o null si
 * no tiene (los beneficiarios nunca tienen). {@code activo=false}: dado de baja (baja logica, migracion V13).
 */
public record PacienteAdminDTO(long pacienteId, String numeroAfiliado, String ci, String nombres, String apellidos,
                               LocalDate fechaNacimiento, boolean titular, boolean activo, String usuario) {
}
