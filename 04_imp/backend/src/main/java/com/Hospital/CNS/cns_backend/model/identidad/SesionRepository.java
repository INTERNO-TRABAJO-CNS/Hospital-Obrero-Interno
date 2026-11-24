package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDateTime;
import java.util.Optional;

/** Sesiones del login (tabla identidad.sesion, migracion V6). Implementacion: PgSesionRepository. */
public interface SesionRepository {
    void guardar(SesionGuardada sesion);

    Optional<SesionGuardada> porTokenHash(String tokenHash);

    /** Cierra una sesion (cerrar sesion). */
    void cerrar(String tokenHash, LocalDateTime cuando);

    /** Cierra todas las sesiones abiertas de un usuario (al darlo de baja). */
    void cerrarTodasDelUsuario(long usuarioId, LocalDateTime cuando);

    /** Borra las sesiones que vencieron antes de {@code antesDe}: ya no sirven para nada. */
    int eliminarVencidas(LocalDateTime antesDe);
}
