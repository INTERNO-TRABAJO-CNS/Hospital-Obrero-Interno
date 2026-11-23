package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.reportes.FakeReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.IndicadoresDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteEstadoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteService;

/** CU-19/CU-20: reportes e indicadores con MockMvc (ReporteService real con FakeReporteRepository, sin BD). */
class ReporteControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 23);

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private final FakeReporteRepository repo = new FakeReporteRepository();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        ReporteService service = new ReporteService(repo, new RelojFijo(HOY.atTime(18, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new ReporteController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private String admin() {
        return sesiones.bearer(1, "ADMINISTRADOR", 0);
    }

    @Test
    void porEstado_200() throws Exception {
        repo.estados.add(new ReporteEstadoDTO(EstadoCita.CANCELADA, 9, 4));

        mvc.perform(get("/api/v1/admin/reportes/estado").param("desde", "2026-11-23").param("hasta", "2026-11-27")
                        .header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[3].estado").value("CANCELADA"))
                .andExpect(jsonPath("$[3].automaticas").value(4));
    }

    @Test
    void cumplimiento_200() throws Exception {
        repo.atendidas.add(new ReporteRepository.AtendidasDia(2, "Dr. Mamani", TipoEspecialidad.ODONTOLOGIA, HOY, 4));

        mvc.perform(get("/api/v1/admin/reportes/cumplimiento").param("desde", "2026-11-23")
                        .param("hasta", "2026-11-27").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreMedico").value("Dr. Mamani"))
                .andExpect(jsonPath("$[0].bajoMeta").value(true));
    }

    @Test
    void indicadores_sinDatos_valoresNulos() throws Exception {
        repo.indicadores = new IndicadoresDTO(86, 3, 9, 4, null, null, 0);

        mvc.perform(get("/api/v1/admin/reportes/indicadores").param("desde", "2026-11-23")
                        .param("hasta", "2026-11-27").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudes").value(86))
                .andExpect(jsonPath("$.fichasPorMedicoDia").doesNotExist());
    }

    @Test
    void periodoInvertido_400() throws Exception {
        mvc.perform(get("/api/v1/admin/reportes/estado").param("desde", "2026-11-27").param("hasta", "2026-11-23")
                        .header("Authorization", admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void sinPeriodo_400() throws Exception {
        mvc.perform(get("/api/v1/admin/reportes/estado").header("Authorization", admin()))
                .andExpect(status().isBadRequest());
    }

    /** CU-20 [1a] Solo el administrador. */
    @Test
    void reportes_enfermeria_403() throws Exception {
        mvc.perform(get("/api/v1/admin/reportes/indicadores").param("desde", "2026-11-23")
                        .param("hasta", "2026-11-27").header("Authorization", sesiones.bearer(3, "ENFERMERIA", 0)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void intentoModificar_403() throws Exception {
        mvc.perform(post("/api/v1/admin/reportes/estado").header("Authorization", admin()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SOLO_LECTURA"));
    }
}
