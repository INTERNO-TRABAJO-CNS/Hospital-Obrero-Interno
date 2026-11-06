package com.Hospital.CNS.cns_backend.model.compartido;

/** Especialidades con la duracion minima y maxima (en minutos) de cada ficha. */
public enum TipoEspecialidad {
    CONSULTORIO_FAMILIAR(30, 45),
    ODONTOLOGIA(45, 60),
    PEDIATRIA(30, 45);

    private final int duracionMinMinutos;
    private final int duracionMaxMinutos;

    TipoEspecialidad(int duracionMinMinutos, int duracionMaxMinutos) {
        this.duracionMinMinutos = duracionMinMinutos;
        this.duracionMaxMinutos = duracionMaxMinutos;
    }

    public int duracionMinMinutos() {
        return duracionMinMinutos;
    }

    public int duracionMaxMinutos() {
        return duracionMaxMinutos;
    }
}
