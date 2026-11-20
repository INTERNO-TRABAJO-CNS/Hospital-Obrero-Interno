package com.Hospital.CNS.cns_backend.model.comunicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvisoPrevioServiceTest {
    private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");

    private FakeCitaRepository citas;
    private FakeNotificacionRepository repository;
    private FakeNotificacionPublicador publicador;
    private AvisoPrevioService service;

    @BeforeEach
    void setUp() {
        citas = new FakeCitaRepository();
        repository = new FakeNotificacionRepository();
        publicador = new FakeNotificacionPublicador();
        service = new AvisoPrevioService(citas, publicador, repository, () -> AHORA);
    }

    @Test
    void aviso_aT45_genera() {
        citas.citas.add(new CitaProgramada(1L, 7L, AHORA.plusSeconds(45 * 60L), false));

        assertEquals(1, service.generarAvisosT45());
        assertEquals("AVISO_T45", publicador.publicadas.get(0).tipo());
        assertEquals(1, repository.marcadas);
    }

    @Test
    void aviso_aT46_noGenera() {
        citas.citas.add(new CitaProgramada(1L, 7L, AHORA.plusSeconds(46 * 60L), false));

        assertEquals(0, service.generarAvisosT45());
        assertEquals(0, publicador.publicadas.size());
    }

    @Test
    void aviso_noSeRepite() {
        citas.citas.add(new CitaProgramada(1L, 7L, AHORA.plusSeconds(30 * 60L), false));
        repository.enviados.add(1L);

        assertEquals(0, service.generarAvisosT45());
        assertEquals(0, publicador.publicadas.size());
    }

    @Test
    void aviso_citaConfirmada_noGenera() {
        citas.citas.add(new CitaProgramada(1L, 7L, AHORA.plusSeconds(30 * 60L), true));

        assertEquals(0, service.generarAvisosT45());
        assertEquals(0, publicador.publicadas.size());
    }

    private static class FakeCitaRepository implements CitaRepository {
        private final List<CitaProgramada> citas = new ArrayList<>();

        @Override
        public List<CitaProgramada> programadasEntre(Instant desde, Instant hasta) {
            return citas.stream()
                    .filter(cita -> !cita.inicio().isBefore(desde) && !cita.inicio().isAfter(hasta))
                    .toList();
        }
    }

    private static class FakeNotificacionPublicador implements NotificacionPublicador {
        private final List<NotificacionDTO> publicadas = new ArrayList<>();

        @Override
        public void publicar(NotificacionDTO notificacion) {
            publicadas.add(notificacion);
        }
    }

    private static class FakeNotificacionRepository implements NotificacionRepository {
        private final List<Long> enviados = new ArrayList<>();
        private int marcadas;

        @Override
        public void store(NotificacionDTO notificacion) {
        }

        @Override
        public List<NotificacionDTO> delPaciente(long pacienteId) {
            return List.of();
        }

        @Override
        public void marcarAvisoEnviado(long citaId) {
            enviados.add(citaId);
            marcadas++;
        }

        @Override
        public boolean avisoYaEnviado(long citaId) {
            return enviados.contains(citaId);
        }
    }
}