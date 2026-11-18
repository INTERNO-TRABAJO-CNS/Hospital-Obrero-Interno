package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.cirugias.FakeCirugiaRepository;
import com.Hospital.CNS.cns_backend.model.cirugias.ProgramacionCirugiaService;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FakeFichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeQuirofanoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoCatalogoService;

/** T100-19/23: estados HTTP de quirofanos y cirugias (lunes 16/11/2026, 09:00). */
class CirugiaControllerTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final String QUIROFANO = "{\"nombre\": \"G-1\", \"especialidad\": \"GASTROENTEROLOGIA\", "
            + "\"limpiezaMin\": 40}";
    private static final String SOLICITUD = "{\"fichaTicket\": 1019, \"duracionMin\": 90, \"gravedad\": \"ALTA\", "
            + "\"urgente\": false}";

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        RelojFijo reloj = new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(9, 0)));
        FakeMedicoRepository medicos = new FakeMedicoRepository();
        medicos.store(new MedicoDTO(10, "Raul", "Vargas", TipoEspecialidad.GASTROENTEROLOGIA, 25, 6, true));
        FakeFichaInternaRepository fichas = new FakeFichaInternaRepository();
        fichas.insertar(new FichaInternaDTO(1019, TipoEspecialidad.GASTROENTEROLOGIA, false, 110, "Pedro", "", null,
                null, 1, "", TipoEspecialidad.CONSULTORIO_FAMILIAR, 10L, "Raul Vargas", 12, "Consultorio 12", HOY,
                LocalTime.of(8, 0), 25, Gravedad.ALTA, false, false, 1019, "Motivo", null, EstadoFichaInterna.ATENDIDA,
                reloj.ahora()));
        FakeCirugiaRepository cirugias = new FakeCirugiaRepository();
        FakeQuirofanoRepository quirofanos = new FakeQuirofanoRepository();
        ProgramacionCirugiaService programacion = new ProgramacionCirugiaService(cirugias, fichas, quirofanos, medicos,
                new FakeCitaRepository(), new FakeDiaNoHabilRepository(), (o, id, a, u, m) -> { },
                new FakeNotificacionPublicador(), Transaccion.DIRECTA, reloj);
        QuirofanoCatalogoService catalogo = new QuirofanoCatalogoService(quirofanos,
                id -> cirugias.contarProgramadasDesde(id, reloj.ahora()));
        mvc = MockMvcBuilders.standaloneSetup(new CirugiaController(programacion, catalogo, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void quirofanos_soloAdministrador_y_limpiezaFueraDeRango422() throws Exception {
        mvc.perform(post("/api/v1/admin/quirofanos").header("Authorization", sesiones.bearer(2, "MEDICO", 10))
                        .contentType(MediaType.APPLICATION_JSON).content(QUIROFANO))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/quirofanos").header("Authorization", sesiones.bearer(3, "ADMINISTRADOR", 3))
                        .contentType(MediaType.APPLICATION_JSON).content(QUIROFANO.replace("40", "50")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("LIMPIEZA_FUERA_DE_RANGO"));
        mvc.perform(post("/api/v1/admin/quirofanos").header("Authorization", sesiones.bearer(3, "ADMINISTRADOR", 3))
                        .contentType(MediaType.APPLICATION_JSON).content(QUIROFANO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apertura").value("07:00:00"));
    }

    @Test
    void proponer_confirmar_201_y_otraVezLaMisma_409() throws Exception {
        crearQuirofano();
        mvc.perform(post("/api/v1/especialista/cirugias/propuesta").header("Authorization", vargas())
                        .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quirofano").value("G-1"))
                .andExpect(jsonPath("$.inicio").value("07:00"))
                .andExpect(jsonPath("$.limpiezaHasta").value("09:10"));
        String confirmacion = SOLICITUD.replace("}", ", \"quirofanoId\": 1, \"fecha\": \"2026-11-17\", "
                + "\"inicio\": \"07:00\"}");

        mvc.perform(post("/api/v1/especialista/cirugias").header("Authorization", vargas())
                        .contentType(MediaType.APPLICATION_JSON).content(confirmacion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(58));
        mvc.perform(post("/api/v1/especialista/cirugias").header("Authorization", vargas())
                        .contentType(MediaType.APPLICATION_JSON).content(confirmacion))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CHOQUE_DE_HORARIO"));
        mvc.perform(get("/api/v1/especialista/quirofanos").header("Authorization", vargas()))
                .andExpect(jsonPath("$[0]").value("G-1"));
    }

    private void crearQuirofano() throws Exception {
        mvc.perform(post("/api/v1/admin/quirofanos").header("Authorization", sesiones.bearer(3, "ADMINISTRADOR", 3))
                .contentType(MediaType.APPLICATION_JSON).content(QUIROFANO)).andExpect(status().isCreated());
    }

    private String vargas() {
        return sesiones.bearer(2, "MEDICO", 10);
    }
}
