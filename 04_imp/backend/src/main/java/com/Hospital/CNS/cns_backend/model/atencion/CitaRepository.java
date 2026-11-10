package com.Hospital.CNS.cns_backend.model.atencion;

import java.util.Optional;

public interface CitaRepository {
    Optional<DatosAtencionDTO> buscarParaAtencion(long citaId);
}