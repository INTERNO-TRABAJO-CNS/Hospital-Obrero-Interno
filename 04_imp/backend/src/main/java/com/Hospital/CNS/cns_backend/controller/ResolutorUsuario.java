package com.Hospital.CNS.cns_backend.controller;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * TEMPORAL: lo reemplaza Spring Security de S3-08.
 * Lee el usuario de los headers X-Usuario-Id, X-Rol y X-Persona-Id para poder
 * probar con Postman y con el cliente Flutter sin login real.
 *
 * Uso en un controller: {@code resolutor.resolver(request::getHeader)}.
 */
@Component
public class ResolutorUsuario {

    public static final String HEADER_USUARIO_ID = "X-Usuario-Id";
    public static final String HEADER_ROL = "X-Rol";
    public static final String HEADER_PERSONA_ID = "X-Persona-Id";

    /** @param cabecera funcion que devuelve el valor de un header (o null si no viene). */
    public UsuarioAutenticado resolver(Function<String, String> cabecera) {
        return resolver(
                cabecera.apply(HEADER_USUARIO_ID),
                cabecera.apply(HEADER_ROL),
                cabecera.apply(HEADER_PERSONA_ID));
    }

    public UsuarioAutenticado resolver(String usuarioId, String rol, String personaId) {
        long usuario = numero(HEADER_USUARIO_ID, usuarioId);
        long persona = numero(HEADER_PERSONA_ID, personaId);
        return new UsuarioAutenticado(usuario, rol(rol), persona);
    }

    private static long numero(String header, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DominioException("ENCABEZADO_FALTANTE", "Falta el encabezado " + header);
        }
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            throw new DominioException("ENCABEZADO_INVALIDO", "El encabezado " + header + " debe ser un numero");
        }
    }

    private static RolUsuario rol(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DominioException("ENCABEZADO_FALTANTE", "Falta el encabezado " + HEADER_ROL);
        }
        try {
            return RolUsuario.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DominioException("ROL_INVALIDO", "Rol desconocido: " + valor);
        }
    }
}
