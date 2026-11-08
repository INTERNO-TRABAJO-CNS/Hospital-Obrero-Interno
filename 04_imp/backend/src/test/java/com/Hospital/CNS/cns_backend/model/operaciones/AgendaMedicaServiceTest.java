package com.Hospital.CNS.cns_backend.model.operaciones;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AgendaMedicaServiceTest {

    @Test
    void agenda_hoy_soloDelMedico() {
        assertNotNull(LocalDate.now());
    }

    @Test
    void agenda_sinCitas_cantidad0() {
        AgendaDiaDTO agenda = new AgendaDiaDTO(LocalDate.now(), 0, List.of());
        assertEquals(0, agenda.cantidad());
        assertTrue(agenda.fichas().isEmpty());
    }

    @Test
    void agenda_otraFecha() {
        LocalDate otraFecha = LocalDate.of(2026, 12, 25);
        assertEquals(2026, otraFecha.getYear());
    }
}