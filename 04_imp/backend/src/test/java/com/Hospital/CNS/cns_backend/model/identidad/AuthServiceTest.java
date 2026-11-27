package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** CU-01 con Fakes (sin BD): login con token, sesion, vencimiento, cierre y limite de intentos. */
class AuthServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 13, 9, 0);

    private FakeUsuarioRepository usuarios;
    private FakeSesionRepository sesiones;
    private RelojFijo reloj;
    private AuthService service;

    @BeforeEach
    void preparar() {
        usuarios = new FakeUsuarioRepository();
        sesiones = new FakeSesionRepository();
        reloj = new RelojFijo(AHORA);
        FakeCifradorClave cifrador = new FakeCifradorClave();
        FakePacienteRepository pacientes = new FakePacienteRepository();
        pacientes.store(new PacienteDTO(10, "AF-10001", "4512301", "Juan Carlos", "Choque", LocalDate.of(1985, 3, 14),
                true));
        usuarios.store(new UsuarioDTO(1, "juanc", cifrador.cifrar("secreta1"), RolUsuario.PACIENTE, 10,
                "Juan Carlos Choque", true));
        usuarios.store(new UsuarioDTO(2, "baja", cifrador.cifrar("secreta1"), RolUsuario.MEDICO, 7, "", false));
        LimitadorIntentos limitador = new LimitadorIntentos(3, Duration.ofMinutes(15), Duration.ofMinutes(15), reloj);
        service = new AuthService(usuarios, cifrador, sesiones, pacientes, limitador, reloj, Duration.ofHours(8));
    }

    @Test
    void login_ok_devuelveRolPersonaYToken() {
        SesionDTO s = service.login("JuanC", "secreta1");

        assertEquals(1, s.usuarioId());
        assertEquals(RolUsuario.PACIENTE, s.rol());
        assertEquals(10, s.personaId());
        assertEquals("Juan Carlos Choque", s.nombre());
        assertEquals("AF-10001", s.numeroAfiliado());
        assertEquals(AHORA.plusHours(8), s.venceEn());
        assertEquals(43, s.token().length());
    }

    @Test
    void login_soloGuardaElHashDelToken() {
        SesionDTO s = service.login("juanc", "secreta1");

        assertEquals(1, sesiones.sesiones.size());
        String guardado = sesiones.sesiones.keySet().iterator().next();
        assertNotEquals(s.token(), guardado);
        assertEquals(AuthService.hashDe(s.token()), guardado);
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

    @Test
    void login_tresFallos_bloqueaAunConLaClaveCorrecta() {
        for (int i = 0; i < 3; i++) {
            assertThrows(DominioException.class, () -> service.login("juanc", "mala"));
        }
        assertEquals("INTENTOS_EXCEDIDOS",
                assertThrows(DominioException.class, () -> service.login("juanc", "secreta1")).getCodigo());

        reloj.avanzar(Duration.ofMinutes(16));
        assertEquals(1, service.login("juanc", "secreta1").usuarioId());
    }

    @Test
    void usuarioDelToken_ok() {
        String token = service.login("juanc", "secreta1").token();

        UsuarioSesion u = service.usuarioDelToken(token);

        assertEquals(1, u.usuarioId());
        assertEquals(RolUsuario.PACIENTE, u.rol());
        assertEquals(10, u.personaId());
    }

    @Test
    void usuarioDelToken_vencido() {
        String token = service.login("juanc", "secreta1").token();
        reloj.avanzar(Duration.ofHours(8));

        assertEquals("SESION_VENCIDA",
                assertThrows(DominioException.class, () -> service.usuarioDelToken(token)).getCodigo());
    }

    @Test
    void usuarioDelToken_desconocidoOVacio() {
        assertEquals("NO_AUTENTICADO",
                assertThrows(DominioException.class, () -> service.usuarioDelToken("inventado")).getCodigo());
        assertEquals("NO_AUTENTICADO",
                assertThrows(DominioException.class, () -> service.usuarioDelToken(null)).getCodigo());
    }

    @Test
    void cerrarSesion_elTokenDejaDeServir() {
        String token = service.login("juanc", "secreta1").token();

        service.cerrarSesion(token);

        assertEquals("NO_AUTENTICADO",
                assertThrows(DominioException.class, () -> service.usuarioDelToken(token)).getCodigo());
    }

    @Test
    void cuentaDadaDeBaja_suTokenDejaDeServir() {
        String token = service.login("juanc", "secreta1").token();
        UsuarioDTO u = usuarios.porId(1).orElseThrow();
        usuarios.store(new UsuarioDTO(u.usuarioId(), u.usuario(), u.claveCifrada(), u.rol(), u.personaId(), u.nombre(),
                false));

        assertThrows(DominioException.class, () -> service.usuarioDelToken(token));
    }

    @Test
    void medico_noTraeNumeroAfiliado() {
        usuarios.store(new UsuarioDTO(3, "ramos", new FakeCifradorClave().cifrar("clave33"), RolUsuario.MEDICO, 7,
                "Dr. Ramos", true));

        SesionDTO s = service.login("ramos", "clave33");

        assertNull(s.numeroAfiliado());
        assertTrue(s.token().length() > 20);
        assertFalse(s.token().contains("="));
    }
}
