package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Fecha en la que no se asignan citas (carga manual, sin calendario oficial). */
public record DiaNoHabilDTO(long diaNoHabilId, LocalDate fecha, String descripcion, boolean activo) {

    /** Lectura desde JSON: diaNoHabilId y activo son opcionales (los pone el Service). */
    @JsonCreator
    static DiaNoHabilDTO desdeJson(@JsonProperty("diaNoHabilId") Long diaNoHabilId,
                                   @JsonProperty("fecha") LocalDate fecha,
                                   @JsonProperty("descripcion") String descripcion,
                                   @JsonProperty("activo") Boolean activo) {
        return new DiaNoHabilDTO(JsonOpcional.largo(diaNoHabilId), fecha, descripcion, JsonOpcional.logico(activo));
    }
}
