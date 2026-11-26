package com.Hospital.CNS.cns_backend.infrastructure.ws;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;

import tools.jackson.databind.json.JsonMapper;

/**
 * AgendaEventos real (RNF-08): cada vez que una cita cambia, envia {@code {"tipo":"CITA_CAMBIO","cita":{...}}} a
 * los clientes conectados a /ws/supervision (administrador, CU-19) y a /ws/fichas (fichas del dia). Reemplaza a
 * RegistroAgendaEventos, que solo escribia en el log.
 *
 * <p>Los canales son de solo lectura: lo que envie el cliente se ignora. Quien puede conectarse lo decide
 * {@code controller.AutorizacionWebSocket} en el handshake.</p>
 */
public class AgendaEventosWs implements AgendaEventos {

    private static final Logger LOG = LoggerFactory.getLogger(AgendaEventosWs.class);

    private final JsonMapper json;
    private final Canal supervision = new Canal("supervision");
    private final Canal fichas = new Canal("fichas");

    public AgendaEventosWs(JsonMapper json) {
        this.json = json;
    }

    /** Handler de /ws/supervision. */
    public Canal supervision() {
        return supervision;
    }

    /** Handler de /ws/fichas. */
    public Canal fichas() {
        return fichas;
    }

    @Override
    public void citaCambio(CitaDTO cita) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("tipo", "CITA_CAMBIO");
        evento.put("cita", cita);
        TextMessage mensaje = new TextMessage(json.writeValueAsString(evento));
        LOG.debug("Cita {} {}: se avisa a {} supervisores y {} pantallas de fichas", cita.citaId(), cita.estado(),
                supervision.conectados(), fichas.conectados());
        supervision.difundir(mensaje);
        fichas.difundir(mensaje);
    }

    /** Un canal (una ruta /ws/...) con sus sesiones abiertas. */
    public static final class Canal extends TextWebSocketHandler {

        private final String nombre;
        private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

        private Canal(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession sesion) {
            sesiones.add(sesion);
            LOG.debug("Conectado a /ws/{}: {}", nombre, sesion.getId());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) {
            sesiones.remove(sesion);
        }

        @Override
        protected void handleTextMessage(WebSocketSession sesion, TextMessage mensaje) {
            // Solo lectura: el cliente no envia ordenes por este canal.
        }

        public int conectados() {
            return sesiones.size();
        }

        void difundir(TextMessage mensaje) {
            for (WebSocketSession sesion : sesiones) {
                if (!sesion.isOpen()) {
                    sesiones.remove(sesion);
                    continue;
                }
                try {
                    // Una WebSocketSession no admite dos envios a la vez.
                    synchronized (sesion) {
                        sesion.sendMessage(mensaje);
                    }
                } catch (IOException | RuntimeException e) {
                    LOG.warn("No se pudo avisar por /ws/{} a la sesion {}; se descarta", nombre, sesion.getId(), e);
                    sesiones.remove(sesion);
                }
            }
        }
    }
}
