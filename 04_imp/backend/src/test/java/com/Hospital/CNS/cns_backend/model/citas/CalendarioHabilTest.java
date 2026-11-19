package com.Hospital.CNS.cns_backend.model.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;

/** Viernes 20/11/2026; lunes 23/11/2026. */
class CalendarioHabilTest {

    private static final LocalDate VIERNES = LocalDate.of(2026, 11, 20);
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);

    private final FakeDiaNoHabilRepository dias = new FakeDiaNoHabilRepository();
    private final CalendarioHabil calendario = new CalendarioHabil(dias);

    @Test
    void viernes_siguienteHabilEsLunes() {
        assertEquals(LUNES, calendario.siguienteDiaHabil(VIERNES));
    }

    @Test
    void diaNoHabilIntermedio_seSalta() {
        dias.store(new DiaNoHabilDTO(1, LUNES, "Feriado", true));

        assertEquals(LUNES.plusDays(1), calendario.siguienteDiaHabil(VIERNES));
        assertFalse(calendario.esHabil(LUNES));
    }

    @Test
    void diaNoHabilDadoDeBaja_vuelveASerHabil() {
        dias.store(new DiaNoHabilDTO(1, LUNES, "Feriado anulado", false));

        assertTrue(calendario.esHabil(LUNES));
    }

    @Test
    void finDeSemana_noEsHabil() {
        assertFalse(calendario.esHabil(VIERNES.plusDays(1)));
        assertFalse(calendario.esHabil(VIERNES.plusDays(2)));
        assertTrue(calendario.esHabil(VIERNES));
    }

    @Test
    void sumarDiasHabiles_saltaFinesDeSemanaYFeriados() {
        assertEquals(VIERNES, calendario.sumarDiasHabiles(VIERNES, 0));
        assertEquals(LUNES, calendario.sumarDiasHabiles(VIERNES, 1));
        assertEquals(LocalDate.of(2026, 12, 11), calendario.sumarDiasHabiles(VIERNES, 15));

        dias.store(new DiaNoHabilDTO(1, LocalDate.of(2026, 12, 8), "Feriado", true));
        assertEquals(LocalDate.of(2026, 12, 14), calendario.sumarDiasHabiles(VIERNES, 15));
    }

    @Test
    void sumarDiasNegativos_esInvalido() {
        assertThrows(IllegalArgumentException.class, () -> calendario.sumarDiasHabiles(VIERNES, -1));
    }
}
