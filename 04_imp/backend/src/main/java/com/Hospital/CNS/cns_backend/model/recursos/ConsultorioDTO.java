package com.Hospital.CNS.cns_backend.model.recursos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de un consultorio. */
public record ConsultorioDTO(int consultorioId, String nombre, boolean activo) {

    /** Lectura desde JSON: consultorioId y activo son opcionales (los pone el Service). */
    @JsonCreator
    static ConsultorioDTO desdeJson(@JsonProperty("consultorioId") Integer consultorioId,
                                    @JsonProperty("nombre") String nombre,
                                    @JsonProperty("activo") Boolean activo) {
        return new ConsultorioDTO(JsonOpcional.entero(consultorioId), nombre, JsonOpcional.logico(activo));
    }
}
