package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** Handshake de /ws/supervision: solo con un token valido de ADMINISTRADOR. */
class AutorizacionWebSocketTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private final AutorizacionWebSocket soloAdmin =
            new AutorizacionWebSocket(sesiones.resolutor(), RolUsuario.ADMINISTRADOR);

    private boolean conectar(String token, MockHttpServletResponse respuesta) {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/ws/supervision");
        if (token != null) {
            peticion.setQueryString("token=" + token);
        }
        return soloAdmin.beforeHandshake(new ServletServerHttpRequest(peticion),
                new ServletServerHttpResponse(respuesta), null, new HashMap<>());
    }

    private String token(String rol) {
        return sesiones.bearer(1, rol, 0).substring("Bearer ".length());
    }

    @Test
    void administrador_conecta() {
        assertTrue(conectar(token("ADMINISTRADOR"), new MockHttpServletResponse()));
    }

    @Test
    void otroRol_403() {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        assertFalse(conectar(token("MEDICO"), respuesta));
        assertEquals(403, respuesta.getStatus());
    }

    @Test
    void sinToken_401() {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        assertFalse(conectar(null, respuesta));
        assertEquals(401, respuesta.getStatus());
    }

    @Test
    void tokenInventado_401() {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        assertFalse(conectar("no-existe", respuesta));
        assertEquals(401, respuesta.getStatus());
    }

    @Test
    void pacienteAutenticado_exponeSuPersonaIdParaElCanalPrivado() {
        AutorizacionWebSocket soloPaciente =
                new AutorizacionWebSocket(sesiones.resolutor(), RolUsuario.PACIENTE);
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/ws/notificaciones");
        peticion.setQueryString("token=" + sesiones.bearer(1, "PACIENTE", 7).substring("Bearer ".length()));
        Map<String, Object> atributos = new HashMap<>();

        assertTrue(soloPaciente.beforeHandshake(
                new ServletServerHttpRequest(peticion),
                new ServletServerHttpResponse(new MockHttpServletResponse()),
                null,
                atributos));
        assertEquals(7L, atributos.get("personaId"));
    }
}
