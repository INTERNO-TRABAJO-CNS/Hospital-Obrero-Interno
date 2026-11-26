package com.Hospital.CNS.cns_backend.infrastructure.ws;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import tools.jackson.databind.ObjectMapper;

class NotificacionesWsTest {

    @Test
    void soloRegistraSesionAutenticadaParaSuPropioPaciente() throws Exception {
        NotificacionesWs websocket = new NotificacionesWs(new ObjectMapper());
        WebSocketSession sesion = mock(WebSocketSession.class);
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("personaId", 7L);
        when(sesion.getAttributes()).thenReturn(atributos);
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost/ws/notificaciones?pacienteId=7&token=token"));
        when(sesion.isOpen()).thenReturn(true);
        when(sesion.getId()).thenReturn("sesion-7");
        websocket.afterConnectionEstablished(sesion);

        websocket.enviar(new NotificacionDTO(
                1, 7, 8L, null, null, "AVISO_T45", "Aviso", LocalDateTime.of(2026, 11, 10, 12, 0)));

        org.mockito.ArgumentCaptor<TextMessage> mensaje = org.mockito.ArgumentCaptor.forClass(TextMessage.class);
        verify(sesion).sendMessage(mensaje.capture());
        assertTrue(mensaje.getValue().getPayload().contains("\"citaId\":8"));
    }

    @Test
    void rechazaSuscripcionAOtroPaciente() throws Exception {
        NotificacionesWs websocket = new NotificacionesWs(new ObjectMapper());
        WebSocketSession sesion = mock(WebSocketSession.class);
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("personaId", 7L);
        when(sesion.getAttributes()).thenReturn(atributos);
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost/ws/notificaciones?pacienteId=8&token=token"));

        websocket.afterConnectionEstablished(sesion);

        verify(sesion).close(CloseStatus.POLICY_VIOLATION);
    }
}
