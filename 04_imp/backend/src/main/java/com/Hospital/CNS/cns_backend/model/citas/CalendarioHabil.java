package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;

/**
 * Permite consultar si una fecha es hábil para solicitar citas.
 */
public interface CalendarioHabil {

    boolean esHabil(LocalDate fecha);
}