package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;

/** CU-19 paso 3: una ficha del dia de un medico (numero, horario, estado), vista por el administrador. */
public record FichaSupervisionDTO(long citaId, int numeroFicha, LocalTime hora, EstadoCita estado, int consultorio,
                                  String nombrePaciente) {
}
