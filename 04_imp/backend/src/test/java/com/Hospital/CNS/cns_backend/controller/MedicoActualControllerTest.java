package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoActualService;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

class MedicoActualControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeMedicoRepository medicos = new FakeMedicoRepository();
        medicos.store(new MedicoDTO(7, "Rosa", "Mamani", TipoEspecialidad.PEDIATRIA, 30, 6, true));
        medicos.store(new MedicoDTO(8, "Luis", "Choque", TipoEspecialidad.ODONTOLOGIA, 45, 5, true));
        medicos.store(new MedicoDTO(9, "Eva", "Flores", TipoEspecialidad.DERMATOLOGIA, 20, 5, true));
        mvc = MockMvcBuilders.standaloneSetup(
                        new MedicoActualController(new MedicoActualService(medicos), new ResolutorUsuario()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void yo_pediatra_200_puedeDerivar() throws Exception {
        mvc.perform(get("/api/v1/medico/yo").header("X-Usuario-Id", "1").header("X-Rol", "MEDICO")
                        .header("X-Persona-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Rosa Mamani"))
                .andExpect(jsonPath("$.especialidad").value("PEDIATRIA"))
                .andExpect(jsonPath("$.puedeDerivar").value(true))
                .andExpect(jsonPath("$.operaEnQuirofano").value(false));
    }

    @Test
    void yo_odontologo_noDeriva() throws Exception {
        mvc.perform(get("/api/v1/medico/yo").header("X-Usuario-Id", "1").header("X-Rol", "MEDICO")
                        .header("X-Persona-Id", "8"))
                .andExpect(jsonPath("$.puedeDerivar").value(false));
    }

    @Test
    void yo_dermatologo_opera() throws Exception {
        mvc.perform(get("/api/v1/medico/yo").header("X-Usuario-Id", "1").header("X-Rol", "MEDICO")
                        .header("X-Persona-Id", "9"))
                .andExpect(jsonPath("$.operaEnQuirofano").value(true));
    }

    @Test
    void yo_paciente_403() throws Exception {
        mvc.perform(get("/api/v1/medico/yo").header("X-Usuario-Id", "1").header("X-Rol", "PACIENTE")
                        .header("X-Persona-Id", "7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void yo_medicoInexistente_404() throws Exception {
        mvc.perform(get("/api/v1/medico/yo").header("X-Usuario-Id", "1").header("X-Rol", "MEDICO")
                        .header("X-Persona-Id", "99"))
                .andExpect(status().isNotFound());
    }
}
