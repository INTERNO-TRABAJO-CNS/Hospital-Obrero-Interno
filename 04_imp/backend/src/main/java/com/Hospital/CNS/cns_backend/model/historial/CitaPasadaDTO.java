package com.Hospital.CNS.cns_backend.model.historial;

import java.time.LocalDate;
import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Una cita ya pasada del paciente (con cualquier medico), para la linea de tiempo del historial. */
public record CitaPasadaDTO(long citaId, LocalDate fecha, LocalTime hora, TipoEspecialidad especialidad,
                            String medico, int numeroFicha, EstadoCita estado) {
}
