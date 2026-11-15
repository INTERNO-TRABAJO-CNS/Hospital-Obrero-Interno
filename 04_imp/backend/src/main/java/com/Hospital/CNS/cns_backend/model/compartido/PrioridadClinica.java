package com.Hospital.CNS.cns_backend.model.compartido;

/** Lo que decide el orden de atencion de una ficha interna o una cirugia. */
public record PrioridadClinica(boolean urgencia, Gravedad gravedad, boolean adultoMayor, long ordenLlegada) {
}
