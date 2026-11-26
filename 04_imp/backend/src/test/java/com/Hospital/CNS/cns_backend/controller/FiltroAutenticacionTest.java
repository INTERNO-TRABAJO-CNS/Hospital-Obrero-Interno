package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Que rutas pasan sin sesion. La TV (RN-16) no inicia sesion y debe poder leer su listado. */
class FiltroAutenticacionTest {

    private final FiltroAutenticacion filtro = new FiltroAutenticacion(null);

    @Test
    @DisplayName("RN-16: la TV lee GET /api/v1/tv/fichas sin token (no responde 401)")
    void tvSinSesion_pasa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tv/fichas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(request, response, cadena);

        assertEquals(200, response.getStatus());
        assertTrue(cadena.getRequest() != null, "la peticion llega al controlador");
    }

    @Test
    void loginYRegistroDelTitular_sonPublicos() {
        assertTrue(filtro.shouldNotFilter(new MockHttpServletRequest("POST", "/api/v1/auth/login")));
        assertTrue(filtro.shouldNotFilter(new MockHttpServletRequest("POST", "/api/v1/pacientes/titular")));
    }

    @Test
    @DisplayName("Las fichas del dia de enfermeria (con nombres de pacientes) siguen pidiendo sesion")
    void fichasDeEnfermeria_pidenSesion() {
        assertFalse(filtro.shouldNotFilter(new MockHttpServletRequest("GET", "/api/v1/fichas/hoy")));
        assertFalse(filtro.shouldNotFilter(new MockHttpServletRequest("POST", "/api/v1/tv/fichas")),
                "solo la lectura de la TV es publica");
    }
}
