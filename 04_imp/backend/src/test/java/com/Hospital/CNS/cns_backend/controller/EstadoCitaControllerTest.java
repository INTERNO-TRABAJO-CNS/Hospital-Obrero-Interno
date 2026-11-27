package com.Hospital.CNS.cns_backend.controller;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.operaciones.ActualizacionEstadoService;

class EstadoCitaControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private FakeCitaRepository citas;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        citas.store(cita(1, 10, 7, HOY, LocalTime.of(10, 0), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 11, 7, HOY, LocalTime.of(8, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA));
        ActualizacionEstadoService service = new ActualizacionEstadoService(citas, new FakeCambioCitaRepository(),
                new FakeAgendaEventos(), new RelojFijo(HOY.atTime(9, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new EstadoCitaController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void confirmar_enfermeria_ok() throws Exception {
        mvc.perform(post("/api/v1/citas/1/confirmar-asistencia")
                        .header("Authorization", sesiones.bearer(3, "ENFERMERIA", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("CONFIRMADA"));
    }

    @Test
    void confirmar_sinToken_401() throws Exception {
        mvc.perform(post("/api/v1/citas/1/confirmar-asistencia"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmar_rolEnElBodyNoSirve_403() throws Exception {
        // Antes el usuario llegaba en el body: ahora se ignora y manda el token.
        mvc.perform(post("/api/v1/citas/1/confirmar-asistencia")
                        .header("Authorization", sesiones.bearer(4, "PACIENTE", 10))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"rol\":\"ENFERMERIA\",\"personaId\":0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void confirmar_yaConfirmada_409() throws Exception {
        mvc.perform(post("/api/v1/citas/2/confirmar-asistencia")
                        .header("Authorization", sesiones.bearer(3, "ENFERMERIA", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTADO_NO_PERMITE"));
    }

    @Test
    void finalizar_medicoDueno_ok() throws Exception {
        mvc.perform(post("/api/v1/citas/2/finalizar")
                        .header("Authorization", sesiones.bearer(5, "MEDICO", 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("FINALIZADA"));
    }

    @Test
    void finalizar_otroMedico_403() throws Exception {
        mvc.perform(post("/api/v1/citas/2/finalizar")
                        .header("Authorization", sesiones.bearer(6, "MEDICO", 8)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DE_SU_AGENDA"));
    }
}
