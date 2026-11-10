package com.Hospital.CNS.cns_backend.model.atencion;

import java.time.LocalDate;
import java.time.LocalTime;

public record DatosAtencionDTO(
        long citaId,
        long medicoId,
        String afiliado,
        String ficha,
        LocalDate fecha,
        LocalTime hora,
        String consulta,
        EstadoCita estado) {
}