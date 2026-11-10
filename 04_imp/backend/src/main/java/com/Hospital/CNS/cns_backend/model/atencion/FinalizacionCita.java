package com.Hospital.CNS.cns_backend.model.atencion;

public interface FinalizacionCita {
    boolean finalizar(long citaId, long medicoId);
}