package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabil;
import com.Hospital.CNS.cns_backend.model.citas.SolicitudService;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;

/**
 * CU-04 (RF-04, RN-05): el paciente elige especialidad y dia; el titular tambien pide para sus beneficiarios.
 * Hoy: miercoles 18/11/2026.
 */
class SolicitudControllerTest {

    private static final long TITULAR = 120;
    private static final long HIJO = 121;

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        SolicitudService solicitudes = new SolicitudService(new FakeSolicitudRepository(), new FakeCitaRepository(),
                new CalendarioHabil(new FakeDiaNoHabilRepository()),
                new RelojFijo(LocalDateTime.of(2026, 11, 18, 7, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new SolicitudController(solicitudes,
                        SesionesDePrueba.pacientes("AF-1", TITULAR, HIJO), sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private String titular() {
        return sesiones.bearer(5, "PACIENTE", TITULAR);
    }

    private org.springframework.test.web.servlet.ResultActions pedir(String cuerpo) throws Exception {
        return mvc.perform(post("/api/v1/solicitudes").header("Authorization", titular())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void solicitar_conEspecialidadYDia_quedaParaEseDia() throws Exception {
        pedir("{\"especialidad\":\"CONSULTORIO_FAMILIAR\",\"dia\":\"2026-11-23\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.diaSolicitado").value("2026-11-23"))
                .andExpect(jsonPath("$.pacienteId").value(TITULAR));
    }

    @Test
    void solicitar_sinDia_400() throws Exception {
        pedir("{\"especialidad\":\"CONSULTORIO_FAMILIAR\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void solicitar_paraUnBeneficiario() throws Exception {
        pedir("{\"especialidad\":\"PEDIATRIA\",\"dia\":\"2026-11-23\",\"pacienteId\":" + HIJO + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(HIJO));

        mvc.perform(get("/api/v1/solicitudes").header("Authorization", titular()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(HIJO));
    }

    @Test
    void solicitar_paraUnPacienteDeOtraAfiliacion_403() throws Exception {
        pedir("{\"especialidad\":\"PEDIATRIA\",\"dia\":\"2026-11-23\",\"pacienteId\":999}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));
    }

    @Test
    void diasDisponibles_soloHabilesDesdeHoy() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/dias-disponibles").header("Authorization", titular()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-11-18"))
                .andExpect(jsonPath("$[3]").value("2026-11-23"))
                .andExpect(jsonPath("$.length()").value(16));
    }

    @Test
    void solicitar_rolMedico_403() throws Exception {
        mvc.perform(post("/api/v1/solicitudes").header("Authorization", sesiones.bearer(6, "MEDICO", 7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"especialidad\":\"CONSULTORIO_FAMILIAR\",\"dia\":\"2026-11-23\"}"))
                .andExpect(status().isForbidden());
    }
}
