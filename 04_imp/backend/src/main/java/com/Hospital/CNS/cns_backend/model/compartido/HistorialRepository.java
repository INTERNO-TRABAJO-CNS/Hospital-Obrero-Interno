package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.Optional;

public interface HistorialRepository {
    void crearSiNoExiste(long pacienteId);

    Optional<HistorialDTO> porPaciente(long pacienteId);

    RecetaDTO agregarReceta(RecetaDTO receta);

    /**
     * Compensacion de CU-15: quita una receta recien agregada cuando la cita no se pudo finalizar.
     * Solo la usa AtencionService; el historial nunca se edita desde otra parte.
     */
    void eliminarReceta(long recetaId);
}
