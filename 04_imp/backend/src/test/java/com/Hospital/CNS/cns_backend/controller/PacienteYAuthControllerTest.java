package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.identidad.AfiliadoDTO;
import com.Hospital.CNS.cns_backend.model.identidad.AuthService;
import com.Hospital.CNS.cns_backend.model.identidad.FakeAfiliadoRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeCifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.FakeUsuarioRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;

/** CU-01 y CU-03 por HTTP: registro del titular, login, beneficiario y consulta. */
class PacienteYAuthControllerTest {

    private static final String TITULAR =
            "{\"numeroAfiliado\":\"AF-10001\",\"ci\":\"4512301\",\"usuario\":\"juanc\",\"clave\":\"secreta1\"}";

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        FakeCifradorClave cifrador = new FakeCifradorClave();
        FakeAfiliadoRepository afiliados = new FakeAfiliadoRepository()
                .con(new AfiliadoDTO("AF-10001", "4512301", "Juan", "Choque", LocalDate.of(1985, 3, 14), true))
                .con(new AfiliadoDTO("AF-10002", "5123402", "Ana", "Quispe", LocalDate.of(1990, 7, 22), true));
        PacienteService pacientes = new PacienteService(new FakePacienteRepository(), afiliados,
                new FakeHistorialRepository(), usuarios, cifrador, new RelojFijo(LocalDate.of(2026, 11, 10).atStartOfDay()));
        mvc = MockMvcBuilders.standaloneSetup(
                        new PacienteController(pacientes, new ResolutorUsuario()),
                        new AuthController(new AuthService(usuarios, cifrador)))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void registrarTitularYLogin() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.titular").value(true));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"juanc\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andExpect(jsonPath("$.personaId").value(1));
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
    void login_credencialesInvalidas_401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"nadie\",\"clave\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void beneficiario_porElTitular_201_yNoDeOtraAfiliacion_403() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));

        mvc.perform(post("/api/v1/pacientes/beneficiario").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Usuario-Id", "1").header("X-Rol", "PACIENTE").header("X-Persona-Id", "1")
                        .content("{\"numeroAfiliadoTitular\":\"AF-10001\",\"ci\":\"9000001\",\"nombres\":\"Sofia\","
                                + "\"apellidos\":\"Choque\",\"fechaNacimiento\":\"2018-10-05\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titular").value(false));

        mvc.perform(post("/api/v1/pacientes/beneficiario").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Usuario-Id", "1").header("X-Rol", "PACIENTE").header("X-Persona-Id", "1")
                        .content("{\"numeroAfiliadoTitular\":\"AF-10002\",\"ci\":\"9000002\",\"nombres\":\"X\","
                                + "\"apellidos\":\"Y\",\"fechaNacimiento\":\"2018-10-05\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("NO_ES_DEL_PACIENTE"));
    }

    @Test
    void buscar_enfermeria_200_medico_403() throws Exception {
        mvc.perform(post("/api/v1/pacientes/titular").contentType(MediaType.APPLICATION_JSON).content(TITULAR));

        mvc.perform(get("/api/v1/pacientes").param("afiliado", "AF-10001")
                        .header("X-Usuario-Id", "9").header("X-Rol", "ENFERMERIA").header("X-Persona-Id", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/v1/pacientes").param("afiliado", "AF-10001")
                        .header("X-Usuario-Id", "9").header("X-Rol", "MEDICO").header("X-Persona-Id", "9"))
                .andExpect(status().isForbidden());
    }
}
