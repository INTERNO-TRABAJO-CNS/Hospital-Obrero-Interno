package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.LocalTime;

/** Fabrica de CitaDTO para las pruebas. */
public final class Citas {

    private Citas() {
    }

    public static CitaDTO cita(long citaId, long pacienteId, long medicoId, LocalDate fecha, LocalTime hora,
                               int ficha, EstadoCita estado) {
        return cita(citaId, pacienteId, medicoId, TipoEspecialidad.CONSULTORIO_FAMILIAR, fecha, hora, ficha, estado);
    }

    public static CitaDTO cita(long citaId, long pacienteId, long medicoId, TipoEspecialidad especialidad,
                               LocalDate fecha, LocalTime hora, int ficha, EstadoCita estado) {
        return new CitaDTO(citaId, pacienteId, "Paciente " + pacienteId, "AF-" + pacienteId,
                medicoId, "Medico " + medicoId, especialidad, 1, fecha, hora, ficha, estado);
    }
}
