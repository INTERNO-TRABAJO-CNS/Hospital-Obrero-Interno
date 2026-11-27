package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * CU-06: combinacion que se quiere validar (la elige T100-03). {@code duracionMin} es la duracion fija del medico
 * (dentro del rango de su especialidad); la cita ocupa {@code [hora, hora + duracionMin)}.
 */
public record CombinacionCandidata(long pacienteId, long medicoId, int consultorioId, LocalDate fecha,
                                   LocalTime hora, int duracionMin) {

    public CombinacionCandidata {
        Objects.requireNonNull(fecha, "fecha");
        Objects.requireNonNull(hora, "hora");
        if (duracionMin <= 0) {
            throw new IllegalArgumentException("La duracion de la cita debe ser mayor a 0 minutos");
        }
    }

    /** Hora en que termina la cita (exclusiva). */
    public LocalTime fin() {
        return hora.plusMinutes(duracionMin);
    }
}
