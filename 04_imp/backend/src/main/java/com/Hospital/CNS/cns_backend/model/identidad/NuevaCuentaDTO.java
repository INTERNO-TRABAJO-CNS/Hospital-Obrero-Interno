package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Cuenta del personal que crea el administrador (POST /api/v1/admin/usuarios).
 * {@code personaId}: el medicoId para el rol MEDICO; se ignora en ENFERMERIA y ADMINISTRADOR.
 * {@code nombre}: para MEDICO puede ir vacio (se toma del catalogo).
 */
public record NuevaCuentaDTO(String usuario, String clave, RolUsuario rol, Long personaId, String nombre) {
}
