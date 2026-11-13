package com.Hospital.CNS.cns_backend.infrastructure.pg;

import java.sql.SQLException;

import org.slf4j.Logger;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * Traduce los errores de PostgreSQL que provoca el usuario (texto muy largo, dato repetido, referencia a algo
 * que no existe) a {@link DominioException}, para que la API responda 400/409 en vez de 500. Lo demas es un
 * error del servidor: se registra en el log y sale como IllegalStateException.
 */
final class ErroresPg {

    static final String UNICO = "23505";
    static final String REFERENCIA = "23503";
    static final String DEMASIADO_LARGO = "22001";
    static final String REGLA = "23514";
    static final String EXCLUSION = "23P01";

    private ErroresPg() {
    }

    /** Un UNIQUE roto sale con {@code codigoDuplicado} (409); el resto como {@link #traducir(Logger, String, SQLException)}. */
    static RuntimeException traducir(Logger log, String accion, SQLException e, String codigoDuplicado,
                                     String mensajeDuplicado) {
        if (UNICO.equals(e.getSQLState()) || EXCLUSION.equals(e.getSQLState())) {
            return new DominioException(codigoDuplicado, mensajeDuplicado);
        }
        return traducir(log, accion, e);
    }

    static RuntimeException traducir(Logger log, String accion, SQLException e) {
        String estado = e.getSQLState();
        if (DEMASIADO_LARGO.equals(estado)) {
            return new DominioException("DATOS_INVALIDOS", "Un texto supera el largo permitido");
        }
        if (REFERENCIA.equals(estado)) {
            return new DominioException("DATOS_INVALIDOS", "Hace referencia a un dato que no existe");
        }
        if (REGLA.equals(estado)) {
            return new DominioException("DATOS_INVALIDOS", "Un dato no cumple las reglas del sistema");
        }
        log.error("No se pudo {}: {}", accion, e.getMessage(), e);
        return new IllegalStateException("No se pudo " + accion, e);
    }
}
