package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Resultado del login. Mientras no exista Spring Security (S3-08), el cliente envia estos datos en los
 * headers temporales X-Usuario-Id, X-Rol y X-Persona-Id.
 */
public record SesionDTO(long usuarioId, RolUsuario rol, long personaId, String nombre) {
}
