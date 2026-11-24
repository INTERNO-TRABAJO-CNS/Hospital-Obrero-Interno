package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reloj real con la hora del hospital (por defecto America/La_Paz), no la de la maquina: si el servidor
 * estuviera en UTC, desde las 20:00 de Bolivia "hoy" ya seria manana.
 */
public class RelojSistema implements Reloj {

    public static final ZoneId ZONA_HOSPITAL = ZoneId.of("America/La_Paz");

    private final ZoneId zona;

    public RelojSistema() {
        this(ZONA_HOSPITAL);
    }

    public RelojSistema(ZoneId zona) {
        this.zona = zona;
    }

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now(zona);
    }
}
