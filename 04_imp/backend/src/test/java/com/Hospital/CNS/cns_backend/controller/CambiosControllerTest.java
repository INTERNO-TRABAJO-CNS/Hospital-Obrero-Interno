package com.Hospital.CNS.cns_backend.controller;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.historial.HistorialCambiosService;

class CambiosControllerTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 4);

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeCambioCitaRepository cambios = new FakeCambioCitaRepository();
        FakeCitaRepository citas = new FakeCitaRepository();
        citas.store(cita(1, 10, 7, DIA, LocalTime.of(9, 0), 3, EstadoCita.ASISTENCIA_CONFIRMADA));
        cambios.registrar(new CambioCitaDTO(1, 1, "ASISTENCIA_CONFIRMADA", "Enfermería · M. Flores",
                DIA.atTime(8, 25), "Paciente llegó al hospital"));
        mvc = MockMvcBuilders.standaloneSetup(
                        new CambiosController(new HistorialCambiosService(cambios, citas), sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void consultar_administrador_ok() throws Exception {
        mvc.perform(get("/api/v1/admin/cambios").param("cita", "1")
                        .header("Authorization", sesiones.bearer(1, "ADMINISTRADOR", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroFicha").value(3))
                .andExpect(jsonPath("$[0].usuario").value("Enfermería · M. Flores"))
                .andExpect(jsonPath("$[0].fechaHora").value("2026-11-04T08:25:00"));
    }

    @Test
    void consultar_enfermeriaConRango_ok() throws Exception {
        mvc.perform(get("/api/v1/admin/cambios").param("desde", "2026-11-04").param("hasta", "2026-11-04")
                        .header("Authorization", sesiones.bearer(2, "ENFERMERIA", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void consultar_rolPacienteOMedico_403() throws Exception {
        mvc.perform(get("/api/v1/admin/cambios").header("Authorization", sesiones.bearer(3, "PACIENTE", 10)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mvc.perform(get("/api/v1/admin/cambios").header("Authorization", sesiones.bearer(4, "MEDICO", 7)))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultar_rangoInvertido_400() throws Exception {
        mvc.perform(get("/api/v1/admin/cambios").param("desde", "2026-11-05").param("hasta", "2026-11-01")
                        .header("Authorization", sesiones.bearer(1, "ADMINISTRADOR", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void borrar_noExiste_405() throws Exception {
        mvc.perform(delete("/api/v1/admin/cambios").header("Authorization", sesiones.bearer(1, "ADMINISTRADOR", 0)))
                .andExpect(status().isMethodNotAllowed());
    }
}
