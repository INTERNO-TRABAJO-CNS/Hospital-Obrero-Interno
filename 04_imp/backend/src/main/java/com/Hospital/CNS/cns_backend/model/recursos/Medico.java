package com.Hospital.CNS.cns_backend.model.recursos;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Entidad Medico. Vive solo dentro del model: hacia afuera sale como {@link MedicoDTO}.
 * Reglas: cupo diario de 5 a 7 fichas y duracion de ficha dentro del rango de su especialidad.
 */
public final class Medico {

    public static final int CUPO_MINIMO = 5;
    public static final int CUPO_MAXIMO = 7;

    private final long id;
    private final String nombres;
    private final String apellidos;
    private final TipoEspecialidad especialidad;
    private final int duracionCitaMin;
    private final int cupoMaximoDia;
    private final boolean activo;

    public Medico(long id, String nombres, String apellidos, TipoEspecialidad especialidad,
                  int duracionCitaMin, int cupoMaximoDia, boolean activo) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.duracionCitaMin = duracionCitaMin;
        this.cupoMaximoDia = cupoMaximoDia;
        this.activo = activo;
    }

    public static Medico desde(MedicoDTO d) {
        return new Medico(d.medicoId(), d.nombres(), d.apellidos(), d.especialidad(),
                d.duracionCitaMin(), d.cupoMaximoDia(), d.activo());
    }

    public MedicoDTO aDTO() {
        return new MedicoDTO(id, nombres, apellidos, especialidad, duracionCitaMin, cupoMaximoDia, activo);
    }

    public Medico conId(long nuevoId) {
        return new Medico(nuevoId, nombres, apellidos, especialidad, duracionCitaMin, cupoMaximoDia, activo);
    }

    public Medico conActivo(boolean nuevoActivo) {
        return new Medico(id, nombres, apellidos, especialidad, duracionCitaMin, cupoMaximoDia, nuevoActivo);
    }

    /** Nombres, apellidos y especialidad presentes. */
    public boolean datosBasicosValidos() {
        return hayTexto(nombres) && hayTexto(apellidos) && especialidad != null;
    }

    /** Cupo diario entre 5 y 7 (no se aceptan valores fuera de ese rango). */
    public boolean cupoValido() {
        return cupoMaximoDia >= CUPO_MINIMO && cupoMaximoDia <= CUPO_MAXIMO;
    }

    /** Duracion de la ficha dentro del rango de la especialidad. */
    public boolean duracionValida() {
        return especialidad != null
                && duracionCitaMin >= especialidad.duracionMinMinutos()
                && duracionCitaMin <= especialidad.duracionMaxMinutos();
    }

    public boolean isValid() {
        return datosBasicosValidos() && cupoValido() && duracionValida();
    }

    /** true si cambia algo que usa la asignacion: especialidad, duracion de ficha o cupo. */
    public boolean cambiaLaAgenda(Medico otro) {
        return especialidad != otro.especialidad
                || duracionCitaMin != otro.duracionCitaMin
                || cupoMaximoDia != otro.cupoMaximoDia;
    }

    public long getId() {
        return id;
    }

    public boolean isActivo() {
        return activo;
    }

    static boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }
}
