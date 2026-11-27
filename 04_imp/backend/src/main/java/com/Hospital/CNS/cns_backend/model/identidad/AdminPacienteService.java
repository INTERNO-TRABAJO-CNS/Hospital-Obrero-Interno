package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * Gestionar &gt; Pacientes: el administrador lista, crea, modifica, da de baja y reactiva pacientes.
 *
 * <ul>
 *   <li>Titular: el administrador carga los datos del afiliado en la tabla interna (si la afiliacion ya existe, el
 *   CI debe coincidir) y, si da usuario y contrasena, le crea la cuenta para entrar a la app.</li>
 *   <li>Beneficiario: usa la afiliacion de un titular activo y nunca tiene cuenta propia (igual que CU-03).</li>
 *   <li>Baja logica: no se aplica si el paciente tiene citas pendientes -> CAMBIO_AFECTA_CITAS.</li>
 * </ul>
 */
public class AdminPacienteService {

    private final AdminPacienteRepository admin;
    private final PacienteRepository pacientes;
    private final PacienteService registro;
    private final HistorialRepository historiales;
    private final UsuarioRepository usuarios;
    private final CifradorClave cifrador;
    private final Reloj reloj;
    private final Transaccion transaccion;

    public AdminPacienteService(AdminPacienteRepository admin, PacienteRepository pacientes, PacienteService registro,
                                HistorialRepository historiales, UsuarioRepository usuarios, CifradorClave cifrador,
                                Reloj reloj, Transaccion transaccion) {
        this.admin = admin;
        this.pacientes = pacientes;
        this.registro = registro;
        this.historiales = historiales;
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.transaccion = transaccion;
    }

    public List<PacienteAdminDTO> listar() {
        return admin.listar();
    }

    public PacienteAdminDTO crear(NuevoPacienteAdminDTO d) {
        if (d == null || !hayTexto(d.numeroAfiliado()) || !hayTexto(d.ci())) {
            throw invalidos("Complete el numero de afiliado y el CI");
        }
        validarDatos(d.nombres(), d.apellidos(), d.fechaNacimiento());
        String numero = d.numeroAfiliado().trim();
        String ci = d.ci().trim();
        if (numero.length() > PacienteService.CI_MAXIMO || ci.length() > PacienteService.CI_MAXIMO) {
            throw invalidos("El numero de afiliado y el CI no pueden pasar de " + PacienteService.CI_MAXIMO
                    + " caracteres");
        }
        boolean conCuenta = hayTexto(d.usuario());

        if (!d.titular()) {
            if (conCuenta) {
                throw invalidos("Los beneficiarios no tienen cuenta propia: entran con la cuenta de su titular");
            }
            PacienteDTO nuevo = registro.registrarBeneficiario(numero, ci, new PacienteDTO(0, numero, ci,
                    d.nombres(), d.apellidos(), d.fechaNacimiento(), false));
            return existente(nuevo.pacienteId());
        }

        String usuario = conCuenta ? d.usuario().trim() : null;
        if (conCuenta) {
            ReglasCuenta.validarUsuario(usuario);
            ReglasCuenta.validarClave(d.clave());
            if (usuarios.porUsuario(usuario).isPresent()) {
                throw new DominioException("USUARIO_DUPLICADO", "Ese nombre de usuario ya esta en uso");
            }
        }
        AfiliadoDTO previo = admin.afiliadoPorNumero(numero).orElse(null);
        if (previo != null && !previo.ci().equalsIgnoreCase(ci)) {
            throw invalidos("Esa afiliacion ya existe con otro CI de titular");
        }
        if (pacientes.porAfiliadoYCi(numero, ci).isPresent()) {
            throw new DominioException("YA_REGISTRADO", "El paciente ya esta registrado");
        }
        String claveCifrada = conCuenta ? cifrador.cifrar(d.clave()) : null;
        String nombres = d.nombres().trim();
        String apellidos = d.apellidos().trim();

        long id = transaccion.ejecutar(() -> {
            if (!admin.guardarAfiliado(new AfiliadoDTO(numero, ci, nombres, apellidos, d.fechaNacimiento(), true))) {
                throw new DominioException("YA_REGISTRADO", "Ese CI ya pertenece a otro afiliado");
            }
            PacienteDTO nuevo = new PacienteDTO(pacientes.nextIdentity(), numero, ci, nombres, apellidos,
                    d.fechaNacimiento(), true);
            if (!pacientes.store(nuevo)) {
                throw new DominioException("YA_REGISTRADO", "Esa afiliacion ya tiene un titular registrado");
            }
            if (conCuenta) {
                UsuarioDTO cuenta = new UsuarioDTO(usuarios.nextIdentity(), usuario, claveCifrada,
                        RolUsuario.PACIENTE, nuevo.pacienteId(), nuevo.nombreCompleto(), true);
                if (!usuarios.store(cuenta)) {
                    throw new DominioException("USUARIO_DUPLICADO", "Ese nombre de usuario ya esta en uso");
                }
            }
            historiales.crearSiNoExiste(nuevo.pacienteId());
            return nuevo.pacienteId();
        });
        return existente(id);
    }

    /** Solo nombres, apellidos y fecha de nacimiento: la afiliacion y el CI no cambian. */
    public PacienteAdminDTO modificar(long id, NuevoPacienteAdminDTO d) {
        PacienteAdminDTO actual = existente(id);
        if (d == null) {
            throw invalidos("Complete nombres, apellidos y fecha de nacimiento");
        }
        validarDatos(d.nombres(), d.apellidos(), d.fechaNacimiento());
        String nombres = d.nombres().trim();
        String apellidos = d.apellidos().trim();
        transaccion.ejecutar(() -> {
            admin.actualizarDatos(id, nombres, apellidos, d.fechaNacimiento());
            if (actual.titular()) {
                admin.guardarAfiliado(new AfiliadoDTO(actual.numeroAfiliado(), actual.ci(), nombres, apellidos,
                        d.fechaNacimiento(), true));
            }
        });
        return existente(id);
    }

    public void darDeBaja(long id) {
        PacienteAdminDTO actual = existente(id);
        if (!actual.activo()) {
            return;
        }
        if (admin.contarCitasPendientes(id) > 0) {
            throw new DominioException("CAMBIO_AFECTA_CITAS",
                    "El paciente tiene citas programadas: no se puede dar de baja");
        }
        admin.cambiarActivo(id, false);
    }

    public PacienteAdminDTO reactivar(long id) {
        existente(id);
        admin.cambiarActivo(id, true);
        return existente(id);
    }

    private PacienteAdminDTO existente(long id) {
        return admin.porId(id).orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No se encontro el paciente"));
    }

    private void validarDatos(String nombres, String apellidos, LocalDate fecha) {
        if (!hayTexto(nombres) || !hayTexto(apellidos)) {
            throw invalidos("Complete nombres y apellidos");
        }
        if (nombres.trim().length() > PacienteService.NOMBRE_MAXIMO
                || apellidos.trim().length() > PacienteService.NOMBRE_MAXIMO) {
            throw invalidos("Nombres y apellidos no pueden pasar de " + PacienteService.NOMBRE_MAXIMO + " caracteres");
        }
        if (fecha == null || fecha.isAfter(reloj.ahora().toLocalDate())) {
            throw invalidos("La fecha de nacimiento es obligatoria y no puede ser futura");
        }
    }

    private static DominioException invalidos(String mensaje) {
        return new DominioException("DATOS_INVALIDOS", mensaje);
    }

    private static boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }
}
