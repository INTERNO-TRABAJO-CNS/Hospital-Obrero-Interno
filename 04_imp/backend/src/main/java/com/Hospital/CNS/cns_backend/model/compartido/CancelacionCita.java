package com.Hospital.CNS.cns_backend.model.compartido;

public interface CancelacionCita {
    ResultadoCancelacion cancelarPorPaciente(long citaId, long pacienteId);
}
