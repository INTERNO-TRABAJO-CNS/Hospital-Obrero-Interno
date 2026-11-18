package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Cuenta de acceso. {@code personaId} = pacienteId (rol PACIENTE) o medicoId (rol MEDICO). La clave se guarda
 * solo cifrada ({@code claveCifrada}); este DTO no sale del model hacia el controller.
 */
public record UsuarioDTO(long usuarioId, String usuario, String claveCifrada, RolUsuario rol, long personaId,
                         String nombre, boolean activo) {
}
