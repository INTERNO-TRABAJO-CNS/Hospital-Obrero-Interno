package com.Hospital.CNS.cns_backend.model.operaciones;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.controller.UsuarioAutenticado;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** CU-12: un test por flujo alternativo, con Fakes y RelojFijo (T-30 exacto, T-31 y T-29). */
class ActualizacionEstadoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    private static final LocalTime HORA_CITA = LocalTime.of(10, 0);
    private static final long MEDICO = 100;
    private static final UsuarioAutenticado ENFERMERA =
            new UsuarioAutenticado(9, RolUsuario.ENFERMERIA, 0, "M. Flores");

    private FakeCitaRepository citas;
    private FakeCambioCitaRepository cambios;
    private FakeAgendaEventos eventos;
    private RelojFijo reloj;
    private ActualizacionEstadoService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        cambios = new FakeCambioCitaRepository();
        eventos = new FakeAgendaEventos();
        reloj = new RelojFijo(HOY.atTime(9, 0));
        service = new ActualizacionEstadoService(citas, cambios, eventos, reloj);
    }

    private void citaDeHoy(EstadoCita estado) {
        citas.store(cita(1, 10, MEDICO, HOY, HORA_CITA, 3, estado));
    }

    @Test
    void confirmar_aT31min_ok() {
        citaDeHoy(EstadoCita.PROGRAMADA);
        reloj.fijar(HOY.atTime(9, 29));

        assertEquals(ResultadoConfirmacion.CONFIRMADA, service.confirmarAsistencia(1, ENFERMERA));
        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, citas.porId(1).orElseThrow().estado());
    }

    @Test
    void confirmar_justoEnT30_ok() {
        citaDeHoy(EstadoCita.PROGRAMADA);
        reloj.fijar(HOY.atTime(9, 30));

        assertEquals(ResultadoConfirmacion.CONFIRMADA, service.confirmarAsistencia(1, ENFERMERA));
    }

    @Test
    void confirmar_aT29min_fueraDePlazo() {
        citaDeHoy(EstadoCita.PROGRAMADA);
        reloj.fijar(HOY.atTime(9, 31));

        assertEquals(ResultadoConfirmacion.FUERA_DE_PLAZO, service.confirmarAsistencia(1, ENFERMERA));
        assertEquals(EstadoCita.PROGRAMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(cambios.cambios.isEmpty());
    }

    @Test
    void confirmar_citaDeOtroDia_rechaza() {
        citas.store(cita(1, 10, MEDICO, HOY.plusDays(1), HORA_CITA, 3, EstadoCita.PROGRAMADA));

        assertEquals(ResultadoConfirmacion.NO_ES_DE_HOY, service.confirmarAsistencia(1, ENFERMERA));
    }

    @Test
    void confirmar_rolMedico_rechaza() {
        citaDeHoy(EstadoCita.PROGRAMADA);

        UsuarioAutenticado medico = new UsuarioAutenticado(2, RolUsuario.MEDICO, MEDICO);
        assertEquals(ResultadoConfirmacion.ROL_NO_AUTORIZADO, service.confirmarAsistencia(1, medico));
    }

    @Test
    void confirmar_citaInexistente_noEncontrada() {
        assertEquals(ResultadoConfirmacion.NO_ENCONTRADA, service.confirmarAsistencia(99, ENFERMERA));
    }

    @Test
    void confirmar_yaConfirmada_estadoInvalido() {
        citaDeHoy(EstadoCita.ASISTENCIA_CONFIRMADA);

        assertEquals(ResultadoConfirmacion.ESTADO_INVALIDO, service.confirmarAsistencia(1, ENFERMERA));
    }

    @Test
    void confirmar_registraCambioConNombreYAvisaConEstadoNuevo() {
        citaDeHoy(EstadoCita.PROGRAMADA);

        service.confirmarAsistencia(1, ENFERMERA);

        assertEquals(1, cambios.cambios.size());
        assertEquals("ASISTENCIA_CONFIRMADA", cambios.cambios.get(0).accion());
        assertEquals("Enfermería · M. Flores", cambios.cambios.get(0).usuario());
        assertEquals("Paciente llegó al hospital", cambios.cambios.get(0).motivo());
        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, eventos.eventos.get(0).estado());
    }

    @Test
    void finalizar_desdeConfirmada_atendida() {
        citaDeHoy(EstadoCita.ASISTENCIA_CONFIRMADA);
        LocalDateTime fin = HOY.atTime(10, 20);
        reloj.fijar(fin);

        assertEquals(ResultadoFinalizacion.FINALIZADA, service.finalizar(1, MEDICO));
        assertEquals(EstadoCita.ATENDIDA, citas.porId(1).orElseThrow().estado());
        assertEquals(fin, citas.horasFin.get(1L));
        assertEquals("ATENDIDA", cambios.cambios.get(0).accion());
        assertEquals("Médico · Medico " + MEDICO, cambios.cambios.get(0).usuario());
        assertEquals(EstadoCita.ATENDIDA, eventos.eventos.get(0).estado());
    }

    @Test
    void finalizar_desdeProgramada_rechaza() {
        citaDeHoy(EstadoCita.PROGRAMADA);

        assertEquals(ResultadoFinalizacion.ESTADO_NO_PERMITE, service.finalizar(1, MEDICO));
        assertEquals(EstadoCita.PROGRAMADA, citas.porId(1).orElseThrow().estado());
    }

    @Test
    void finalizar_citaDeOtroMedico_rechaza() {
        citaDeHoy(EstadoCita.ASISTENCIA_CONFIRMADA);

        assertEquals(ResultadoFinalizacion.NO_ES_DE_SU_AGENDA, service.finalizar(1, 555));
    }
}
