package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/** CU-03 con Fakes (sin BD). */
class PacienteServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    private static final AfiliadoDTO JUAN =
            new AfiliadoDTO("AF-10001", "4512301", "Juan Carlos", "Choque Mamani", LocalDate.of(1985, 3, 14), true);

    private FakePacienteRepository pacientes;
    private FakeHistorialRepository historiales;
    private FakeUsuarioRepository usuarios;
    private PacienteService service;

    @BeforeEach
    void preparar() {
        pacientes = new FakePacienteRepository();
        historiales = new FakeHistorialRepository();
        usuarios = new FakeUsuarioRepository();
        FakeAfiliadoRepository afiliados = new FakeAfiliadoRepository().con(JUAN)
                .con(new AfiliadoDTO("AF-99999", "1", "Baja", "Inactivo", LocalDate.of(1980, 1, 1), false));
        service = new PacienteService(pacientes, afiliados, historiales, usuarios, new FakeCifradorClave(),
                new RelojFijo(HOY.atTime(9, 0)));
    }

    private static RegistroTitularDTO registroJuan() {
        return new RegistroTitularDTO("AF-10001", "4512301", "juanc", "secreta1");
    }

    private static PacienteDTO hijo(LocalDate nacimiento) {
        return new PacienteDTO(0, "AF-10001", "9000001", "Sofía", "Choque Ramos", nacimiento, false);
    }

    @Test
    void registrarTitular_ok_creaPacienteYCuentaCifrada() {
        PacienteDTO p = service.registrarTitular(registroJuan());

        assertTrue(p.titular());
        assertEquals("Juan Carlos", p.nombres(), "el nombre sale de la tabla interna de afiliados");
        assertEquals(LocalDate.of(1985, 3, 14), p.fechaNacimiento());
        UsuarioDTO cuenta = usuarios.porUsuario("juanc").orElseThrow();
        assertEquals(RolUsuario.PACIENTE, cuenta.rol());
        assertEquals(p.pacienteId(), cuenta.personaId());
        assertEquals("cifrada:secreta1", cuenta.claveCifrada());
    }

    @Test
    void registrarTitular_afiliadoInexistente() { // [1b]
        DominioException e = assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO("AF-00000", "4512301", "x", "secreta1")));

        assertEquals("AFILIADO_NO_ENCONTRADO", e.getCodigo());
        assertTrue(pacientes.pacientes.isEmpty());
        assertTrue(usuarios.usuarios.isEmpty());
    }

    @Test
    void registrarTitular_afiliadoInactivo_noEncontrado() { // [1b]
        DominioException e = assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO("AF-99999", "1", "baja", "secreta1")));

        assertEquals("AFILIADO_NO_ENCONTRADO", e.getCodigo());
    }

    @Test
    void registrarTitular_ciQueNoCorresponde_datosInvalidos() { // [3a]
        DominioException e = assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO("AF-10001", "0000000", "juanc", "secreta1")));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void registrarTitular_datosIncompletos() { // [3a]
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO("AF-10001", "4512301", "juanc", "123"))).getCodigo());
        assertEquals("DATOS_INVALIDOS", assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO(" ", "4512301", "juanc", "secreta1"))).getCodigo());
    }

    @Test
    void registrarTitular_yaRegistrado() { // [1a]
        service.registrarTitular(registroJuan());

        DominioException e = assertThrows(DominioException.class, () -> service.registrarTitular(
                new RegistroTitularDTO("AF-10001", "4512301", "otro", "secreta1")));

        assertEquals("YA_REGISTRADO", e.getCodigo());
        assertEquals(1, pacientes.pacientes.size());
    }

    @Test
    void registrarTitular_usuarioOcupado() {
        usuarios.store(new UsuarioDTO(50, "JUANC", "x", RolUsuario.ENFERMERIA, 1, "", true));

        DominioException e = assertThrows(DominioException.class, () -> service.registrarTitular(registroJuan()));

        assertEquals("USUARIO_DUPLICADO", e.getCodigo());
        assertTrue(pacientes.pacientes.isEmpty(), "no queda un paciente sin cuenta");
    }

    @Test
    void primerRegistro_creaHistorial() {
        PacienteDTO p = service.registrarTitular(registroJuan());

        assertTrue(historiales.historiales.contains(p.pacienteId()));
    }

    @Test
    void registrarBeneficiario_porAfiliacionYCI() {
        service.registrarTitular(registroJuan());

        PacienteDTO b = service.registrarBeneficiario("AF-10001", "9000001", hijo(LocalDate.of(2018, 10, 5)));

        assertFalse(b.titular());
        assertEquals("AF-10001", b.numeroAfiliado());
        assertEquals("9000001", b.ci());
        assertTrue(historiales.historiales.contains(b.pacienteId()));
        assertEquals(1, usuarios.usuarios.size(), "el beneficiario no tiene cuenta propia");
        assertEquals(List.of(true, false),
                service.buscar("AF-10001", null).stream().map(PacienteDTO::titular).toList());
    }

    @Test
    void registrarBeneficiario_duplicado() { // [1a]
        service.registrarBeneficiario("AF-10001", "9000001", hijo(LocalDate.of(2018, 10, 5)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarBeneficiario("AF-10001", "9000001", hijo(LocalDate.of(2018, 10, 5))));

        assertEquals("YA_REGISTRADO", e.getCodigo());
    }

    @Test
    void registrarBeneficiario_afiliacionInexistente() { // [1b]
        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarBeneficiario("AF-00000", "9000001", hijo(LocalDate.of(2018, 10, 5))));

        assertEquals("AFILIADO_NO_ENCONTRADO", e.getCodigo());
    }

    @Test
    void registro_fechaFutura_rechaza() { // decision del 10/11/2026
        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarBeneficiario("AF-10001", "9000001", hijo(HOY.plusDays(1))));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void registro_sinFechaNacimiento_rechaza() { // [3a]
        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarBeneficiario("AF-10001", "9000001", hijo(null)));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void buscar_porAfiliacionYCi() {
        service.registrarTitular(registroJuan());

        assertEquals(1, service.buscar("AF-10001", "4512301").size());
        assertTrue(service.buscar("AF-10001", "1111111").isEmpty());
        assertEquals("DATOS_INVALIDOS",
                assertThrows(DominioException.class, () -> service.buscar(null, null)).getCodigo());
    }

    @Test
    void exigirMismaAfiliacion_otraAfiliacion_rechaza() {
        PacienteDTO juan = service.registrarTitular(registroJuan());

        service.exigirMismaAfiliacion(juan.pacienteId(), "AF-10001");
        DominioException e = assertThrows(DominioException.class,
                () -> service.exigirMismaAfiliacion(juan.pacienteId(), "AF-10002"));
        assertEquals("NO_ES_DEL_PACIENTE", e.getCodigo());
    }
}
