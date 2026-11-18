package com.Hospital.CNS.cns_backend.model.compartido;

/**
 * Especialidades con la duracion minima y maxima (en minutos) de cada ficha.
 *
 * <p>Decision del 10/11/2026: 3 especialidades BASICAS (las pide el paciente) y 10 INTERNAS
 * (solo por derivacion con ficha interna). Solo derivan Medico General (CONSULTORIO_FAMILIAR)
 * y PEDIATRIA; Odontologia nunca. TERAPIA_INTENSIVA atiende solo urgencias. Operan en quirofano
 * GASTROENTEROLOGIA, HEMATOLOGIA y DERMATOLOGIA (Cardiologia, Nefrologia, Neumologia y
 * Endocrinologia: {@code // PENDIENTE PV-20}).</p>
 */
public enum TipoEspecialidad {
    CONSULTORIO_FAMILIAR(Categoria.BASICA, 30, 45, 0, 0),
    ODONTOLOGIA(Categoria.BASICA, 45, 60, 0, 0),
    PEDIATRIA(Categoria.BASICA, 30, 45, 0, 0),

    CARDIOLOGIA(Categoria.INTERNA, 20, 40, 0, 0),
    NEFROLOGIA(Categoria.INTERNA, 20, 40, 0, 0),
    NEUMOLOGIA(Categoria.INTERNA, 20, 30, 0, 0),
    ENDOCRINOLOGIA(Categoria.INTERNA, 20, 40, 0, 0),
    GASTROENTEROLOGIA(Categoria.INTERNA, 20, 30, 30, 120),
    HEMATOLOGIA(Categoria.INTERNA, 20, 40, 30, 120),
    INFECTOLOGIA(Categoria.INTERNA, 30, 45, 0, 0),
    DERMATOLOGIA(Categoria.INTERNA, 15, 30, 15, 120),
    MEDICINA_TRABAJO(Categoria.INTERNA, 20, 40, 0, 0),
    TERAPIA_INTENSIVA(Categoria.INTERNA, 30, 60, 0, 0);

    /** BASICA = la pide el paciente; INTERNA = solo por derivacion (ficha interna). */
    public enum Categoria { BASICA, INTERNA }

    private final Categoria categoria;
    private final int duracionMinMinutos;
    private final int duracionMaxMinutos;
    private final int cirugiaMinMinutos;
    private final int cirugiaMaxMinutos;

    TipoEspecialidad(Categoria categoria, int duracionMinMinutos, int duracionMaxMinutos,
                     int cirugiaMinMinutos, int cirugiaMaxMinutos) {
        this.categoria = categoria;
        this.duracionMinMinutos = duracionMinMinutos;
        this.duracionMaxMinutos = duracionMaxMinutos;
        this.cirugiaMinMinutos = cirugiaMinMinutos;
        this.cirugiaMaxMinutos = cirugiaMaxMinutos;
    }

    public int duracionMinMinutos() {
        return duracionMinMinutos;
    }

    public int duracionMaxMinutos() {
        return duracionMaxMinutos;
    }

    public Categoria categoria() {
        return categoria;
    }

    public boolean esBasica() {
        return categoria == Categoria.BASICA;
    }

    public boolean esInterna() {
        return categoria == Categoria.INTERNA;
    }

    /** Solo Medico General y Pediatria generan fichas internas. */
    public boolean puedeDerivar() {
        return this == CONSULTORIO_FAMILIAR || this == PEDIATRIA;
    }

    /** Terapia Intensiva: solo reanimacion por urgencia, sin agenda ni cupo. */
    public boolean soloUrgencia() {
        return this == TERAPIA_INTENSIVA;
    }

    public boolean operaEnQuirofano() {
        return cirugiaMaxMinutos > 0;
    }

    /** 0 si la especialidad no opera. */
    public int cirugiaMinMinutos() {
        return cirugiaMinMinutos;
    }

    /** 0 si la especialidad no opera. */
    public int cirugiaMaxMinutos() {
        return cirugiaMaxMinutos;
    }
}
