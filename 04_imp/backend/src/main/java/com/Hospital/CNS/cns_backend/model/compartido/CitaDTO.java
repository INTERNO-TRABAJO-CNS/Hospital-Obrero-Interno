package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaDTO(long citaId, long pacienteId, String nombrePaciente, String numeroAfiliado,
                      long medicoId, String nombreMedico, TipoEspecialidad especialidad, int consultorio,
                      LocalDate fecha, LocalTime hora, int numeroFicha, EstadoCita estado) {
}
