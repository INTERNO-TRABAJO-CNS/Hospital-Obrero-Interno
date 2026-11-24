package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.CatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.FakeConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeHorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;

/**
 * MockMvc sin levantar Spring ni la base de datos: el Service real corre con los Fake*Repository.
 * Verifica que el controller solo traduce: rol, estado HTTP y cuerpo JSON.
 */
class AdminCatalogoControllerTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();

    private static final String MEDICO_PEDIATRA =
            "{\"nombres\":\"Juan\",\"apellidos\":\"Perez\",\"especialidad\":\"PEDIATRIA\","
                    + "\"duracionCitaMin\":30,\"cupoMaximoDia\":6}";

    private MockMvc mvc;
    private FakeCitaRepository citas;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        CatalogoService service = new CatalogoService(new FakeMedicoRepository(), new FakeConsultorioRepository(),
                new FakeHorarioRepository(), new FakeDiaNoHabilRepository(), citas,
                new RelojFijo(LocalDateTime.of(2026, 11, 9, 8, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new AdminCatalogoController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    // ------------------------------------------------------------ [1a] rol

    @Test
    void controller_sinRolAdmin_403() throws Exception {
        mvc.perform(get("/api/v1/admin/medicos")
                        .header("Authorization", sesiones.bearer(1, "PACIENTE", 1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void controller_rolMedico_403_tambienEnEscritura() throws Exception {
        mvc.perform(post("/api/v1/admin/medicos").contentType(MediaType.APPLICATION_JSON).content(MEDICO_PEDIATRA)
                        .header("Authorization", sesiones.bearer(1, "MEDICO", 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void controller_sinToken_401() throws Exception {
        mvc.perform(get("/api/v1/admin/consultorios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void controller_cabecerasTemporalesDeAdmin_yaNoSirven_401() throws Exception {
        mvc.perform(get("/api/v1/admin/consultorios")
                        .header("X-Usuario-Id", "1").header("X-Rol", "ADMINISTRADOR").header("X-Persona-Id", "1"))
                .andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------------------- medicos

    @Test
    void listarMedicos_vacio_200() throws Exception {
        mvc.perform(admin(get("/api/v1/admin/medicos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listarMedicos_conEspecialidadInvalida_400() throws Exception {
        mvc.perform(admin(get("/api/v1/admin/medicos")).param("especialidad", "ONCOLOGIA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void crearMedico_201() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/medicos")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicoId").value(1))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crearMedico_cupo8_422() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/medicos")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA.replace("\"cupoMaximoDia\":6", "\"cupoMaximoDia\":8")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_CUPO"));
    }

    @Test
    void crearMedico_duracionFueraDeRango_422() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/medicos")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA.replace("\"duracionCitaMin\":30", "\"duracionCitaMin\":50")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_DURACION"));
    }

    @Test
    void modificarMedico_conCitasProgramadas_409() throws Exception {
        crearMedicoPediatra();
        citas.store(new CitaDTO(1, 1, "Paciente", "A1", 1, "Dr Perez", TipoEspecialidad.PEDIATRIA, 1,
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), 1, EstadoCita.PROGRAMADA));

        mvc.perform(admin(put("/api/v1/admin/medicos/1")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA.replace("\"duracionCitaMin\":30", "\"duracionCitaMin\":40")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CAMBIO_AFECTA_CITAS"));
    }

    @Test
    void modificarMedico_noExiste_404() throws Exception {
        mvc.perform(admin(put("/api/v1/admin/medicos/99")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADA"));
    }

    @Test
    void darDeBajaMedico_204_yQuedaInactivo() throws Exception {
        crearMedicoPediatra();

        mvc.perform(admin(delete("/api/v1/admin/medicos/1"))).andExpect(status().isNoContent());

        mvc.perform(admin(get("/api/v1/admin/medicos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    // ----------------------------------------------- consultorios, horarios, dias

    @Test
    void crearConsultorio_201_yListar() throws Exception {
        crearConsultorio("Consultorio 1");

        mvc.perform(admin(get("/api/v1/admin/consultorios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Consultorio 1"));
    }

    @Test
    void definirHorario_solapado_409() throws Exception {
        crearMedicoPediatra();
        crearConsultorio("Consultorio 1");
        String horario = "{\"medicoId\":1,\"consultorioId\":1,\"diaSemana\":\"MONDAY\","
                + "\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}";
        mvc.perform(admin(post("/api/v1/admin/horarios")).contentType(MediaType.APPLICATION_JSON).content(horario))
                .andExpect(status().isCreated());

        mvc.perform(admin(post("/api/v1/admin/horarios")).contentType(MediaType.APPLICATION_JSON)
                        .content(horario.replace("08:00", "11:00").replace("12:00", "14:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("HORARIO_SOLAPADO"));
    }

    @Test
    void registrarDiaNoHabil_201_yFechaPasada422() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/dias-no-habiles")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-11-20\",\"descripcion\":\"Feriado\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaNoHabilId").value(1));

        mvc.perform(admin(post("/api/v1/admin/dias-no-habiles")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-11-01\",\"descripcion\":\"Pasado\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_FECHA"));

        mvc.perform(admin(get("/api/v1/admin/dias-no-habiles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void consultorio_put200_delete204() throws Exception {
        crearConsultorio("Consultorio 1");

        mvc.perform(admin(put("/api/v1/admin/consultorios/1")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Consultorio 1A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Consultorio 1A"));
        mvc.perform(admin(delete("/api/v1/admin/consultorios/1"))).andExpect(status().isNoContent());

        mvc.perform(admin(get("/api/v1/admin/consultorios")))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void horario_put200_delete204() throws Exception {
        crearMedicoPediatra();
        crearConsultorio("Consultorio 1");
        mvc.perform(admin(post("/api/v1/admin/horarios")).contentType(MediaType.APPLICATION_JSON)
                        .content(HORARIO_LUNES))
                .andExpect(status().isCreated());

        mvc.perform(admin(put("/api/v1/admin/horarios/1")).contentType(MediaType.APPLICATION_JSON)
                        .content(HORARIO_LUNES.replace("12:00", "12:30")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaFin").value("12:30:00"));
        mvc.perform(admin(delete("/api/v1/admin/horarios/1"))).andExpect(status().isNoContent());
    }

    @Test
    void horario_delete_conCitasProgramadas_409() throws Exception {
        crearMedicoPediatra();
        crearConsultorio("Consultorio 1");
        mvc.perform(admin(post("/api/v1/admin/horarios")).contentType(MediaType.APPLICATION_JSON)
                        .content(HORARIO_LUNES))
                .andExpect(status().isCreated());
        citas.store(new CitaDTO(1, 1, "Paciente", "A1", 1, "Dr Perez", TipoEspecialidad.PEDIATRIA, 1,
                LocalDate.of(2026, 11, 16), LocalTime.of(9, 0), 1, EstadoCita.PROGRAMADA));

        mvc.perform(admin(delete("/api/v1/admin/horarios/1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CAMBIO_AFECTA_CITAS"));
    }

    @Test
    void diaNoHabil_put200_delete204() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/dias-no-habiles")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-12-25\",\"descripcion\":\"Navidad\"}"))
                .andExpect(status().isCreated());

        mvc.perform(admin(put("/api/v1/admin/dias-no-habiles/1")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-12-25\",\"descripcion\":\"Navidad (feriado)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Navidad (feriado)"));
        mvc.perform(admin(delete("/api/v1/admin/dias-no-habiles/1"))).andExpect(status().isNoContent());
    }

    // ----------------------------------------------------------------- ayudas

    private static final String HORARIO_LUNES = "{\"medicoId\":1,\"consultorioId\":1,\"diaSemana\":\"MONDAY\","
            + "\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}";

    private MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder peticion) {
        return peticion.header("Authorization", sesiones.bearer(1, "ADMINISTRADOR", 1));
    }

    private void crearMedicoPediatra() throws Exception {
        mvc.perform(admin(post("/api/v1/admin/medicos")).contentType(MediaType.APPLICATION_JSON)
                        .content(MEDICO_PEDIATRA))
                .andExpect(status().isCreated());
    }

    private void crearConsultorio(String nombre) throws Exception {
        mvc.perform(admin(post("/api/v1/admin/consultorios")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated());
    }
}
