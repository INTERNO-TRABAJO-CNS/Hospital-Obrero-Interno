package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Entidad HorarioMedico. Solo vive dentro del model; hacia afuera sale como {@link HorarioDTO}. */
public final class HorarioMedico {

    private final long id;
    private final long medicoId;
    private final int consultorioId;
    private final DayOfWeek diaSemana;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final boolean activo;

    public HorarioMedico(long id, long medicoId, int consultorioId, DayOfWeek diaSemana,
                         LocalTime horaInicio, LocalTime horaFin, boolean activo) {
        this.id = id;
        this.medicoId = medicoId;
        this.consultorioId = consultorioId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = activo;
    }

    public static HorarioMedico desde(HorarioDTO d) {
        return new HorarioMedico(d.horarioId(), d.medicoId(), d.consultorioId(), d.diaSemana(),
                d.horaInicio(), d.horaFin(), d.activo());
    }

    public HorarioDTO aDTO() {
        return new HorarioDTO(id, medicoId, consultorioId, diaSemana, horaInicio, horaFin, activo);
    }

    public HorarioMedico conId(long nuevoId) {
        return new HorarioMedico(nuevoId, medicoId, consultorioId, diaSemana, horaInicio, horaFin, activo);
    }

    /** Medico y consultorio indicados, dia y horas presentes, y inicio menor que fin. */
    public boolean isValid() {
        return medicoId > 0 && consultorioId > 0 && diaSemana != null
                && horaInicio != null && horaFin != null && horaInicio.isBefore(horaFin);
    }

    /** Mismo dia y franjas que se cruzan. Si una termina justo cuando la otra empieza, NO se solapan. */
    public boolean seSolapaCon(HorarioMedico otro) {
        return diaSemana == otro.diaSemana
                && horaInicio.isBefore(otro.horaFin)
                && otro.horaInicio.isBefore(horaFin);
    }

    public long getId() {
        return id;
    }

    public long getMedicoId() {
        return medicoId;
    }

    public int getConsultorioId() {
        return consultorioId;
    }

    public boolean isActivo() {
        return activo;
    }
}
