package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDateTime;

/**
 * Sesion abierta con el login. Solo se guarda el hash SHA-256 del token ({@code tokenHash}), nunca el token.
 * {@code cerradaEn} es null mientras la sesion sigue abierta.
 */
public record SesionGuardada(String tokenHash, long usuarioId, LocalDateTime creadaEn, LocalDateTime venceEn,
                             LocalDateTime cerradaEn) {

    public boolean cerrada() {
        return cerradaEn != null;
    }
}
