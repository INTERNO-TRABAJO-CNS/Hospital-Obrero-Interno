package com.Hospital.CNS.cns_backend.model.identidad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * CU-01: iniciar sesion, identificar a quien hace cada peticion y cerrar sesion (decision PV-11 del 13/11/2026:
 * token aleatorio guardado en la BD).
 *
 * <ul>
 *   <li>[2a] credenciales invalidas (usuario inexistente, inactivo o clave incorrecta) -> CREDENCIALES_INVALIDAS
 *       (401) con el mismo mensaje en todos los casos. Siempre se compara la clave, exista o no el usuario, para
 *       que el tiempo de respuesta no delate que usuarios existen.</li>
 *   <li>Tras varios fallos seguidos con el mismo usuario -> INTENTOS_EXCEDIDOS (429) por unos minutos.</li>
 *   <li>Token ausente, desconocido o cerrado -> NO_AUTENTICADO (401); vencido -> SESION_VENCIDA (401).</li>
 *   <li>[3a] las funciones segun el rol las aplica cada controller con el rol de la sesion.</li>
 * </ul>
 */
public class AuthService {

    private static final String PREFIJO_LOGIN = "login:";
    private static final int BYTES_TOKEN = 32;

    private final UsuarioRepository usuarios;
    private final CifradorClave cifrador;
    private final SesionRepository sesiones;
    private final PacienteRepository pacientes;
    private final LimitadorIntentos limitador;
    private final Reloj reloj;
    private final Duration duracion;
    private final String hashParaComparar;
    private final SecureRandom azar = new SecureRandom();

    /**
     * @param pacientes puede ser null; sirve para devolver el numero de afiliado al iniciar sesion un PACIENTE
     */
    public AuthService(UsuarioRepository usuarios, CifradorClave cifrador, SesionRepository sesiones,
                       PacienteRepository pacientes, LimitadorIntentos limitador, Reloj reloj, Duration duracion) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.sesiones = sesiones;
        this.pacientes = pacientes;
        this.limitador = limitador;
        this.reloj = reloj;
        this.duracion = duracion;
        this.hashParaComparar = cifrador.cifrar("clave-que-no-es-de-nadie");
    }

    public SesionDTO login(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw invalidas();
        }
        String nombreUsuario = usuario.trim();
        String claveLimite = PREFIJO_LOGIN + nombreUsuario.toLowerCase(Locale.ROOT);
        limitador.comprobar(claveLimite);

        Optional<UsuarioDTO> encontrado = usuarios.porUsuario(nombreUsuario);
        boolean coincide = cifrador.coincide(clave, encontrado.map(UsuarioDTO::claveCifrada).orElse(hashParaComparar));
        if (encontrado.isEmpty() || !coincide || !encontrado.get().activo()) {
            limitador.registrarFallo(claveLimite);
            throw invalidas();
        }
        limitador.limpiar(claveLimite);

        UsuarioDTO u = encontrado.get();
        LocalDateTime ahora = reloj.ahora();
        LocalDateTime vence = ahora.plus(duracion);
        String token = nuevoToken();
        sesiones.eliminarVencidas(ahora.minusDays(1));
        sesiones.guardar(new SesionGuardada(hashDe(token), u.usuarioId(), ahora, vence, null));
        return new SesionDTO(u.usuarioId(), u.rol(), u.personaId(), u.nombre(), token, vence, numeroAfiliadoDe(u));
    }

    /** Usuario dueno del token. Errores: NO_AUTENTICADO, SESION_VENCIDA (ambos 401). */
    public UsuarioSesion usuarioDelToken(String token) {
        if (token == null || token.isBlank()) {
            throw noAutenticado();
        }
        SesionGuardada sesion = sesiones.porTokenHash(hashDe(token.trim())).orElseThrow(AuthService::noAutenticado);
        if (sesion.cerrada()) {
            throw noAutenticado();
        }
        if (!reloj.ahora().isBefore(sesion.venceEn())) {
            throw new DominioException("SESION_VENCIDA", "Su sesion vencio. Vuelva a iniciar sesion");
        }
        UsuarioDTO u = usuarios.porId(sesion.usuarioId()).filter(UsuarioDTO::activo)
                .orElseThrow(AuthService::noAutenticado);
        return new UsuarioSesion(u.usuarioId(), u.rol(), u.personaId(), u.nombre());
    }

    /** Cierra la sesion del token. Un token desconocido no hace nada. */
    public void cerrarSesion(String token) {
        if (token != null && !token.isBlank()) {
            sesiones.cerrar(hashDe(token.trim()), reloj.ahora());
        }
    }

    /** SHA-256 en hexadecimal: lo unico que se guarda del token. */
    public static String hashDe(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("La JVM no tiene SHA-256", e);
        }
    }

    private String nuevoToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        azar.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String numeroAfiliadoDe(UsuarioDTO u) {
        if (u.rol() != RolUsuario.PACIENTE || pacientes == null) {
            return null;
        }
        return pacientes.porId(u.personaId()).map(PacienteDTO::numeroAfiliado).orElse(null);
    }

    private static DominioException invalidas() {
        return new DominioException("CREDENCIALES_INVALIDAS", "Usuario o contrasena incorrectos");
    }

    private static DominioException noAutenticado() {
        return new DominioException("NO_AUTENTICADO", "Inicie sesion para continuar");
    }
}
