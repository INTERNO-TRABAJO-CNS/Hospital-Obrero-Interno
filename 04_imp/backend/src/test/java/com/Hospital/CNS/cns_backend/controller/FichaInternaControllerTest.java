package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.AsignadorFichaInterna;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FakeFichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaService;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeHorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/** T100-20/21/22: estados HTTP y JSON de las fichas internas (lunes 16/11/2026, 09:00). */
class FichaInternaControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final String CUERPO = "{\"citaOrigenId\": 500, \"especialidad\": \"GASTROENTEROLOGIA\", "
            + "\"gravedad\": \"ALTA\", \"urgencia\": false, \"motivo\": \"Dolor abdominal\"}";

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        RelojFijo reloj = new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(9, 0)));
        FakeMedicoRepository medicos = new FakeMedicoRepository();
        medicos.store(new MedicoDTO(1, "Carlos", "Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 7, true));
        medicos.store(new MedicoDTO(3, "Jorge", "Mamani", TipoEspecialidad.ODONTOLOGIA, 50, 5, true));
        medicos.store(new MedicoDTO(10, "Raul", "Vargas", TipoEspecialidad.GASTROENTEROLOGIA, 25, 6, true));
        FakeHorarioRepository horarios = new FakeHorarioRepository();
        horarios.store(new HorarioDTO(1, 10, 12, DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(10, 0), true));
        FakeConsultorioRepository consultorios = new FakeConsultorioRepository();
        consultorios.store(new ConsultorioDTO(12, "Consultorio 12", true));
        FakePacienteRepository pacientes = new FakePacienteRepository();
        pacientes.store(new PacienteDTO(100, "AF-10006", "CI-1", "Elena", "Copa", LocalDate.of(1957, 2, 10), false));
        FakeCitaRepository citas = new FakeCitaRepository();
        citas.store(new CitaDTO(500, 100, "Elena Copa", "AF-10006", 1, "Carlos Ramos",
                TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, HOY, LocalTime.of(8, 0), 1, EstadoCita.ATENDIDA));
        FakeFichaInternaRepository fichas = new FakeFichaInternaRepository();
        AsignadorFichaInterna asignador = new AsignadorFichaInterna(fichas, medicos, horarios, consultorios, citas,
                new FakeDiaNoHabilRepository(), null, reloj);
        FichaInternaService service = new FichaInternaService(fichas, citas, medicos, pacientes, asignador,
                (objeto, id, accion, usuario, motivo) -> { }, new FakeNotificacionPublicador(), Transaccion.DIRECTA,
                reloj);
        mvc = MockMvcBuilders.standaloneSetup(new FichaInternaController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void generar_201_conTicketFechaYHora() throws Exception {
        mvc.perform(generar(1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticket").value(1001))
                .andExpect(jsonPath("$.estado").value("ASIGNADA"))
                .andExpect(jsonPath("$.fecha").value("2026-11-17"))
                .andExpect(jsonPath("$.hora").value("08:00"))
                .andExpect(jsonPath("$.medico").value("Raul Vargas"))
                .andExpect(jsonPath("$.consultorio").value("Consultorio 12"))
                .andExpect(jsonPath("$.edad").value(69));
    }

    @Test
    void generar_odontologo_403_y_segundaVez_409() throws Exception {
        mvc.perform(generar(3))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("MEDICO_NO_DERIVA"));
        mvc.perform(generar(1)).andExpect(status().isCreated());
        mvc.perform(generar(1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CITA_YA_DERIVADA"));
    }

    @Test
    void generar_motivoVacio_422() throws Exception {
        mvc.perform(post("/api/v1/medico/fichas-internas").header("Authorization", sesiones.bearer(1, "MEDICO", 1))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO.replace("Dolor abdominal", " ")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("MOTIVO_INVALIDO"));
    }

    @Test
    void paciente_noUsaFichasInternas_403() throws Exception {
        mvc.perform(post("/api/v1/medico/fichas-internas").header("Authorization", sesiones.bearer(1, "PACIENTE", 100))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());
    }

    @Test
    void especialista_veSuAgendaSinGravedad_y_porCitaSinDerivacion404() throws Exception {
        mvc.perform(generar(1)).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/especialista/fichas-internas").param("fecha", "2026-11-17")
                        .header("Authorization", sesiones.bearer(2, "MEDICO", 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticket").value(1001))
                .andExpect(jsonPath("$[0].gravedad").doesNotExist());
        mvc.perform(get("/api/v1/medico/fichas-internas/por-cita/999").header("Authorization",
                        sesiones.bearer(1, "MEDICO", 1)))
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.RequestBuilder generar(long medicoId) {
        return post("/api/v1/medico/fichas-internas").header("Authorization", sesiones.bearer(1, "MEDICO", medicoId))
                .contentType(MediaType.APPLICATION_JSON).content(CUERPO);
    }
}
