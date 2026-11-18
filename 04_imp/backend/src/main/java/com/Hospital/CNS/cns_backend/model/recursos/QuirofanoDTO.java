package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Quirofano del catalogo (T100-19). Pertenece a UNA especialidad que opera; horario por defecto 07:00-19:00
 * ({@code // PENDIENTE PV-19}); limpieza de 30 a 45 min despues de cada cirugia.
 */
public record QuirofanoDTO(int quirofanoId, String nombre, TipoEspecialidad especialidad, LocalTime apertura,
                           LocalTime cierre, int limpiezaMin, boolean activo) {

    public static final LocalTime APERTURA_DEFECTO = LocalTime.of(7, 0);
    public static final LocalTime CIERRE_DEFECTO = LocalTime.of(19, 0);

    /** Lectura desde JSON: quirofanoId, horario y activo son opcionales. */
    @JsonCreator
    static QuirofanoDTO desdeJson(@JsonProperty("quirofanoId") Integer quirofanoId,
                                  @JsonProperty("nombre") String nombre,
                                  @JsonProperty("especialidad") TipoEspecialidad especialidad,
                                  @JsonProperty("apertura") LocalTime apertura,
                                  @JsonProperty("cierre") LocalTime cierre,
                                  @JsonProperty("limpiezaMin") Integer limpiezaMin,
                                  @JsonProperty("activo") Boolean activo) {
        return new QuirofanoDTO(JsonOpcional.entero(quirofanoId), nombre, especialidad,
                apertura == null ? APERTURA_DEFECTO : apertura, cierre == null ? CIERRE_DEFECTO : cierre,
                JsonOpcional.entero(limpiezaMin), activo == null || activo);
    }
}
