package com.Hospital.CNS.cns_backend.model.citas;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;

/**
 * Comprueba si una fecha es hábil considerando fines de semana
 * y días no hábiles registrados en la base de datos.
 */
public class CalendarioHabilImpl implements CalendarioHabil {

    private final DiaNoHabilRepository diasNoHabiles;

    public CalendarioHabilImpl(DiaNoHabilRepository diasNoHabiles) {
        this.diasNoHabiles = diasNoHabiles;
    }

    @Override
    public boolean esHabil(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }

        DayOfWeek dia = fecha.getDayOfWeek();

        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return false;
        }

        return !diasNoHabiles.existeActivoEnFecha(fecha);
    }
}
