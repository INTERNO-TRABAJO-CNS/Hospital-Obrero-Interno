package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** Cuenta tal como la ve el administrador: nunca incluye la contrasena ni su hash. */
public record CuentaDTO(long usuarioId, String usuario, RolUsuario rol, long personaId, String nombre,
                        boolean activo) {

    static CuentaDTO desde(UsuarioDTO u) {
        return new CuentaDTO(u.usuarioId(), u.usuario(), u.rol(), u.personaId(), u.nombre(), u.activo());
    }
}
