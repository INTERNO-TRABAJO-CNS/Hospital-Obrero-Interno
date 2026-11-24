package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * CU-03: registrar y consultar pacientes.
 *
 * <ul>
 *   <li>[1b] la afiliacion no existe en la tabla interna -> AFILIADO_NO_ENCONTRADO (no se crea nada).</li>
 *   <li>[1a] el paciente ya esta registrado -> YA_REGISTRADO (nunca se duplica).</li>
 *   <li>[3a] datos incompletos o invalidos -> DATOS_INVALIDOS.</li>
 * </ul>
 *
 * <p>Solo el titular crea cuenta. Los beneficiarios no son afiliados: se registran con la afiliacion del titular
 * mas su CI y no tienen cuenta propia. En el primer registro se crea el historial clinico. La fecha de nacimiento
 * es obligatoria y no puede ser futura (decision del 10/11/2026). Usuario/contrasena creados por enfermeria:
 * {@code // PENDIENTE PV-18}.</p>
 *
 * <p>Todo se valida ANTES de guardar, y paciente + cuenta + historial se guardan en una sola {@link Transaccion}:
 * si algo falla no queda un paciente sin cuenta (que despues ya no podria registrarse). Tras varios intentos
 * fallidos con el mismo numero de afiliado (CI equivocado) el registro se bloquea unos minutos.</p>
 */
public class PacienteService {

    public static final int CLAVE_MINIMA = ReglasCuenta.CLAVE_MINIMA;
    public static final int CI_MAXIMO = 20;
    public static final int NOMBRE_MAXIMO = 100;

    private static final String PREFIJO_REGISTRO = "registro:";

    private final PacienteRepository pacientes;
    private final AfiliadoRepository afiliados;
    private final HistorialRepository historiales;
    private final UsuarioRepository usuarios;
    private final CifradorClave cifrador;
    private final Reloj reloj;
    private final Transaccion transaccion;
    private final LimitadorIntentos limitador;

    /** Sin transaccion ni limite de intentos (pruebas con Fakes). */
    public PacienteService(PacienteRepository p, AfiliadoRepository a, HistorialRepository h, UsuarioRepository u,
                           CifradorClave cifrador, Reloj reloj) {
        this(p, a, h, u, cifrador, reloj, Transaccion.DIRECTA, LimitadorIntentos.sinLimite());
    }

    public PacienteService(PacienteRepository p, AfiliadoRepository a, HistorialRepository h, UsuarioRepository u,
                           CifradorClave cifrador, Reloj reloj, Transaccion transaccion,
                           LimitadorIntentos limitador) {
        this.pacientes = p;
        this.afiliados = a;
        this.historiales = h;
        this.usuarios = u;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.transaccion = transaccion;
        this.limitador = limitador;
    }

    /** El titular crea su cuenta: valida su afiliacion y su CI contra la tabla interna. */
    public PacienteDTO registrarTitular(RegistroTitularDTO d) {
        if (d == null || !hayTexto(d.numeroAfiliado()) || !hayTexto(d.ci()) || !hayTexto(d.usuario())
                || d.clave() == null || d.clave().length() < CLAVE_MINIMA) {
            throw invalidos("Complete numero de afiliado, CI, usuario y una contrasena de al menos "
                    + CLAVE_MINIMA + " caracteres");
        }
        ReglasCuenta.validarUsuario(d.usuario());
        ReglasCuenta.validarClave(d.clave());
        String numero = d.numeroAfiliado().trim();
        String ci = d.ci().trim();
        String usuario = d.usuario().trim();
        String claveLimite = PREFIJO_REGISTRO + numero.toLowerCase(Locale.ROOT);
        limitador.comprobar(claveLimite);

        AfiliadoDTO afiliado = afiliados.titularPorNumero(numero).orElse(null);
        if (afiliado == null) {
            limitador.registrarFallo(claveLimite);
            throw new DominioException("AFILIADO_NO_ENCONTRADO", "El numero de afiliado no esta registrado en la CNS");
        }
        if (!afiliado.ci().equalsIgnoreCase(ci)) {
            limitador.registrarFallo(claveLimite);
            throw invalidos("El CI no corresponde al titular de esa afiliacion");
        }
        if (pacientes.porAfiliadoYCi(numero, ci).isPresent()) {
            throw yaRegistrado();
        }
        if (usuarios.porUsuario(usuario).isPresent()) {
            throw usuarioDuplicado();
        }
        validarNacimiento(afiliado.fechaNacimiento());
        String claveCifrada = cifrador.cifrar(d.clave());

        PacienteDTO registrado = transaccion.ejecutar(() -> {
            PacienteDTO nuevo = new PacienteDTO(pacientes.nextIdentity(), numero, ci, afiliado.nombres(),
                    afiliado.apellidos(), afiliado.fechaNacimiento(), true);
            if (!pacientes.store(nuevo)) {
                throw yaRegistrado();
            }
            UsuarioDTO cuenta = new UsuarioDTO(usuarios.nextIdentity(), usuario, claveCifrada, RolUsuario.PACIENTE,
                    nuevo.pacienteId(), nuevo.nombreCompleto(), true);
            if (!usuarios.store(cuenta)) {
                throw usuarioDuplicado();
            }
            historiales.crearSiNoExiste(nuevo.pacienteId());
            return nuevo;
        });
        limitador.limpiar(claveLimite);
        return registrado;
    }

    /** Registra un beneficiario con la afiliacion del titular y su propio CI. No crea cuenta. */
    public PacienteDTO registrarBeneficiario(String afiliadoTitular, String ci, PacienteDTO d) {
        if (!hayTexto(afiliadoTitular) || !hayTexto(ci) || d == null || !hayTexto(d.nombres())
                || !hayTexto(d.apellidos()) || d.fechaNacimiento() == null) {
            throw invalidos("Complete afiliacion del titular, CI, nombres, apellidos y fecha de nacimiento");
        }
        String numero = afiliadoTitular.trim();
        String ciBeneficiario = ci.trim();
        String nombres = d.nombres().trim();
        String apellidos = d.apellidos().trim();
        if (ciBeneficiario.length() > CI_MAXIMO) {
            throw invalidos("El CI no puede pasar de " + CI_MAXIMO + " caracteres");
        }
        if (nombres.length() > NOMBRE_MAXIMO || apellidos.length() > NOMBRE_MAXIMO) {
            throw invalidos("Nombres y apellidos no pueden pasar de " + NOMBRE_MAXIMO + " caracteres");
        }
        if (!afiliados.existePorNumero(numero)) {
            throw new DominioException("AFILIADO_NO_ENCONTRADO", "La afiliacion del titular no existe en la CNS");
        }
        if (pacientes.porAfiliadoYCi(numero, ciBeneficiario).isPresent()) {
            throw yaRegistrado();
        }
        validarNacimiento(d.fechaNacimiento());

        return transaccion.ejecutar(() -> {
            PacienteDTO nuevo = new PacienteDTO(pacientes.nextIdentity(), numero, ciBeneficiario, nombres, apellidos,
                    d.fechaNacimiento(), false);
            if (!pacientes.store(nuevo)) {
                throw yaRegistrado();
            }
            historiales.crearSiNoExiste(nuevo.pacienteId());
            return nuevo;
        });
    }

    /** Consulta por afiliacion (titular y beneficiarios) o por afiliacion + CI. */
    public List<PacienteDTO> buscar(String afiliado, String ci) {
        if (!hayTexto(afiliado)) {
            throw invalidos("Indique el numero de afiliado");
        }
        if (hayTexto(ci)) {
            return pacientes.porAfiliadoYCi(afiliado.trim(), ci.trim()).map(List::of).orElse(List.of());
        }
        return pacientes.deLaAfiliacion(afiliado.trim());
    }

    /** Un paciente solo registra beneficiarios o consulta pacientes de su propia afiliacion. */
    public void exigirMismaAfiliacion(long pacienteId, String afiliado) {
        String propia = pacientes.porId(pacienteId).map(PacienteDTO::numeroAfiliado).orElse(null);
        if (propia == null || afiliado == null || !propia.equals(afiliado.trim())) {
            throw new DominioException("NO_ES_DEL_PACIENTE", "Solo puede gestionar pacientes de su propia afiliacion");
        }
    }

    private void validarNacimiento(LocalDate fecha) {
        if (fecha == null || fecha.isAfter(reloj.ahora().toLocalDate())) {
            throw invalidos("La fecha de nacimiento es obligatoria y no puede ser futura");
        }
    }

    private static DominioException invalidos(String mensaje) {
        return new DominioException("DATOS_INVALIDOS", mensaje);
    }

    private static DominioException yaRegistrado() {
        return new DominioException("YA_REGISTRADO", "El paciente ya esta registrado");
    }

    private static DominioException usuarioDuplicado() {
        return new DominioException("USUARIO_DUPLICADO", "Ese nombre de usuario ya esta en uso");
    }

    private static boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }
}
