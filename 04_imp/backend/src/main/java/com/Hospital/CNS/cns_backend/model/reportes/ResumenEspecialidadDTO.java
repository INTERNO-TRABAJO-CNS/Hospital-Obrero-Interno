package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-19 paso 2: una fila del resumen por especialidad, con un medico, su horario del dia y cuantas fichas tiene.
 *
 * @param horaInicio inicio del horario del medico ese dia ({@code null} si ese dia no tiene horario)
 * @param horaFin    fin del horario del medico ese dia ({@code null} si ese dia no tiene horario)
 * @param fichas     fichas que ocupan cupo (todas menos las CANCELADAS)
 */
public record ResumenEspecialidadDTO(TipoEspecialidad especialidad, long medicoId, String nombreMedico,
                                     LocalTime horaInicio, LocalTime horaFin, int cupoMaximo, int fichas,
                                     int programadas, int confirmadas, int atendidas, int canceladas) {
}
