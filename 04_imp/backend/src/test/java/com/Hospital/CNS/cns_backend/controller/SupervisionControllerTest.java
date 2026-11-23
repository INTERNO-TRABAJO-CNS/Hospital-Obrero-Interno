package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.reportes.FakeReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.FichaSupervisionDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ResumenEspecialidadDTO;
import com.Hospital.CNS.cns_backend.model.reportes.SupervisionService;

/** CU-19: agenda de supervision con MockMvc (SupervisionService real con FakeReporteRepository, sin BD). */
class SupervisionControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 23);

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private final FakeReporteRepository repo = new FakeReporteRepository();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        SupervisionService service = new SupervisionService(repo, new RelojFijo(HOY.atTime(9, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new SupervisionController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
        repo.resumenes.put(HOY, List.of(new ResumenEspecialidadDTO(TipoEspecialidad.CONSULTORIO_FAMILIAR, 1,
                "Dr. Ramos", LocalTime.of(8, 0), LocalTime.of(11, 45), 7, 4, 1, 1, 2, 0)));
        repo.fichasDe(1, HOY, List.of(
                new FichaSupervisionDTO(30, 3, LocalTime.of(9, 30), EstadoCita.ASISTENCIA_CONFIRMADA, 4, "Ana Paz")));
    }

    private String admin() {
        return sesiones.bearer(1, "ADMINISTRADOR", 0);
    }

    @Test
    void resumen_administrador_200() throws Exception {
        mvc.perform(get("/api/v1/admin/supervision").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].especialidad").value("CONSULTORIO_FAMILIAR"))
                .andExpect(jsonPath("$[0].nombreMedico").value("Dr. Ramos"))
                .andExpect(jsonPath("$[0].fichas").value(4))
                .andExpect(jsonPath("$[0].cupoMaximo").value(7));
    }

    @Test
    void fichasDeMedico_administrador_200() throws Exception {
        mvc.perform(get("/api/v1/admin/supervision/medicos/1/fichas").param("fecha", "2026-11-23")
                        .header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroFicha").value(3))
                .andExpect(jsonPath("$[0].estado").value("ASISTENCIA_CONFIRMADA"));
    }

    /** [3a] Sin datos para la fecha: lista vacia. */
    @Test
    void resumen_sinDatos_listaVacia() throws Exception {
        mvc.perform(get("/api/v1/admin/supervision").param("fecha", "2026-11-24").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /** [1a] Solo el administrador. */
    @Test
    void resumen_medico_403() throws Exception {
        mvc.perform(get("/api/v1/admin/supervision").header("Authorization", sesiones.bearer(2, "MEDICO", 7)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void resumen_sinSesion_401() throws Exception {
        mvc.perform(get("/api/v1/admin/supervision"))
                .andExpect(status().isUnauthorized());
    }

    /** [3b] La agenda de supervision es de solo lectura. */
    @Test
    void intentoModificar_403() throws Exception {
        mvc.perform(put("/api/v1/admin/supervision/medicos/1/fichas").header("Authorization", admin()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SOLO_LECTURA"));
        mvc.perform(post("/api/v1/admin/supervision").header("Authorization", admin()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SOLO_LECTURA"));
        mvc.perform(delete("/api/v1/admin/supervision/citas/30").header("Authorization", admin()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SOLO_LECTURA"));
    }
}
