package com.Hospital.CNS.cns_backend.model.identidad;

import java.nio.charset.StandardCharsets;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * Reglas comunes de las cuentas (paciente y personal). Los maximos son limites tecnicos: la columna
 * {@code usuario} es varchar(60) y BCrypt solo usa los primeros 72 bytes de la contrasena (con mas, falla).
 */
public final class ReglasCuenta {

    public static final int CLAVE_MINIMA = 6;
    public static final int CLAVE_MAXIMA_BYTES = 72;
    public static final int USUARIO_MAXIMO = 60;
    public static final int NOMBRE_MAXIMO = 200;

    private ReglasCuenta() {
    }

    public static void validarUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw invalidos("Indique el nombre de usuario");
        }
        if (usuario.trim().length() > USUARIO_MAXIMO) {
            throw invalidos("El nombre de usuario no puede pasar de " + USUARIO_MAXIMO + " caracteres");
        }
    }

    public static void validarClave(String clave) {
        if (clave == null || clave.length() < CLAVE_MINIMA) {
            throw invalidos("La contrasena debe tener al menos " + CLAVE_MINIMA + " caracteres");
        }
        if (clave.getBytes(StandardCharsets.UTF_8).length > CLAVE_MAXIMA_BYTES) {
            throw invalidos("La contrasena es demasiado larga (maximo " + CLAVE_MAXIMA_BYTES
                    + " caracteres simples; las letras con tilde ocupan el doble)");
        }
    }

    public static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw invalidos("Indique el nombre de la persona");
        }
        if (nombre.trim().length() > NOMBRE_MAXIMO) {
            throw invalidos("El nombre no puede pasar de " + NOMBRE_MAXIMO + " caracteres");
        }
    }

    static DominioException invalidos(String mensaje) {
        return new DominioException("DATOS_INVALIDOS", mensaje);
    }
}
