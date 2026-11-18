package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Quien hace la peticion, segun su token. Se lee de la cuenta en cada peticion: si el administrador la da de
 * baja, el token deja de servir al instante.
 */
public record UsuarioSesion(long usuarioId, RolUsuario rol, long personaId, String nombre) {
}
