package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

public record CambioCitaDTO(long cambioId, long citaId, String accion, String usuario, LocalDateTime fechaHora,
                            String motivo) {
}
