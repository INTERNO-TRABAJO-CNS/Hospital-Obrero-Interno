package com.Hospital.CNS.cns_backend.model.identidad;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** SesionRepository en memoria. {@code sesiones} es publica para que los tests verifiquen. */
public class FakeSesionRepository implements SesionRepository {

    public final Map<String, SesionGuardada> sesiones = new LinkedHashMap<>();

    @Override
    public synchronized void guardar(SesionGuardada sesion) {
        sesiones.put(sesion.tokenHash(), sesion);
    }

    @Override
    public synchronized Optional<SesionGuardada> porTokenHash(String tokenHash) {
        return Optional.ofNullable(sesiones.get(tokenHash));
    }

    @Override
    public synchronized void cerrar(String tokenHash, LocalDateTime cuando) {
        SesionGuardada s = sesiones.get(tokenHash);
        if (s != null && !s.cerrada()) {
            sesiones.put(tokenHash, cerrada(s, cuando));
        }
    }

    @Override
    public synchronized void cerrarTodasDelUsuario(long usuarioId, LocalDateTime cuando) {
        sesiones.replaceAll((k, s) -> s.usuarioId() == usuarioId && !s.cerrada() ? cerrada(s, cuando) : s);
    }

    @Override
    public synchronized int eliminarVencidas(LocalDateTime antesDe) {
        int antes = sesiones.size();
        sesiones.values().removeIf(s -> s.venceEn().isBefore(antesDe));
        return antes - sesiones.size();
    }

    private static SesionGuardada cerrada(SesionGuardada s, LocalDateTime cuando) {
        return new SesionGuardada(s.tokenHash(), s.usuarioId(), s.creadaEn(), s.venceEn(), cuando);
    }
}
