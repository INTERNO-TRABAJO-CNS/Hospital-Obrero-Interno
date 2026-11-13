package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/** Cuentas del personal (decision del 13/11/2026) con Fakes. */
class UsuarioServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 13, 9, 0);

    private FakeUsuarioRepository usuarios;
    private FakeSesionRepository sesiones;
    private FakeMedicoRepository medicos;
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        usuarios = new FakeUsuarioRepository();
        sesiones = new FakeSesionRepository();
        medicos = new FakeMedicoRepository();
        medicos.store(new MedicoDTO(7, "Carlos", "Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 7, true));
        medicos.store(new MedicoDTO(8, "Ana", "Salas", TipoEspecialidad.CONSULTORIO_FAMILIAR, 45, 7, false));
        service = new UsuarioService(usuarios, sesiones, new FakeCifradorClave(), medicos, new RelojFijo(AHORA));
    }

    @Test
    void crearCuentaMedico_tomaElNombreDelCatalogo_yCifraLaClave() {
        CuentaDTO c = service.crearCuentaPersonal(new NuevaCuentaDTO("dr.ramos", "clave123", RolUsuario.MEDICO, 7L, ""));

        assertEquals(RolUsuario.MEDICO, c.rol());
        assertEquals(7, c.personaId());
        assertEquals("Carlos Ramos", c.nombre());
        assertEquals("cifrada:clave123", usuarios.porUsuario("dr.ramos").orElseThrow().claveCifrada());
    }

    @Test
    void crearCuentaMedico_medicoInexistenteODeBaja_noEncontrada() {
        assertEquals("NO_ENCONTRADA", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("x", "clave123", RolUsuario.MEDICO, 99L, "X"))).getCodigo());
        assertEquals("NO_ENCONTRADA", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("x", "clave123", RolUsuario.MEDICO, 8L, "X"))).getCodigo());
    }

    @Test
    void crearCuentaEnfermeria_necesitaNombre() {
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("marta", "clave123", RolUsuario.ENFERMERIA, null, " "))).getCodigo());

        CuentaDTO c = service.crearCuentaPersonal(
                new NuevaCuentaDTO("marta", "clave123", RolUsuario.ENFERMERIA, null, "Marta Limachi"));
        assertEquals(0, c.personaId());
    }

    @Test
    void noCreaPacientesNiRepetidosNiClavesInvalidas() {
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("p", "clave123", RolUsuario.PACIENTE, 1L, "P"))).getCodigo());
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("p", "corta", RolUsuario.ENFERMERIA, null, "P"))).getCodigo());
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("p", "x".repeat(73), RolUsuario.ENFERMERIA, null, "P"))).getCodigo());

        service.crearCuentaPersonal(new NuevaCuentaDTO("marta", "clave123", RolUsuario.ENFERMERIA, null, "Marta"));
        assertEquals("USUARIO_DUPLICADO", assertThrows(DominioException.class, () -> service.crearCuentaPersonal(
                new NuevaCuentaDTO("MARTA", "clave123", RolUsuario.ADMINISTRADOR, null, "Otra"))).getCodigo());
    }

    @Test
    void listar_soloPersonal() {
        usuarios.store(new UsuarioDTO(50, "paciente", "x", RolUsuario.PACIENTE, 1, "Paciente", true));
        service.crearCuentaPersonal(new NuevaCuentaDTO("marta", "clave123", RolUsuario.ENFERMERIA, null, "Marta"));

        assertEquals(1, service.listarCuentasPersonal().size());
    }

    @Test
    void darDeBaja_inactivaYCierraSusSesiones_peroNoASiMismo() {
        CuentaDTO c = service.crearCuentaPersonal(
                new NuevaCuentaDTO("marta", "clave123", RolUsuario.ENFERMERIA, null, "Marta"));
        sesiones.guardar(new SesionGuardada("hash", c.usuarioId(), AHORA, AHORA.plusHours(8), null));

        assertEquals("NO_PERMITIDO",
                assertThrows(DominioException.class, () -> service.darDeBajaCuenta(c.usuarioId(), c.usuarioId()))
                        .getCodigo());
        service.darDeBajaCuenta(c.usuarioId(), 999);

        assertFalse(usuarios.porId(c.usuarioId()).orElseThrow().activo());
        assertTrue(sesiones.sesiones.get("hash").cerrada());
    }

    @Test
    void administradorInicial_soloSiNoHayNingunoYHayClave() {
        assertFalse(service.crearAdministradorInicial("admin", "", "Administrador"));
        assertTrue(service.crearAdministradorInicial("admin", "clave123", "Administrador"));
        assertFalse(service.crearAdministradorInicial("admin2", "clave123", "Otro"));

        assertEquals(RolUsuario.ADMINISTRADOR, usuarios.porUsuario("admin").orElseThrow().rol());
    }
}
