package com.Hospital.CNS.cns_backend.infrastructure.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.Hospital.CNS.cns_backend.controller.AutorizacionWebSocket;
import com.Hospital.CNS.cns_backend.controller.ResolutorUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final NotificacionesWs handler;
    private final ResolutorUsuario resolutor;
    private final String[] origenes;

    public WebSocketConfig(
            NotificacionesWs handler,
            ResolutorUsuario resolutor,
            @Value("${cns.web.origen:http://localhost:5050}") String origen) {
        this.handler = handler;
        this.resolutor = resolutor;
        this.origenes = origen.split("\\s*,\\s*");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/notificaciones")
                .addInterceptors(new AutorizacionWebSocket(resolutor, RolUsuario.PACIENTE))
                .setAllowedOriginPatterns(origenes);
    }
}