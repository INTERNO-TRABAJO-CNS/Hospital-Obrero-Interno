package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.operaciones.FichasDelDiaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FichasControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    private FakeCitaRepository citaRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        citaRepository = new FakeCitaRepository();
        RelojFijo reloj = new RelojFijo(HOY.atStartOfDay());
        FichasDelDiaService service = new FichasDelDiaService(citaRepository, reloj);

        // Si pide argumentos en el constructor de FichasController, pásalos aquí
        FichasController controller = new FichasController(service, null);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listadoParaTv_devuelveSoloCamposDeTv() throws Exception {
        citaRepository.citas.add(new CitaDTO(
                1L, 100L, "Juan Perez", "12345",
                200L, "Dr. Pérez", TipoEspecialidad.CARDIOLOGIA, 3,
                HOY, LocalTime.of(10, 30), 7, EstadoCita.PROGRAMADA
        ));

        mockMvc.perform(get("/api/v1/tv/fichas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroFicha").value(7))
                .andExpect(jsonPath("$[0].consultorio").value(3))
                .andExpect(jsonPath("$[0].nombrePaciente").doesNotExist());
    }
}