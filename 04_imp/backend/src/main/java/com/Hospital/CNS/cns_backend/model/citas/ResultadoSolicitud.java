package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

public record ResultadoSolicitud(
        long solicitudId,
        long pacienteId,
        EstadoSolicitud estado,
        String mensaje,
        TipoEspecialidad especialidad,
        LocalDate diaSolicitado,
        int prioridad,
        long ordenLlegada,
        Long citaId) {

    public static ResultadoSolicitud desde(SolicitudDTO solicitud) {

        String mensaje;

        if (solicitud.estado() == EstadoSolicitud.ASIGNADA) {
            mensaje = "Su cita fue asignada correctamente.";

        } else if (solicitud.estado() == EstadoSolicitud.EN_ESPERA) {

            mensaje = "Todavía no hay cupo: el sistema sigue buscando en los siguientes días hábiles y le avisará.";

        } else if (solicitud.estado() == EstadoSolicitud.SIN_CUPO) {

            mensaje = "NO HAY CUPOS — no se encontró cupo en los próximos 15 días hábiles.";

        } else if (solicitud.estado() == EstadoSolicitud.CANCELADA) {

            mensaje = "La cita fue cancelada.";

        } else {
            mensaje = "Su solicitud fue registrada. El sistema le asignará médico, hora y consultorio para ese día; "
                    + "si no hay cupo, seguirá buscando en los siguientes días hábiles y le avisará.";
        }

        return new ResultadoSolicitud(
                solicitud.solicitudId(),
                solicitud.pacienteId(),
                solicitud.estado(),
                mensaje,
                solicitud.especialidad(),
                solicitud.diaSolicitado(),
                solicitud.prioridad(),
                solicitud.ordenLlegada(),
                solicitud.citaId()
        );
    }
}