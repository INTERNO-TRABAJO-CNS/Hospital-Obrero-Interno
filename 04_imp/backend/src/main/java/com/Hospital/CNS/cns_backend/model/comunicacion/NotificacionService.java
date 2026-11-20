package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.util.Objects;

import com.Hospital.CNS.cns_backend.infrastructure.ws.NotificacionesWs;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService implements NotificacionPublicador {
    private final NotificacionRepository repository;
    private final NotificacionesWs websocket;

    public NotificacionService(NotificacionRepository repository, NotificacionesWs websocket) {
        this.repository = Objects.requireNonNull(repository);
        this.websocket = Objects.requireNonNull(websocket);
    }

    @Override
    public void publicar(NotificacionDTO notificacion) {
        repository.store(notificacion);
        websocket.enviar(notificacion);
    }
}