package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalTime;

public record FichaTvDTO(
        int numeroFicha,
        LocalTime hora,
        TipoEspecialidad especialidad,
        int consultorio,
        EstadoCita estado
) {
}