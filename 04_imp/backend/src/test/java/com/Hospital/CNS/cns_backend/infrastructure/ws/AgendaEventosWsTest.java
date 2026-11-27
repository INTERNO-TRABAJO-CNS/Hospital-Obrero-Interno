package com.Hospital.CNS.cns_backend.infrastructure.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

import tools.jackson.databind.json.JsonMapper;

/** RNF-08: AgendaEventosWs difunde cada cambio de cita a los suscritos de /ws/supervision y /ws/fichas. */
class AgendaEventosWsTest {

    private final AgendaEventosWs eventos = new AgendaEventosWs(JsonMapper.builder().build());

    private static final CitaDTO CITA = new CitaDTO(15, 100, "Ana Paz", "AF-100", 7, "Dr. Ramos",
            TipoEspecialidad.CONSULTORIO_FAMILIAR, 4, LocalDate.of(2026, 11, 23), LocalTime.of(9, 30), 3,
            EstadoCita.ASISTENCIA_CONFIRMADA);

    private static WebSocketSession sesion(String id) {
        WebSocketSession s = mock(WebSocketSession.class);
        when(s.getId()).thenReturn(id);
        when(s.isOpen()).thenReturn(true);
        return s;
    }

    @Test
    void ws_difundeAlSuscrito() throws Exception {
        WebSocketSession admin = sesion("a");
        eventos.supervision().afterConnectionEstablished(admin);

        eventos.citaCambio(CITA);

        ArgumentCaptor<TextMessage> enviado = ArgumentCaptor.forClass(TextMessage.class);
        verify(admin).sendMessage(enviado.capture());
        String json = enviado.getValue().getPayload();
        assertTrue(json.contains("\"tipo\":\"CITA_CAMBIO\""), json);
        assertTrue(json.contains("\"citaId\":15"), json);
        assertTrue(json.contains("\"estado\":\"ASISTENCIA_CONFIRMADA\""), json);
        assertTrue(json.contains("\"fecha\":\"2026-11-23\""), json);
    }

    @Test
    void ws_difundeEnLosDosCanales() throws Exception {
        WebSocketSession admin = sesion("a");
        WebSocketSession enfermeria = sesion("b");
        eventos.supervision().afterConnectionEstablished(admin);
        eventos.fichas().afterConnectionEstablished(enfermeria);

        eventos.citaCambio(CITA);

        verify(admin).sendMessage(any(TextMessage.class));
        verify(enfermeria).sendMessage(any(TextMessage.class));
    }

    @Test
    void ws_noEnviaAlQueSeDesconecto() throws Exception {
        WebSocketSession admin = sesion("a");
        eventos.supervision().afterConnectionEstablished(admin);
        eventos.supervision().afterConnectionClosed(admin, CloseStatus.NORMAL);

        eventos.citaCambio(CITA);

        verify(admin, never()).sendMessage(any(TextMessage.class));
        assertEquals(0, eventos.supervision().conectados());
    }

    @Test
    void ws_sesionRota_seDescartaYSigueConLasDemas() throws Exception {
        WebSocketSession rota = sesion("rota");
        WebSocketSession sana = sesion("sana");
        doThrow(new IOException("cerrada")).when(rota).sendMessage(any(TextMessage.class));
        eventos.supervision().afterConnectionEstablished(rota);
        eventos.supervision().afterConnectionEstablished(sana);

        eventos.citaCambio(CITA);

        verify(sana).sendMessage(any(TextMessage.class));
        assertEquals(1, eventos.supervision().conectados());
    }
}
