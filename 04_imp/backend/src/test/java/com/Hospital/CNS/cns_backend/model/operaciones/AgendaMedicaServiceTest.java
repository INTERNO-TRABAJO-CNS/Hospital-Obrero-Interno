package com.Hospital.CNS.cns_backend.model.operaciones;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;

/** CU-13 con FakeCitaRepository y RelojFijo (sin BD). */
class AgendaMedicaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    private static final long MEDICO = 7;
    private static final long OTRO_MEDICO = 8;

    private FakeCitaRepository citas;
    private AgendaMedicaService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        service = new AgendaMedicaService(citas, new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(7, 30))));
    }

    @Test
    void agenda_hoy_soloDelMedico() {
        citas.store(cita(1, 100, MEDICO, HOY, LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 101, OTRO_MEDICO, HOY, LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));
        citas.store(cita(3, 102, MEDICO, HOY.plusDays(1), LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));

        AgendaDiaDTO agenda = service.agendaDelMedico(MEDICO, null);

        assertEquals(HOY, agenda.fecha());
        assertEquals(1, agenda.cantidad());
        assertEquals(1L, agenda.fichas().get(0).citaId());
    }

    @Test
    void agenda_sinCitas_cantidad0() { // [3a]
        AgendaDiaDTO agenda = service.agendaDelMedico(MEDICO, null);

        assertEquals(0, agenda.cantidad());
        assertTrue(agenda.fichas().isEmpty());
    }

    @Test
    void agenda_otraFecha() {
        LocalDate otra = HOY.plusDays(3);
        citas.store(cita(1, 100, MEDICO, otra, LocalTime.of(9, 0), 2, EstadoCita.PROGRAMADA));

        AgendaDiaDTO agenda = service.agendaDelMedico(MEDICO, otra);

        assertEquals(otra, agenda.fecha());
        assertEquals(1, agenda.cantidad());
        assertEquals(2, agenda.fichas().get(0).numeroFicha());
    }

    @Test
    void agenda_ordenadaPorHora_yCanceladasMarcadasYContadas() {
        citas.store(cita(1, 100, MEDICO, HOY, LocalTime.of(10, 0), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 101, MEDICO, HOY, LocalTime.of(8, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA));
        citas.store(cita(3, 102, MEDICO, HOY, LocalTime.of(9, 0), 2, EstadoCita.CANCELADA));

        AgendaDiaDTO agenda = service.agendaDelMedico(MEDICO, HOY);
        List<FichaAgendaDTO> fichas = agenda.fichas();

        assertEquals(List.of(2L, 3L, 1L), fichas.stream().map(FichaAgendaDTO::citaId).toList());
        assertEquals(EstadoCita.CANCELADA, fichas.get(1).estado());
        assertEquals(3, agenda.cantidad());
    }
}
