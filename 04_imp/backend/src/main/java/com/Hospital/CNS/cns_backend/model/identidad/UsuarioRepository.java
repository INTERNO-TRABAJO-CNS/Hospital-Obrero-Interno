package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.Optional;

/** Cuentas de acceso. Implementacion: PgUsuarioRepository. */
public interface UsuarioRepository {
    long nextIdentity();

    /** false si el nombre de usuario ya existe (sin distinguir mayusculas). */
    boolean store(UsuarioDTO usuario);

    /** Busca sin distinguir mayusculas. */
    Optional<UsuarioDTO> porUsuario(String usuario);
}
