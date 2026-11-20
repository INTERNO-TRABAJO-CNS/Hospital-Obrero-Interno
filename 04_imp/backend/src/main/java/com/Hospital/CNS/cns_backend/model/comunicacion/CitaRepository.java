package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.time.Instant;
import java.util.List;

public interface CitaRepository {
    List<CitaProgramada> programadasEntre(Instant desde, Instant hasta);
}