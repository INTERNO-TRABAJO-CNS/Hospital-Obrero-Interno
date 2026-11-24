package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Cuentas del personal (decision del 13/11/2026, adelanta S3-09): el administrador crea las cuentas de MEDICO,
 * ENFERMERIA y ADMINISTRADOR. Los pacientes crean la suya en el registro del titular (CU-03).
 *
 * <ul>
 *   <li>Rol PACIENTE o sin rol -> DATOS_INVALIDOS.</li>
 *   <li>MEDICO: {@code personaId} debe ser un medico activo del catalogo -> si no, NO_ENCONTRADA.</li>
 *   <li>Usuario repetido -> USUARIO_DUPLICADO (409).</li>
 *   <li>Baja logica: la cuenta queda inactiva y se cierran sus sesiones. Nadie se da de baja a si mismo.</li>
 * </ul>
 */
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final CifradorClave cifrador;
    private final MedicoRepository medicos;
    private final Reloj reloj;

    public UsuarioService(UsuarioRepository usuarios, SesionRepository sesiones, CifradorClave cifrador,
                          MedicoRepository medicos, Reloj reloj) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.cifrador = cifrador;
        this.medicos = medicos;
        this.reloj = reloj;
    }

    public CuentaDTO crearCuentaPersonal(NuevaCuentaDTO d) {
        if (d == null || d.rol() == null) {
            throw ReglasCuenta.invalidos("Indique el rol de la cuenta");
        }
        if (d.rol() == RolUsuario.PACIENTE) {
            throw ReglasCuenta.invalidos("Los pacientes crean su cuenta desde la aplicacion");
        }
        ReglasCuenta.validarUsuario(d.usuario());
        ReglasCuenta.validarClave(d.clave());

        long personaId = 0;
        String nombre = d.nombre() == null ? "" : d.nombre().trim();
        if (d.rol() == RolUsuario.MEDICO) {
            MedicoDTO medico = medicos.porId(d.personaId() == null ? 0 : d.personaId()).filter(MedicoDTO::activo)
                    .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "Medico no encontrado o dado de baja"));
            personaId = medico.medicoId();
            if (nombre.isEmpty()) {
                nombre = (medico.nombres() + " " + medico.apellidos()).trim();
            }
        }
        ReglasCuenta.validarNombre(nombre);

        String usuario = d.usuario().trim();
        if (usuarios.porUsuario(usuario).isPresent()) {
            throw duplicado();
        }
        UsuarioDTO nueva = new UsuarioDTO(usuarios.nextIdentity(), usuario, cifrador.cifrar(d.clave()), d.rol(),
                personaId, nombre, true);
        if (!usuarios.store(nueva)) {
            throw duplicado();
        }
        return CuentaDTO.desde(nueva);
    }

    /** Cuentas del personal (sin pacientes), sin contrasenas. */
    public List<CuentaDTO> listarCuentasPersonal() {
        return usuarios.listar().stream()
                .filter(u -> u.rol() != RolUsuario.PACIENTE)
                .map(CuentaDTO::desde)
                .toList();
    }

    public void darDeBajaCuenta(long usuarioId, long quienLaPide) {
        UsuarioDTO u = usuarios.porId(usuarioId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "Cuenta no encontrada"));
        if (usuarioId == quienLaPide) {
            throw new DominioException("NO_PERMITIDO", "No puede dar de baja su propia cuenta");
        }
        if (u.activo()) {
            usuarios.store(new UsuarioDTO(u.usuarioId(), u.usuario(), u.claveCifrada(), u.rol(), u.personaId(),
                    u.nombre(), false));
        }
        sesiones.cerrarTodasDelUsuario(usuarioId, reloj.ahora());
    }

    /**
     * Crea el primer administrador si todavia no hay ninguno activo y {@code clave} tiene valor.
     *
     * @return true si lo creo
     */
    public boolean crearAdministradorInicial(String usuario, String clave, String nombre) {
        if (clave == null || clave.isBlank() || usuarios.existeActivoConRol(RolUsuario.ADMINISTRADOR)) {
            return false;
        }
        crearCuentaPersonal(new NuevaCuentaDTO(usuario, clave, RolUsuario.ADMINISTRADOR, null, nombre));
        return true;
    }

    private static DominioException duplicado() {
        return new DominioException("USUARIO_DUPLICADO", "Ese nombre de usuario ya esta en uso");
    }
}
