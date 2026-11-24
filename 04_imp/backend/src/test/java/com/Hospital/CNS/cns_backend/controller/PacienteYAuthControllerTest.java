package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoDTO;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.FakeAfiliadoRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeCifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeSesionRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeUsuarioRepository;
import com.Hospital.CNS.cns_backend.model.identidad.LimitadorIntentos;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioDTO;

/** CU-01 y CU-03 por HTTP: registro del titular, login con token, beneficiario, consulta y cierre de sesion. */
class PacienteYAuthControllerTest {

    private static final String TITULAR =
            "{\"numeroAfiliado\":\"AF-10001\",\"ci\":\"4512301\",\"usuario\":\"juanc\",\"clave\":\"secreta1\"}";
    private static final RelojFijo RELOJ = new RelojFijo(LocalDate.of(2026, 11, 10).atTime(9, 0));

    private MockMvc mvc;
    private FakeUsuarioRepository usuarios;
    private FakeCifradorClave cifrador;

    @BeforeEach
    void preparar() {
        usuarios = new FakeUsuarioRepository();
        cifrador = new FakeCifradorClave();
        FakePacienteRepository pacientesRepo = new FakePacienteRepository();
        FakeAfiliadoRepository afiliados = new FakeAfiliadoRepository()
                .con(new AfiliadoDTO("AF-10001", "4512301", "Juan", "Choque", LocalDate.of(1985, 3, 14), true))
                .con(new AfiliadoDTO("AF-10002", "5123402", "Ana", "Quispe", LocalDate.of(1990, 7, 22), true));
        PacienteService pacientes = new PacienteService(pacientesRepo, afiliados, new FakeHistorialRepository(), usuarios,
                cifrador, RELOJ);
        AuthService auth = new AuthService(usuarios, cifrador, new FakeSesionRepository(), pacientesRepo,
                LimitadorIntentos.sinLimite(), RELOJ, Duration.ofHours(8));
        mvc = MockMvcBuilders.standaloneSetup(
                        new PacienteController(pacientes, new ResolutorUsuario(auth)),
                        new AuthController(auth))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void registrarTitularYLogin_devuelveToken() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.titular").value(true));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"juanc\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andExpect(jsonPath("$.personaId").value(1))
                .andExpect(jsonPath("$.numeroAfiliado").value("AF-10001"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registrarTitular_afiliadoInexistente_404() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON)
                        .content(TITULAR.replace("AF-10001", "AF-00000")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("AFILIADO_NO_ENCONTRADO"));
    }

    @Test
    void registrarTitular_dosVeces_409() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON)
                        .content(TITULAR.replace("juanc", "otro")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("YA_REGISTRADO"));
    }

    @Test
    void registrarTitular_claveMuyLarga_400_yNoQuedaNada() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON)
                        .content(TITULAR.replace("secreta1", "x".repeat(73))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));

        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR))
                .andExpect(status().isCreated());
    }

    @Test
    void login_credencialesInvalidas_401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"nadie\",\"clave\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void beneficiario_porElTitular_201_yNoDeOtraAfiliacion_403() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));
        String bearer = bearer("juanc", "secreta1");

        mvc.perform(post("/api/v1/pacientes/beneficiario").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer)
                        .content("{\"numeroAfiliadoTitular\":\"AF-10001\",\"ci\":\"9000001\",\"nombres\":\"Sofia\","
                                + "\"apellidos\":\"Choque\",\"fechaNacimiento\":\"2018-10-05\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titular").value(false));

        mvc.perform(post("/api/v1/pacientes/beneficiario").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer)
                        .content("{\"numeroAfiliadoTitular\":\"AF-10002\",\"ci\":\"9000002\",\"nombres\":\"X\","
                                + "\"apellidos\":\"Y\",\"fechaNacimiento\":\"2018-10-05\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));
    }

    @Test
    void beneficiario_sinToken_401() throws Exception {
        mvc.perform(post("/api/v1/pacientes/beneficiario").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroAfiliadoTitular\":\"AF-10001\",\"ci\":\"9000001\",\"nombres\":\"Sofia\","
                                + "\"apellidos\":\"Choque\",\"fechaNacimiento\":\"2018-10-05\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void buscar_enfermeria_200_medico_403() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));
        usuarios.store(new UsuarioDTO(90, "enfermera", cifrador.cifrar("clave90"), RolUsuario.ENFERMERIA, 0,
                "Marta", true));
        usuarios.store(new UsuarioDTO(91, "medico", cifrador.cifrar("clave91"), RolUsuario.MEDICO, 7, "Dr. Ramos",
                true));

        mvc.perform(get("/api/v1/pacientes").param("afiliado", "AF-10001")
                        .header("Authorization", bearer("enfermera", "clave90")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/v1/pacientes").param("afiliado", "AF-10001")
                        .header("Authorization", bearer("medico", "clave91")))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_elTokenDejaDeServir() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));
        String bearer = bearer("juanc", "secreta1");

        mvc.perform(post("/api/v1/auth/logout").header("Authorization", bearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/pacientes").param("afiliado", "AF-10001").header("Authorization", bearer))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    /** Hace login por HTTP y devuelve el valor del header Authorization. */
    private String bearer(String usuario, String clave) throws Exception {
        String json = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int inicio = json.indexOf("\"token\":\"") + 9;
        String token = json.substring(inicio, json.indexOf('"', inicio));
        assertEquals(43, token.length());
        return "Bearer " + token;
    }
}
