package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.time.Instant;

public record NotificacionDTO(
        long pacienteId,
        long citaId,
        String tipo,
        String mensaje,
        Instant creadaEn) {
}