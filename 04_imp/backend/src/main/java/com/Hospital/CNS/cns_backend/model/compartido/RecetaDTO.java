package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;

public record RecetaDTO(long recetaId, long citaId, long pacienteId, String medicacion, LocalDateTime fechaHora,
                        int numeroFicha) {
}
