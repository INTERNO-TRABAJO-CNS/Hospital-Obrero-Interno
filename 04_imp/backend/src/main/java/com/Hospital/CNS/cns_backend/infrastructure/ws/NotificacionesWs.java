package com.Hospital.CNS.cns_backend.infrastructure.ws;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificacionesWs extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NotificacionesWs.class);

    private final ObjectMapper objectMapper;
    private final Map<Long, WebSocketSession> sesiones = new ConcurrentHashMap<>();

    public NotificacionesWs(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long pacienteId = pacienteId(session);
        if (pacienteId != null) {
            sesiones.put(pacienteId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.values().removeIf(actual -> actual.getId().equals(session.getId()));
    }

    public void enviar(NotificacionDTO notificacion) {
        WebSocketSession session = sesiones.get(notificacion.pacienteId());
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(notificacion)));
        } catch (JacksonException | IOException exception) {
            LOG.warn("No se pudo enviar notificacion por WebSocket", exception);
        }
    }

    private Long pacienteId(WebSocketSession session) {
        String value = session.getUri() == null
                ? null
                : session.getUri().getQuery();
        if (value == null || !value.startsWith("pacienteId=")) {
            return null;
        }
        try {
            return Long.parseLong(value.substring("pacienteId=".length()));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}