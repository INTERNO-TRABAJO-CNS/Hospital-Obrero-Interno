package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.time.Instant;

public record CitaProgramada(
        long citaId,
        long pacienteId,
        Instant inicio,
        boolean asistenciaConfirmada) {
}