package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** CU-01 con Fakes (sin BD). */
class AuthServiceTest {

    private FakeUsuarioRepository usuarios;
    private AuthService service;

    @BeforeEach
    void preparar() {
        usuarios = new FakeUsuarioRepository();
        FakeCifradorClave cifrador = new FakeCifradorClave();
        usuarios.store(new UsuarioDTO(1, "juanc", cifrador.cifrar("secreta1"), RolUsuario.PACIENTE, 10,
                "Juan Carlos Choque", true));
        usuarios.store(new UsuarioDTO(2, "baja", cifrador.cifrar("secreta1"), RolUsuario.MEDICO, 7, "", false));
        service = new AuthService(usuarios, cifrador);
    }

    @Test
    void login_ok_devuelveRolYPersona() {
        SesionDTO s = service.login("JuanC", "secreta1");

        assertEquals(1, s.usuarioId());
        assertEquals(RolUsuario.PACIENTE, s.rol());
        assertEquals(10, s.personaId());
        assertEquals("Juan Carlos Choque", s.nombre());
    }

    @Test
    void login_credencialesInvalidas() { // [2a]
        assertEquals("CREDENCIALES_INVALIDAS",
                assertThrows(DominioException.class, () -> service.login("juanc", "mala")).getCodigo());
        assertEquals("CREDENCIALES_INVALIDAS",
                assertThrows(DominioException.class, () -> service.login("nadie", "secreta1")).getCodigo());
        assertEquals("CREDENCIALES_INVALIDAS",
                assertThrows(DominioException.class, () -> service.login("", "")).getCodigo());
    }

    @Test
    void login_usuarioInactivo_rechaza() { // [2a]
        assertEquals("CREDENCIALES_INVALIDAS",
                assertThrows(DominioException.class, () -> service.login("baja", "secreta1")).getCodigo());
    }
}
