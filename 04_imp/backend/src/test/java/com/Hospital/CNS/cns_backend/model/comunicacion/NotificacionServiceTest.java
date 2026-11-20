package com.Hospital.CNS.cns_backend.model.comunicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.Hospital.CNS.cns_backend.infrastructure.ws.NotificacionesWs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class NotificacionServiceTest {
    @Test
    void publicar_guardaYDifunde() {
        FakeNotificacionRepository repository = new FakeNotificacionRepository();
        NotificacionService service = new NotificacionService(
                repository,
                new NotificacionesWs(new ObjectMapper()));
        NotificacionDTO notificacion = new NotificacionDTO(
                7L,
                8L,
                "CITA_REGISTRADA",
                "Su cita fue registrada.",
                Instant.parse("2026-11-10T12:00:00Z"));

        service.publicar(notificacion);

        assertEquals(List.of(notificacion), repository.guardadas);
    }

    private static class FakeNotificacionRepository implements NotificacionRepository {
        private final List<NotificacionDTO> guardadas = new ArrayList<>();

        @Override
        public void store(NotificacionDTO notificacion) {
            guardadas.add(notificacion);
        }

        @Override
        public List<NotificacionDTO> delPaciente(long pacienteId) {
            return guardadas;
        }

        @Override
        public void marcarAvisoEnviado(long citaId) {
        }

        @Override
        public boolean avisoYaEnviado(long citaId) {
            return false;
        }
    }
}