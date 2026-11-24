package com.Hospital.CNS.cns_backend.infrastructure.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificacionesWs extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NotificacionesWs.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sesiones = new ConcurrentHashMap<>();

    public NotificacionesWs(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Long personaId = (Long) session.getAttributes().get("personaId");
        Long pacienteId = pacienteId(session);
        if (personaId == null || !personaId.equals(pacienteId)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sesiones.computeIfAbsent(personaId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long personaId = (Long) session.getAttributes().get("personaId");
        if (personaId != null) {
            sesiones.computeIfPresent(personaId, (id, conexiones) -> {
                conexiones.remove(session);
                return conexiones.isEmpty() ? null : conexiones;
            });
        }
    }

    public void enviar(NotificacionDTO notificacion) {
        Set<WebSocketSession> conexiones = sesiones.get(notificacion.pacienteId());
        if (conexiones == null) {
            return;
        }
        final TextMessage mensaje;
        try {
            mensaje = new TextMessage(objectMapper.writeValueAsString(notificacion));
        } catch (JacksonException exception) {
            LOG.warn("No se pudo serializar la notificacion {}", notificacion.tipo(), exception);
            return;
        }
        for (WebSocketSession session : conexiones) {
            if (!session.isOpen()) {
                conexiones.remove(session);
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(mensaje);
                }
            } catch (IOException | RuntimeException exception) {
                LOG.warn("No se pudo enviar notificacion por WebSocket a la sesion {}", session.getId(), exception);
                conexiones.remove(session);
            }
        }
    }

    private Long pacienteId(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        String value = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("pacienteId");
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}