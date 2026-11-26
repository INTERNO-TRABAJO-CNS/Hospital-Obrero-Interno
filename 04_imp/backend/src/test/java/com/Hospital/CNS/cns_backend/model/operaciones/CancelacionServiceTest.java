package com.Hospital.CNS.cns_backend.model.operaciones;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;

class CancelacionServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 10, 0);
    private static final LocalDate HOY = AHORA.toLocalDate();
    private static final long PACIENTE = 12;

    private FakeCitaRepository citas;
    private FakeCambioCitaRepository cambios;
    private FakeNotificacionPublicador notificaciones;
    private FakeAgendaEventos eventos;
    private CancelacionService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        cambios = new FakeCambioCitaRepository();
        notificaciones = new FakeNotificacionPublicador();
        eventos = new FakeAgendaEventos();
        service = new CancelacionService(citas, cambios, notificaciones, eventos, new RelojFijo(AHORA));
    }

    @Test
    void cancelarPorPaciente_ok_conservaCita() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(11, 0), 3, EstadoCita.PROGRAMADA));

        ResultadoCancelacion resultado = service.cancelarPorPaciente(1, PACIENTE);

        assertEquals(ResultadoCancelacion.CANCELADA, resultado);
        assertEquals(EstadoCita.CANCELADA, citas.porId(1).orElseThrow().estado());
        assertEquals(1, citas.citas.size());
        assertEquals("Cancelada por el paciente", cambios.cambios.get(0).motivo());
        assertEquals("Paciente " + PACIENTE, cambios.cambios.get(0).usuario());
        assertEquals("CITA_CANCELADA", notificaciones.publicadas.get(0).tipo());
        assertEquals(EstadoCita.CANCELADA, eventos.eventos.get(0).estado());
    }

    @Test
    void cancelarPorPaciente_ajena() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(11, 0), 3, EstadoCita.PROGRAMADA));

        assertEquals(ResultadoCancelacion.NO_ES_DEL_PACIENTE, service.cancelarPorPaciente(1, PACIENTE + 1));

        assertEquals(EstadoCita.PROGRAMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(cambios.cambios.isEmpty());
        assertTrue(notificaciones.publicadas.isEmpty());
        assertTrue(eventos.eventos.isEmpty());
    }

    @Test
    void cancelarPorPaciente_confirmada_rechaza() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(11, 0), 3, EstadoCita.ASISTENCIA_CONFIRMADA));

        assertEquals(ResultadoCancelacion.ESTADO_NO_PERMITE, service.cancelarPorPaciente(1, PACIENTE));

        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(cambios.cambios.isEmpty());
        assertTrue(notificaciones.publicadas.isEmpty());
        assertTrue(eventos.eventos.isEmpty());
    }

    @Test
    void automatica_aT30_sinConfirmar_cancela() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(10, 30), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, PACIENTE + 1, 7, HOY, LocalTime.of(10, 31), 4, EstadoCita.PROGRAMADA));

        assertEquals(1, service.cancelarAutomaticas());

        assertEquals(EstadoCita.CANCELADA, citas.porId(1).orElseThrow().estado());
        assertEquals(EstadoCita.PROGRAMADA, citas.porId(2).orElseThrow().estado());
        assertEquals(1, cambios.cambios.size());
        assertEquals(1, notificaciones.publicadas.size());
        assertEquals(1, eventos.eventos.size());
    }

    @Test
    void automatica_confirmada_noCancela() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(10, 20), 3, EstadoCita.ASISTENCIA_CONFIRMADA));

        assertEquals(0, service.cancelarAutomaticas());

        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(cambios.cambios.isEmpty());
        assertTrue(notificaciones.publicadas.isEmpty());
        assertTrue(eventos.eventos.isEmpty());
    }

    @Test
    void automatica_registraUsuarioSistema() {
        citas.store(cita(1, PACIENTE, 7, HOY, LocalTime.of(10, 20), 3, EstadoCita.PROGRAMADA));

        assertEquals(1, service.cancelarAutomaticas());

        assertEquals("Sistema", cambios.cambios.get(0).usuario());
        assertEquals("No confirmó asistencia", cambios.cambios.get(0).motivo());
    }
}