package com.Hospital.CNS.cns_backend.model.citas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;

/** Dias habiles del hospital: lunes a viernes que no estan registrados como DiaNoHabil activo. */
public class CalendarioHabil {

    private final DiaNoHabilRepository diasNoHabiles;

    public CalendarioHabil(DiaNoHabilRepository diasNoHabiles) {
        this.diasNoHabiles = Objects.requireNonNull(diasNoHabiles, "diasNoHabiles");
    }

    public boolean esHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !diasNoHabiles.existeActivoEnFecha(fecha);
    }

    /** El primer dia habil DESPUES de {@code fecha} (viernes &rarr; lunes). */
    public LocalDate siguienteDiaHabil(LocalDate fecha) {
        LocalDate dia = fecha.plusDays(1);
        while (!esHabil(dia)) {
            dia = dia.plusDays(1);
        }
        return dia;
    }

    /** Avanza {@code dias} dias habiles desde {@code fecha}; con 0 devuelve la misma fecha. */
    public LocalDate sumarDiasHabiles(LocalDate fecha, int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("Los dias habiles a sumar no pueden ser negativos");
        }
        LocalDate dia = fecha;
        for (int i = 0; i < dias; i++) {
            dia = siguienteDiaHabil(dia);
        }
        return dia;
    }
}
