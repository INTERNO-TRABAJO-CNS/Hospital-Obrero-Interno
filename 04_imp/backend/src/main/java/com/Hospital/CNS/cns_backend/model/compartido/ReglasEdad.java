package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.Period;

/** Pediatria solo menores de 10 anios; adulto mayor = 60 o mas (Ley 369). */
public final class ReglasEdad {

    public static final int EDAD_LIMITE_PEDIATRIA = 10;
    public static final int EDAD_ADULTO_MAYOR = 60;

    private ReglasEdad() {
    }

    public static boolean admitidoEnPediatria(int edad) {
        return edad < EDAD_LIMITE_PEDIATRIA;
    }

    public static boolean esAdultoMayor(int edad) {
        return edad >= EDAD_ADULTO_MAYOR;
    }

    /** Anios cumplidos en {@code fecha}; 0 si no se conoce la fecha de nacimiento. */
    public static int edadEn(LocalDate nacimiento, LocalDate fecha) {
        if (nacimiento == null || nacimiento.isAfter(fecha)) {
            return 0;
        }
        return Period.between(nacimiento, fecha).getYears();
    }
}
