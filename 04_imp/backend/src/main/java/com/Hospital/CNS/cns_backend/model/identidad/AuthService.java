package com.Hospital.CNS.cns_backend.model.identidad;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * CU-01: iniciar sesion. [2a] credenciales invalidas (usuario inexistente, inactivo o clave incorrecta) ->
 * CREDENCIALES_INVALIDAS (401) con el mismo mensaje en todos los casos. [3a] las funciones segun el rol las
 * aplica cada controller con el rol de la sesion.
 */
public class AuthService {

    private final UsuarioRepository usuarios;
    private final CifradorClave cifrador;

    public AuthService(UsuarioRepository usuarios, CifradorClave cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    public SesionDTO login(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw invalidas();
        }
        UsuarioDTO u = usuarios.porUsuario(usuario.trim()).orElseThrow(AuthService::invalidas);
        if (!u.activo() || !cifrador.coincide(clave, u.claveCifrada())) {
            throw invalidas();
        }
        return new SesionDTO(u.usuarioId(), u.rol(), u.personaId(), u.nombre());
    }

    private static DominioException invalidas() {
        return new DominioException("CREDENCIALES_INVALIDAS", "Usuario o contrasena incorrectos");
    }
}
