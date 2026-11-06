package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;

/** Entidad DiaNoHabil. Solo vive dentro del model; hacia afuera sale como {@link DiaNoHabilDTO}. */
public final class DiaNoHabil {

    private final long id;
    private final LocalDate fecha;
    private final String descripcion;
    private final boolean activo;

    public DiaNoHabil(long id, LocalDate fecha, String descripcion, boolean activo) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public static DiaNoHabil desde(DiaNoHabilDTO d) {
        return new DiaNoHabil(d.diaNoHabilId(), d.fecha(), d.descripcion(), d.activo());
    }

    public DiaNoHabilDTO aDTO() {
        return new DiaNoHabilDTO(id, fecha, descripcion, activo);
    }

    public DiaNoHabil conId(long nuevoId) {
        return new DiaNoHabil(nuevoId, fecha, descripcion, activo);
    }

    /** Fecha y descripcion presentes. */
    public boolean datosBasicosValidos() {
        return fecha != null && Medico.hayTexto(descripcion);
    }

    /** La fecha es posterior a hoy. Se recibe "hoy" para poder probarlo con un reloj fijo. */
    public boolean fechaFutura(LocalDate hoy) {
        return fecha != null && fecha.isAfter(hoy);
    }

    public boolean isValid(LocalDate hoy) {
        return datosBasicosValidos() && fechaFutura(hoy);
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
