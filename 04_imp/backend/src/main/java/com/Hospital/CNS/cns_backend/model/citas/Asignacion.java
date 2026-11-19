package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** CU-05 &rarr; CU-07: la combinacion ya validada que se registra como cita PROGRAMADA. */
public record Asignacion(long solicitudId, long pacienteId, long medicoId, TipoEspecialidad especialidad,
                         int consultorioId, LocalDate fecha, LocalTime hora, int duracionMin) {

    /** La combinacion que CU-06 debe validar. */
    public CombinacionCandidata combinacion() {
        return new CombinacionCandidata(pacienteId, medicoId, consultorioId, fecha, hora, duracionMin);
    }
}
