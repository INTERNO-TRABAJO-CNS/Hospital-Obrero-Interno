package com.Hospital.CNS.cns_backend.controller;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioSesion;

/**
 * Identifica a quien hace la peticion a partir de su token ({@code Authorization: Bearer <token>}), que se
 * obtiene con POST /api/v1/auth/login. Reemplaza a las cabeceras temporales X-Usuario-Id / X-Rol / X-Persona-Id,
 * que ya no se aceptan (decision PV-11 del 13/11/2026).
 *
 * <p>Uso en un controller: {@code resolutor.resolver(cabeceras::getFirst)}. Sin token o con uno invalido:
 * NO_AUTENTICADO (401); vencido: SESION_VENCIDA (401).</p>
 */
@Component
public class ResolutorUsuario {

    public static final String HEADER_AUTORIZACION = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final AuthService auth;

    public ResolutorUsuario(AuthService auth) {
        this.auth = auth;
    }

    /** @param cabecera funcion que devuelve el valor de un header (o null si no viene). */
    public UsuarioAutenticado resolver(Function<String, String> cabecera) {
        UsuarioSesion u = auth.usuarioDelToken(token(cabecera.apply(HEADER_AUTORIZACION)));
        return new UsuarioAutenticado(u.usuarioId(), u.rol(), u.personaId());
    }

    /** El token de un valor "Bearer xxx" (o null si no tiene ese formato). */
    static String token(String autorizacion) {
        if (autorizacion == null || !autorizacion.regionMatches(true, 0, PREFIJO, 0, PREFIJO.length())) {
            return null;
        }
        String token = autorizacion.substring(PREFIJO.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
