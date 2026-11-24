package com.Hospital.CNS.cns_backend.model.comunicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvisoPrevioServiceTest {
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 12, 0);

    private FakeCitaRepository citas;
    private FakeNotificacionRepository repository;
    private FakeNotificacionPublicador publicador;
    private RelojFijo reloj;
    private AvisoPrevioService service;

    @BeforeEach
    void setUp() {
        citas = new FakeCitaRepository();
        repository = new FakeNotificacionRepository();
        publicador = new FakeNotificacionPublicador(repository);
        reloj = new RelojFijo(AHORA);
        service = new AvisoPrevioService(citas, publicador, repository, reloj);
    }

    @Test
    void aviso_enLimiteT45_sePublicaUnaSolaVez() {
        citas.store(cita(1, AHORA.plusMinutes(45), EstadoCita.PROGRAMADA));

        assertEquals(1, service.generarAvisosT45());
        assertEquals("AVISO_T45", publicador.publicadas.get(0).tipo());
        assertEquals(0, service.generarAvisosT45());
        assertEquals(1, publicador.publicadas.size());
    }

    @Test
    void aviso_aT46_noSePublica() {
        citas.store(cita(1, AHORA.plusMinutes(46), EstadoCita.PROGRAMADA));

        assertEquals(0, service.generarAvisosT45());
        assertEquals(0, publicador.publicadas.size());
    }

    @Test
    void aviso_noSePublicaParaCitasVencidasNiConfirmadas() {
        citas.store(cita(1, AHORA.minusMinutes(1), EstadoCita.PROGRAMADA));
        citas.store(cita(2, AHORA.plusMinutes(30), EstadoCita.ASISTENCIA_CONFIRMADA));

        assertEquals(0, service.generarAvisosT45());
        assertEquals(0, publicador.publicadas.size());
    }

    private static CitaDTO cita(long id, LocalDateTime inicio, EstadoCita estado) {
        return new CitaDTO(id, 7, "Paciente", "AF-7", 8, "Medico", TipoEspecialidad.CONSULTORIO_FAMILIAR, 1,
                inicio.toLocalDate(), inicio.toLocalTime(), (int) id, estado);
    }

    private static class FakeNotificacionPublicador implements NotificacionPublicador {
        private final FakeNotificacionRepository repository;
        private final List<NotificacionDTO> publicadas = new java.util.ArrayList<>();

        private FakeNotificacionPublicador(FakeNotificacionRepository repository) {
            this.repository = repository;
        }

        @Override
        public void publicar(NotificacionDTO notificacion) {
            publicadas.add(notificacion);
            repository.guardar(notificacion);
        }
    }

    private static class FakeNotificacionRepository implements NotificacionRepository {
        private final List<NotificacionDTO> notificaciones = new java.util.ArrayList<>();
        private final Set<Long> avisos = new HashSet<>();

        public void guardar(NotificacionDTO notificacion) {
            notificaciones.add(notificacion);
            if ("AVISO_T45".equals(notificacion.tipo()) && notificacion.citaId() != null) {
                avisos.add(notificacion.citaId());
            }
        }

        @Override
        public List<NotificacionDTO> delPaciente(long pacienteId) {
            return notificaciones.stream().filter(n -> n.pacienteId() == pacienteId).toList();
        }

        @Override
        public boolean avisoYaEnviado(long citaId) {
            return avisos.contains(citaId);
        }
    }
}