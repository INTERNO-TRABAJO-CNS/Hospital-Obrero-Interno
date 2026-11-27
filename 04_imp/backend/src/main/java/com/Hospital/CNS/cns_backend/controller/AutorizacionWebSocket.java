package com.Hospital.CNS.cns_backend.controller;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * Handshake de los WebSocket: solo deja conectar con un token de sesion valido de uno de los roles permitidos. El
 * navegador no puede mandar el header Authorization al abrir un WebSocket, por eso el token viaja como
 * {@code ?token=...} (el mismo que entrega POST /api/v1/auth/login). Sin token, vencido o de otro rol: 401/403 y no
 * se abre la conexion.
 */
public class AutorizacionWebSocket implements HandshakeInterceptor {

    private final ResolutorUsuario resolutor;
    private final Set<RolUsuario> roles;

    public AutorizacionWebSocket(ResolutorUsuario resolutor, RolUsuario primero, RolUsuario... resto) {
        this.resolutor = resolutor;
        this.roles = EnumSet.of(primero, resto);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest peticion, ServerHttpResponse respuesta, WebSocketHandler handler,
                                   Map<String, Object> atributos) {
        String token = UriComponentsBuilder.fromUri(peticion.getURI()).build().getQueryParams().getFirst("token");
        try {
            UsuarioAutenticado usuario = resolutor.resolver(
                    h -> ResolutorUsuario.HEADER_AUTORIZACION.equals(h) && token != null ? "Bearer " + token : null);
            if (!roles.contains(usuario.rol())) {
                respuesta.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }
            atributos.put("usuarioId", usuario.usuarioId());
            atributos.put("personaId", usuario.personaId());
            return true;
        } catch (DominioException sinSesion) {
            respuesta.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest peticion, ServerHttpResponse respuesta, WebSocketHandler handler,
                               Exception error) {
        // Nada que hacer.
    }
}
