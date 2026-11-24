package com.Hospital.CNS.cns_backend.controller;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.operaciones.AgendaMedicaService;

/** MockMvc sin Spring ni BD: el Service real corre con FakeCitaRepository. */
class AgendaMedicaControllerTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);

    private MockMvc mvc;
    private FakeCitaRepository citas;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        AgendaMedicaService service = new AgendaMedicaService(citas, new RelojFijo(HOY.atTime(7, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new AgendaMedicaController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void controller_rolPaciente_403() throws Exception { // [1a]
        mvc.perform(get("/api/v1/medico/agenda")
                        .header("Authorization", sesiones.bearer(1, "PACIENTE", 7)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void controller_medico_usaSuPersonaIdYHoy() throws Exception {
        citas.store(cita(1, 100, 7, HOY, LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 101, 9, HOY, LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));

        mvc.perform(get("/api/v1/medico/agenda")
                        .header("Authorization", sesiones.bearer(50, "MEDICO", 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-11-10"))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.fichas[0].citaId").value(1));
    }

    @Test
    void controller_otraFecha() throws Exception {
        citas.store(cita(1, 100, 7, HOY.plusDays(1), LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));

        mvc.perform(get("/api/v1/medico/agenda").param("fecha", "2026-11-11")
                        .header("Authorization", sesiones.bearer(50, "MEDICO", 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1));
    }

    @Test
    void controller_fechaInvalida_400() throws Exception {
        mvc.perform(get("/api/v1/medico/agenda").param("fecha", "10-11-2026")
                        .header("Authorization", sesiones.bearer(50, "MEDICO", 7)))
                .andExpect(status().isBadRequest());
    }
}
