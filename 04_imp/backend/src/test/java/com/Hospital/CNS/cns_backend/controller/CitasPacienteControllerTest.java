package com.Hospital.CNS.cns_backend.controller;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.citas.CalendarioHabil;
import com.Hospital.CNS.cns_backend.model.citas.ConsultaCitasPacienteService;
import com.Hospital.CNS.cns_backend.model.citas.ReprogramacionService;
import com.Hospital.CNS.cns_backend.model.citas.SolicitudService;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeAgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.FakeSolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.operaciones.CancelacionService;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;

class CitasPacienteControllerTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 7, 0);
    private static final LocalDate JUEVES = LocalDate.of(2026, 11, 19);
    private static final long PACIENTE = 120;
    private static final long BENEFICIARIO = 121;

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeCitaRepository citas = new FakeCitaRepository();
        citas.store(cita(1, PACIENTE, 7, JUEVES, LocalTime.of(9, 30), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 999, 7, JUEVES, LocalTime.of(10, 0), 4, EstadoCita.PROGRAMADA));
        citas.store(cita(3, PACIENTE, 7, JUEVES.minusDays(10), LocalTime.of(8, 0), 1, EstadoCita.ATENDIDA));
        citas.store(cita(4, BENEFICIARIO, 8, JUEVES, LocalTime.of(11, 0), 5, EstadoCita.PROGRAMADA));
        RelojFijo reloj = new RelojFijo(AHORA);
        CalendarioHabil calendario = new CalendarioHabil(new FakeDiaNoHabilRepository());
        SolicitudService solicitudes = new SolicitudService(new FakeSolicitudRepository(), citas, calendario, reloj);
        CancelacionService cancelacion = new CancelacionService(citas, new FakeCambioCitaRepository(),
                new FakeNotificacionPublicador(), new FakeAgendaEventos(), reloj);
        mvc = MockMvcBuilders.standaloneSetup(new CitasPacienteController(new ConsultaCitasPacienteService(citas),
                        new ReprogramacionService(citas, cancelacion, solicitudes, calendario, reloj),
                        SesionesDePrueba.pacientes("AF-1", PACIENTE, BENEFICIARIO), sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private String paciente() {
        return sesiones.bearer(5, "PACIENTE", PACIENTE);
    }

    @Test
    void misCitas_lasDelTitularYSusBeneficiarios_nuncaAjenas() throws Exception {
        mvc.perform(get("/api/v1/citas/mias").header("Authorization", paciente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].citaId").value(4))
                .andExpect(jsonPath("$[0].pacienteId").value(BENEFICIARIO))
                .andExpect(jsonPath("$[1].citaId").value(1))
                .andExpect(jsonPath("$[1].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$[2].citaId").value(3));
    }

    @Test
    void reprogramar_citaDelBeneficiario_conSuPacienteId() throws Exception {
        mvc.perform(post("/api/v1/citas/4/reprogramar").param("pacienteId", "" + BENEFICIARIO)
                        .header("Authorization", paciente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(BENEFICIARIO))
                .andExpect(jsonPath("$.prioridad").value(2));
    }

    @Test
    void pacienteIdDeOtraAfiliacion_403() throws Exception {
        mvc.perform(get("/api/v1/citas/2").param("pacienteId", "999").header("Authorization", paciente()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));
    }

    @Test
    void misCitas_rolMedico_403() throws Exception {
        mvc.perform(get("/api/v1/citas/mias").header("Authorization", sesiones.bearer(6, "MEDICO", 7)))
                .andExpect(status().isForbidden());
    }

    @Test
    void detalle_citaAjena_403() throws Exception {
        mvc.perform(get("/api/v1/citas/2").header("Authorization", paciente()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));
    }

    @Test
    void reprogramar_ok_devuelveSolicitudPrioridad2() throws Exception {
        mvc.perform(post("/api/v1/citas/1/reprogramar").header("Authorization", paciente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridad").value(2))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.diaSolicitado").value("2026-11-20"));

        mvc.perform(get("/api/v1/citas/1").header("Authorization", paciente()))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void reprogramar_citaAtendida_409() throws Exception {
        mvc.perform(post("/api/v1/citas/3/reprogramar").header("Authorization", paciente()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTADO_NO_PERMITE"));
    }
}
