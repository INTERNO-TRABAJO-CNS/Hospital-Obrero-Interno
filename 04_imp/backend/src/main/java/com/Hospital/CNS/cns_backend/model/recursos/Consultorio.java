package com.Hospital.CNS.cns_backend.model.recursos;

/** Entidad Consultorio. Solo vive dentro del model; hacia afuera sale como {@link ConsultorioDTO}. */
public final class Consultorio {

    private final int id;
    private final String nombre;
    private final boolean activo;

    public Consultorio(int id, String nombre, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public static Consultorio desde(ConsultorioDTO d) {
        return new Consultorio(d.consultorioId(), d.nombre(), d.activo());
    }

    public ConsultorioDTO aDTO() {
        return new ConsultorioDTO(id, nombre, activo);
    }

    public Consultorio conId(int nuevoId) {
        return new Consultorio(nuevoId, nombre, activo);
    }

    public Consultorio conActivo(boolean nuevoActivo) {
        return new Consultorio(id, nombre, nuevoActivo);
    }

    public boolean isValid() {
        return Medico.hayTexto(nombre);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }
}
