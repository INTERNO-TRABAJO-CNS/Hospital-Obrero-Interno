package com.Hospital.CNS.cns_backend.model.historial;

/** Resultado del buscador del historial: un paciente de la agenda del medico (sin CI ni contacto). */
public record PacienteDeAgendaDTO(long pacienteId, String nombre, String numeroAfiliado, boolean titular, int edad) {
}
