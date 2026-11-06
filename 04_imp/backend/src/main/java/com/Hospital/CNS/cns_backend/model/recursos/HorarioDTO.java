package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Franja semanal en la que un medico atiende en un consultorio. */
public record HorarioDTO(long horarioId, long medicoId, int consultorioId, DayOfWeek diaSemana,
                         LocalTime horaInicio, LocalTime horaFin, boolean activo) {
}
