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
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.historial.HistorialService;

class HistorialControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);

    private MockMvc mvc;
    private FakeCitaRepository citas;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        HistorialService service = new HistorialService(new FakeHistorialRepository(), citas,
                new RelojFijo(HOY.atTime(12, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new HistorialController(service, new ResolutorUsuario()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void historial_pacienteDeSuAgenda_200() throws Exception {
        citas.store(cita(1, 100, 7, HOY, LocalTime.of(8, 0), 1, EstadoCita.ATENDIDA));

        mvc.perform(get("/api/v1/medico/pacientes/100/historial")
                        .header("X-Usuario-Id", "50").header("X-Rol", "MEDICO").header("X-Persona-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(100))
                .andExpect(jsonPath("$.citasPasadas.length()").value(1));
    }

    @Test
    void historial_pacienteAjeno_403() throws Exception {
        mvc.perform(get("/api/v1/medico/pacientes/100/historial")
                        .header("X-Usuario-Id", "50").header("X-Rol", "MEDICO").header("X-Persona-Id", "7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PACIENTE_FUERA_DE_AGENDA"));
    }

    @Test
    void historial_rolEnfermeria_403() throws Exception {
        mvc.perform(get("/api/v1/medico/pacientes/100/historial")
                        .header("X-Usuario-Id", "50").header("X-Rol", "ENFERMERIA").header("X-Persona-Id", "7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }
}
