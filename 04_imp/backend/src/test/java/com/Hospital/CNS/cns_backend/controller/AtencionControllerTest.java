package com.Hospital.CNS.cns_backend.controller;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.atencion.AtencionService;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeFinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;

class AtencionControllerTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeCitaRepository citas = new FakeCitaRepository();
        citas.store(cita(1, 100, 7, HOY, LocalTime.of(8, 0), 2, EstadoCita.ASISTENCIA_CONFIRMADA));
        citas.store(cita(2, 101, 7, HOY, LocalTime.of(9, 0), 3, EstadoCita.PROGRAMADA));
        AtencionService service = new AtencionService(citas, new FakeHistorialRepository(), new FakeFinalizacionCita(),
                new RelojFijo(HOY.atTime(8, 30)));
        mvc = MockMvcBuilders.standaloneSetup(new AtencionController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void datos_200() throws Exception {
        mvc.perform(medico(get("/api/v1/citas/1/atencion")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroAfiliado").value("AF-100"))
                .andExpect(jsonPath("$.consulta").value("CONSULTORIO_FAMILIAR"))
                .andExpect(jsonPath("$.puedeDerivar").value(true));
    }

    @Test
    void registrar_200() throws Exception {
        mvc.perform(medico(post("/api/v1/citas/1/atencion")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicacion\":\"Paracetamol\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("REGISTRADA"));
    }

    @Test
    void registrar_recetaVacia_400() throws Exception {
        mvc.perform(medico(post("/api/v1/citas/1/atencion")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicacion\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RECETA_INVALIDA"));
    }

    @Test
    void registrar_citaNoConfirmada_409() throws Exception {
        mvc.perform(medico(post("/api/v1/citas/2/atencion")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicacion\":\"Reposo\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTADO_NO_PERMITE"));
    }

    @Test
    void datos_citaInexistente_404() throws Exception {
        mvc.perform(medico(get("/api/v1/citas/99/atencion")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rolPaciente_403() throws Exception {
        mvc.perform(get("/api/v1/citas/1/atencion")
                        .header("Authorization", sesiones.bearer(1, "PACIENTE", 100)))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder medico(MockHttpServletRequestBuilder b) {
        return b.header("Authorization", sesiones.bearer(50, "MEDICO", 7));
    }
}
