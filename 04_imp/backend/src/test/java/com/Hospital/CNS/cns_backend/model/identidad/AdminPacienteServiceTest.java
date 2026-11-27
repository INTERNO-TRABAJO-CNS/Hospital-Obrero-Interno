package com.Hospital.CNS.cns_backend.model.identidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/** Gestionar > Pacientes del administrador, con Fakes (sin BD). */
class AdminPacienteServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 27);
    private static final AfiliadoDTO JUAN =
            new AfiliadoDTO("AF-10001", "4512301", "Juan Carlos", "Choque Mamani", LocalDate.of(1985, 3, 14), true);

    private FakeAfiliadoRepository afiliados;
    private FakeUsuarioRepository usuarios;
    private FakeAdminPacienteRepository admin;
    private AdminPacienteService service;

    @BeforeEach
    void preparar() {
        FakePacienteRepository pacientes = new FakePacienteRepository();
        afiliados = new FakeAfiliadoRepository().con(JUAN);
        usuarios = new FakeUsuarioRepository();
        admin = new FakeAdminPacienteRepository(pacientes, afiliados, usuarios);
        FakeHistorialRepository historiales = new FakeHistorialRepository();
        FakeCifradorClave cifrador = new FakeCifradorClave();
        RelojFijo reloj = new RelojFijo(HOY.atTime(9, 0));
        PacienteService registro = new PacienteService(pacientes, afiliados, historiales, usuarios, cifrador, reloj);
        service = new AdminPacienteService(admin, pacientes, registro, historiales, usuarios, cifrador, reloj,
                Transaccion.DIRECTA);
    }

    private static NuevoPacienteAdminDTO titular(String numero, String ci, String usuario, String clave) {
        return new NuevoPacienteAdminDTO(true, numero, ci, "Rosa", "Mamani Apaza", LocalDate.of(1995, 1, 30),
                usuario, clave);
    }

    private static NuevoPacienteAdminDTO beneficiario(String usuario) {
        return new NuevoPacienteAdminDTO(false, "AF-10001", "9000001", "Sofia", "Choque", LocalDate.of(2018, 10, 5),
                usuario, usuario == null ? null : "secreta1");
    }

    @Test
    void crearTitular_nuevaAfiliacion_conCuenta() {
        PacienteAdminDTO p = service.crear(titular("AF-20001", "7000001", "rosam", "secreta1"));

        assertTrue(p.titular());
        assertTrue(p.activo());
        assertEquals("rosam", p.usuario());
        assertTrue(afiliados.existePorNumero("AF-20001"), "se agrega a la tabla interna de afiliados");
        assertEquals(RolUsuario.PACIENTE, usuarios.porUsuario("rosam").orElseThrow().rol());
    }

    @Test
    void crearTitular_sinCuenta() {
        PacienteAdminDTO p = service.crear(titular("AF-20002", "7000002", " ", null));

        assertNull(p.usuario());
        assertTrue(usuarios.listar().isEmpty());
    }

    @Test
    void crearTitular_afiliacionExistenteConOtroCi() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.crear(titular("AF-10001", "999", null, null)));
        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void crearTitular_yaRegistrado() {
        service.crear(titular("AF-20003", "7000003", null, null));
        DominioException e = assertThrows(DominioException.class,
                () -> service.crear(titular("AF-20003", "7000003", null, null)));
        assertEquals("YA_REGISTRADO", e.getCodigo());
    }

    @Test
    void crearBeneficiario_sinCuentaPropia() {
        DominioException e = assertThrows(DominioException.class, () -> service.crear(beneficiario("sofi")));
        assertEquals("DATOS_INVALIDOS", e.getCodigo());

        PacienteAdminDTO b = service.crear(beneficiario(null));
        assertFalse(b.titular());
        assertEquals("AF-10001", b.numeroAfiliado());
    }

    @Test
    void fechaFutura_esInvalida() {
        DominioException e = assertThrows(DominioException.class, () -> service.crear(new NuevoPacienteAdminDTO(
                true, "AF-20004", "7000004", "Ana", "Paz", HOY.plusDays(1), null, null)));
        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void modificar_cambiaDatosYAfiliadoDelTitular() {
        PacienteAdminDTO p = service.crear(titular("AF-20005", "7000005", null, null));

        PacienteAdminDTO cambiado = service.modificar(p.pacienteId(), new NuevoPacienteAdminDTO(
                true, "OTRO", "OTRO", "Rosa Maria", "Mamani", LocalDate.of(1994, 2, 1), null, null));

        assertEquals("Rosa Maria", cambiado.nombres());
        assertEquals("AF-20005", cambiado.numeroAfiliado(), "la afiliacion no cambia");
        assertEquals("Rosa Maria", afiliados.afiliados.get("AF-20005").nombres());
    }

    @Test
    void darDeBaja_bloqueadaConCitasPendientes_yReactivar() {
        PacienteAdminDTO p = service.crear(titular("AF-20006", "7000006", null, null));
        admin.citasPendientes.put(p.pacienteId(), 1);

        DominioException e = assertThrows(DominioException.class, () -> service.darDeBaja(p.pacienteId()));
        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());

        admin.citasPendientes.clear();
        service.darDeBaja(p.pacienteId());
        assertFalse(admin.porId(p.pacienteId()).orElseThrow().activo());

        assertTrue(service.reactivar(p.pacienteId()).activo());
    }
}
