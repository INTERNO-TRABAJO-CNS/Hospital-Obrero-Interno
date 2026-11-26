package com.Hospital.CNS.cns_backend.model.citas;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;

/** CU-08: solo las citas propias; [2a] sin citas; [4a] cita ajena. */
class ConsultaCitasPacienteServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 12);
    private static final long PACIENTE = 100;

    private FakeCitaRepository citas;
    private ConsultaCitasPacienteService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        citas.store(cita(1, PACIENTE, 7, DIA.minusMonths(1), LocalTime.of(11, 0), 4, EstadoCita.CANCELADA));
        citas.store(cita(2, PACIENTE, 7, DIA, LocalTime.of(9, 30), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(3, 200, 7, DIA, LocalTime.of(10, 0), 5, EstadoCita.PROGRAMADA));
        citas.store(cita(4, PACIENTE, 8, DIA.minusDays(3), LocalTime.of(10, 15), 5, EstadoCita.ATENDIDA));
        service = new ConsultaCitasPacienteService(citas);
    }

    @Test
    void misCitas_soloLasPropias_masRecientePrimero() {
        List<CitaDTO> mias = service.misCitas(PACIENTE);

        assertEquals(List.of(2L, 4L, 1L), mias.stream().map(CitaDTO::citaId).toList());
    }

    @Test
    void misCitas_sinCitas_vacio() {
        assertTrue(service.misCitas(999).isEmpty());
    }

    @Test
    void detalle_propia_ok() {
        assertEquals(3, service.detalle(2, PACIENTE).numeroFicha());
    }

    @Test
    void detalle_citaAjena_rechaza() {
        DominioException e = assertThrows(DominioException.class, () -> service.detalle(3, PACIENTE));
        assertEquals("NO_ES_DEL_PACIENTE", e.getCodigo());
    }

    @Test
    void detalle_inexistente_noEncontrada() {
        DominioException e = assertThrows(DominioException.class, () -> service.detalle(77, PACIENTE));
        assertEquals("NO_ENCONTRADA", e.getCodigo());
    }
}
