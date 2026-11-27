package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.Comparator;

/**
 * Regla unica de prioridad (decision del 10/11/2026): urgencia &gt; gravedad (ALTA &gt; MEDIA &gt; BAJA) &gt;
 * adulto mayor (60 o mas) &gt; orden de llegada. Menor = se atiende primero.
 */
public final class ComparadorPrioridad implements Comparator<PrioridadClinica> {

    public static final ComparadorPrioridad INSTANCIA = new ComparadorPrioridad();

    private ComparadorPrioridad() {
    }

    @Override
    public int compare(PrioridadClinica a, PrioridadClinica b) {
        if (a.urgencia() != b.urgencia()) {
            return a.urgencia() ? -1 : 1;
        }
        if (a.gravedad() != b.gravedad()) {
            return a.gravedad().compareTo(b.gravedad());
        }
        if (a.adultoMayor() != b.adultoMayor()) {
            return a.adultoMayor() ? -1 : 1;
        }
        return Long.compare(a.ordenLlegada(), b.ordenLlegada());
    }
}
