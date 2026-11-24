package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Cuentas de acceso. Implementacion: PgUsuarioRepository.
 *
 * <p>{@code activo} de una cuenta MEDICO tambien exige que su medico siga activo en el catalogo: al dar de baja
 * al medico, su cuenta deja de entrar.</p>
 */
public interface UsuarioRepository {
    long nextIdentity();

    /** Inserta o actualiza (por id). false si el nombre de usuario ya existe (sin distinguir mayusculas). */
    boolean store(UsuarioDTO usuario);

    /** Busca sin distinguir mayusculas. */
    Optional<UsuarioDTO> porUsuario(String usuario);

    Optional<UsuarioDTO> porId(long usuarioId);

    /** Todas las cuentas, ordenadas por rol y usuario. */
    List<UsuarioDTO> listar();

    boolean existeActivoConRol(RolUsuario rol);
}
