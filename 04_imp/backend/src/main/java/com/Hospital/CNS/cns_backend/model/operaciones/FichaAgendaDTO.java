package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;

/**
 * Una fila de la agenda: ficha, hora, paciente y estado (las CANCELADAS se muestran marcadas).
 * {@code pacienteId} permite abrir el historial (CU-14) desde la cita.
 */
public record FichaAgendaDTO(long citaId, int numeroFicha, LocalTime hora, String nombrePaciente, EstadoCita estado,
                             long pacienteId) {
}
