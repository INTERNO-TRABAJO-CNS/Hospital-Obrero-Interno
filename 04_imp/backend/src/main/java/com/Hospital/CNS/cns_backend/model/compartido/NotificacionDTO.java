package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

public record NotificacionDTO(long notificacionId, long pacienteId, Long citaId, String tipo, String mensaje,
                              LocalDateTime fechaHora) {
}
