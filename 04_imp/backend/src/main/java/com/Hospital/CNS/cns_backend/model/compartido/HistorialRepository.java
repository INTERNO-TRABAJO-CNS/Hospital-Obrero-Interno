package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.Optional;

public interface HistorialRepository {
    void crearSiNoExiste(long pacienteId);

    Optional<HistorialDTO> porPaciente(long pacienteId);

    RecetaDTO agregarReceta(RecetaDTO receta);
}
