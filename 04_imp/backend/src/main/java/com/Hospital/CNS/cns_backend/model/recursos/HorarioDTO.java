package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Franja semanal en la que un medico atiende en un consultorio. */
public record HorarioDTO(long horarioId, long medicoId, int consultorioId, DayOfWeek diaSemana,
                         LocalTime horaInicio, LocalTime horaFin, boolean activo) {

    /** Lectura desde JSON: horarioId y activo son opcionales; un id faltante llega como 0 (DATOS_INVALIDOS). */
    @JsonCreator
    static HorarioDTO desdeJson(@JsonProperty("horarioId") Long horarioId,
                                @JsonProperty("medicoId") Long medicoId,
                                @JsonProperty("consultorioId") Integer consultorioId,
                                @JsonProperty("diaSemana") DayOfWeek diaSemana,
                                @JsonProperty("horaInicio") LocalTime horaInicio,
                                @JsonProperty("horaFin") LocalTime horaFin,
                                @JsonProperty("activo") Boolean activo) {
        return new HorarioDTO(JsonOpcional.largo(horarioId), JsonOpcional.largo(medicoId),
                JsonOpcional.entero(consultorioId), diaSemana, horaInicio, horaFin, JsonOpcional.logico(activo));
    }
}
