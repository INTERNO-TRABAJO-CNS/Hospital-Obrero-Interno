package com.Hospital.CNS.cns_backend.model.atencion;

import java.time.Instant;

public interface HistorialRepository {
    void agregarReceta(long citaId, String medicacion, Instant registradaEn);

    void quitarReceta(long citaId, String medicacion);
}