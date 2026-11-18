package com.Hospital.CNS.cns_backend.model.recursos;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Datos de un medico que viajan entre controller, service y repository. */
public record MedicoDTO(long medicoId, String nombres, String apellidos, TipoEspecialidad especialidad,
                        int duracionCitaMin, int cupoMaximoDia, boolean activo) {

    /**
     * Lectura desde JSON. El cliente puede omitir medicoId y activo (los pone el Service) y un numero
     * faltante llega como 0, asi la regla correspondiente responde con su propio codigo (p. ej.
     * VALIDACION_CUPO) en vez de un error de formato.
     */
    @JsonCreator
    static MedicoDTO desdeJson(@JsonProperty("medicoId") Long medicoId,
                               @JsonProperty("nombres") String nombres,
                               @JsonProperty("apellidos") String apellidos,
                               @JsonProperty("especialidad") TipoEspecialidad especialidad,
                               @JsonProperty("duracionCitaMin") Integer duracionCitaMin,
                               @JsonProperty("cupoMaximoDia") Integer cupoMaximoDia,
                               @JsonProperty("activo") Boolean activo) {
        return new MedicoDTO(JsonOpcional.largo(medicoId), nombres, apellidos, especialidad,
                JsonOpcional.entero(duracionCitaMin), JsonOpcional.entero(cupoMaximoDia), JsonOpcional.logico(activo));
    }
}
