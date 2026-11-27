package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** Comprobacion de rol comun a los controllers. Lee el usuario con el ResolutorUsuario TEMPORAL. */
final class Permisos {

    private Permisos() {
    }

    /** Devuelve el usuario si es MEDICO; si no, ACCESO_DENEGADO (403). Su personaId es el medicoId. */
    static UsuarioAutenticado exigirMedico(ResolutorUsuario resolutor, HttpHeaders cabeceras) {
        return exigir(resolutor, cabeceras, "Solo un medico puede usar esta funcion", RolUsuario.MEDICO);
    }

    /** Devuelve el usuario si su rol es uno de {@code roles}; si no, ACCESO_DENEGADO (403). */
    static UsuarioAutenticado exigir(ResolutorUsuario resolutor, HttpHeaders cabeceras, String mensaje,
                                     RolUsuario... roles) {
        UsuarioAutenticado usuario = resolutor.resolver(cabeceras::getFirst);
        for (RolUsuario rol : roles) {
            if (usuario.rol() == rol) {
                return usuario;
            }
        }
        throw new DominioException("ACCESO_DENEGADO", mensaje);
    }
}
