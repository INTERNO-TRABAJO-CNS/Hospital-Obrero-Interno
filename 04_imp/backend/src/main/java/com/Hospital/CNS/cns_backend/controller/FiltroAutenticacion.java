package com.Hospital.CNS.cns_backend.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.web.filter.OncePerRequestFilter;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Defensa central: toda peticion a /api/** necesita un token valido, salvo las publicas (login y registro del
 * titular). Asi, un controller nuevo que olvide pedir el rol igual queda protegido. El rol de cada funcion lo
 * siguen revisando los controllers con {@link Permisos}.
 *
 * <p>No es un @Component: lo agrega ConfiguracionSeguridad a la cadena de Spring Security (si fuera bean,
 * Spring lo registraria dos veces).</p>
 */
public class FiltroAutenticacion extends OncePerRequestFilter {

    /** "METODO ruta" que no necesitan sesion. */
    private static final Set<String> PUBLICAS = Set.of(
            "POST /api/v1/auth/login",
            "POST /api/v1/pacientes/titular");

    private final AuthService auth;

    public FiltroAutenticacion(AuthService auth) {
        this.auth = auth;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return !ruta.startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || PUBLICAS.contains(request.getMethod().toUpperCase() + " " + ruta);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        try {
            auth.usuarioDelToken(ResolutorUsuario.token(request.getHeader(ResolutorUsuario.HEADER_AUTORIZACION)));
        } catch (DominioException e) {
            responder401(response, e.getCodigo(), e.getMessage());
            return;
        }
        cadena.doFilter(request, response);
    }

    private static void responder401(HttpServletResponse response, String codigo, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"codigo\":\"" + json(codigo) + "\",\"mensaje\":\"" + json(mensaje) + "\"}");
    }

    private static String json(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
