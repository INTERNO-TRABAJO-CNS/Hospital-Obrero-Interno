package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

class ResolutorUsuarioTest {

    private final ResolutorUsuario resolutor = new ResolutorUsuario();

    @Test
    void leeElUsuarioDeLosHeaders() {
        Map<String, String> headers = Map.of("X-Usuario-Id", "7", "X-Rol", "medico", "X-Persona-Id", "42");

        UsuarioAutenticado usuario = resolutor.resolver(headers::get);

        assertEquals(new UsuarioAutenticado(7, RolUsuario.MEDICO, 42), usuario);
    }

    @Test
    void faltaUnHeader_error400() {
        DominioException e = assertThrows(DominioException.class,
                () -> resolutor.resolver("7", null, "42"));

        assertEquals("ENCABEZADO_FALTANTE", e.getCodigo());
    }

    @Test
    void idQueNoEsNumero_error400() {
        DominioException e = assertThrows(DominioException.class,
                () -> resolutor.resolver("abc", "PACIENTE", "42"));

        assertEquals("ENCABEZADO_INVALIDO", e.getCodigo());
    }

    @Test
    void rolDesconocido_error400() {
        DominioException e = assertThrows(DominioException.class,
                () -> resolutor.resolver("7", "DIRECTOR", "42"));

        assertEquals("ROL_INVALIDO", e.getCodigo());
    }
}
