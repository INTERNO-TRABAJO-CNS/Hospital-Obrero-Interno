package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** El usuario sale del token (Authorization: Bearer); las cabeceras X-* ya no se aceptan. */
class ResolutorUsuarioTest {

    private final SesionesDePrueba sesiones = new SesionesDePrueba();
    private final ResolutorUsuario resolutor = sesiones.resolutor();

    @Test
    void tokenValido_devuelveSuUsuario() {
        String bearer = sesiones.bearer(50, "MEDICO", 7);

        UsuarioAutenticado u = resolutor.resolver(Map.of("Authorization", bearer)::get);

        assertEquals(RolUsuario.MEDICO, u.rol());
        assertEquals(7, u.personaId());
    }

    @Test
    void sinToken_noAutenticado() {
        DominioException e = assertThrows(DominioException.class, () -> resolutor.resolver(Map.<String, String>of()::get));
        assertEquals("NO_AUTENTICADO", e.getCodigo());
    }

    @Test
    void cabecerasTemporales_yaNoSirven() {
        Map<String, String> viejas = Map.of("X-Usuario-Id", "1", "X-Rol", "ADMINISTRADOR", "X-Persona-Id", "1");
        DominioException e = assertThrows(DominioException.class, () -> resolutor.resolver(viejas::get));
        assertEquals("NO_AUTENTICADO", e.getCodigo());
    }

    @Test
    void tokenInventado_noAutenticado() {
        DominioException e = assertThrows(DominioException.class,
                () -> resolutor.resolver(Map.of("Authorization", "Bearer inventado")::get));
        assertEquals("NO_AUTENTICADO", e.getCodigo());
    }

    @Test
    void token_soloConPrefijoBearer() {
        assertEquals("abc", ResolutorUsuario.token("Bearer abc"));
        assertEquals("abc", ResolutorUsuario.token("bearer  abc "));
        assertNull(ResolutorUsuario.token("abc"));
        assertNull(ResolutorUsuario.token("Bearer "));
        assertNull(ResolutorUsuario.token(null));
    }
}
