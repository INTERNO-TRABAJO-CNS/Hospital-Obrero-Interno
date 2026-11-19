package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.FakeCancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;

class CancelacionControllerTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();

    private FakeCancelacionCita cancelacion;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        cancelacion = new FakeCancelacionCita();
        mvc = MockMvcBuilders.standaloneSetup(new CancelacionController(cancelacion, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void cancelar_pacienteAutenticado_usaSuPersonaId() throws Exception {
        mvc.perform(post("/api/v1/citas/91/cancelar")
                        .header("Authorization", sesiones.bearer(5, "PACIENTE", 120)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("CANCELADA"))
                .andExpect(jsonPath("$.mensaje").value("Cita cancelada"));

        assertEquals(1, cancelacion.llamadas.size());
        assertEquals(91, cancelacion.llamadas.get(0).citaId());
        assertEquals(120, cancelacion.llamadas.get(0).pacienteId());
    }

    @Test
    void cancelar_rolNoPaciente_403() throws Exception {
        mvc.perform(post("/api/v1/citas/91/cancelar")
                        .header("Authorization", sesiones.bearer(5, "MEDICO", 120)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void cancelar_resultadosDelService_seMapeanAErroresHttp() throws Exception {
        cancelacion.resultado = ResultadoCancelacion.NO_ENCONTRADA;
        mvc.perform(post("/api/v1/citas/91/cancelar")
                        .header("Authorization", sesiones.bearer(5, "PACIENTE", 120)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADA"));

        cancelacion.resultado = ResultadoCancelacion.NO_ES_DEL_PACIENTE;
        mvc.perform(post("/api/v1/citas/91/cancelar")
                        .header("Authorization", sesiones.bearer(5, "PACIENTE", 120)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));

        cancelacion.resultado = ResultadoCancelacion.ESTADO_NO_PERMITE;
        mvc.perform(post("/api/v1/citas/91/cancelar")
                        .header("Authorization", sesiones.bearer(5, "PACIENTE", 120)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTADO_NO_PERMITE"));
    }
}
