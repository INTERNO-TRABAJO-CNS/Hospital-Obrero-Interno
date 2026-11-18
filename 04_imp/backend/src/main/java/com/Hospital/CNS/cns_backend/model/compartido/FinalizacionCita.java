package com.Hospital.CNS.cns_backend.model.compartido;

public interface FinalizacionCita {
    ResultadoFinalizacion finalizar(long citaId, long medicoId);
}
