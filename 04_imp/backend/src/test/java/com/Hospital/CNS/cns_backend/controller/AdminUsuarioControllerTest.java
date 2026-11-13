package com.Hospital.CNS.cns_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.FakeCifradorClave;
import com.Hospital.CNS.cns_backend.model.identidad.FakeSesionRepository;
import com.Hospital.CNS.cns_backend.model.identidad.UsuarioService;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/** Cuentas del personal por HTTP: solo ADMINISTRADOR; la respuesta nunca trae la clave. */
class AdminUsuarioControllerTest {

    private static final String MEDICO =
            "{\"usuario\":\"dr.ramos\",\"clave\":\"clave123\",\"rol\":\"MEDICO\",\"personaId\":7}";

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FakeMedicoRepository medicos = new FakeMedicoRepository();
        medicos.store(new MedicoDTO(7, "Carlos", "Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 7, true));
        UsuarioService service = new UsuarioService(sesiones.usuarios(), new FakeSesionRepository(),
                new FakeCifradorClave(), medicos, new RelojFijo(LocalDateTime.of(2026, 11, 13, 9, 0)));
        mvc = MockMvcBuilders.standaloneSetup(new AdminUsuarioController(service, sesiones.resolutor()))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void administrador_creaCuentaDeMedico_201_sinClave() throws Exception {
        mvc.perform(post("/api/v1/admin/usuarios").contentType(MediaType.APPLICATION_JSON).content(MEDICO)
                        .header("Authorization", sesiones.bearer(1, "ADMINISTRADOR", 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Carlos Ramos"))
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.claveCifrada").doesNotExist());
    }

    @Test
    void medico_noPuedeCrearCuentas_403() throws Exception {
        mvc.perform(post("/api/v1/admin/usuarios").contentType(MediaType.APPLICATION_JSON).content(MEDICO)
                        .header("Authorization", sesiones.bearer(2, "MEDICO", 7)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void sinToken_401() throws Exception {
        mvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarYDarDeBaja() throws Exception {
        String admin = sesiones.bearer(1, "ADMINISTRADOR", 0);
        mvc.perform(post("/api/v1/admin/usuarios").contentType(MediaType.APPLICATION_JSON).content(MEDICO)
                .header("Authorization", admin));

        mvc.perform(get("/api/v1/admin/usuarios").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.usuario == 'dr.ramos')].activo").value(true));

        long id = sesiones.usuarios().porUsuario("dr.ramos").orElseThrow().usuarioId();
        mvc.perform(delete("/api/v1/admin/usuarios/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
    }
}
