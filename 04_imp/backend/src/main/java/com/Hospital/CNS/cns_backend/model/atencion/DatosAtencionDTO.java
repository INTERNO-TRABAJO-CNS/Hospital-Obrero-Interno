package com.Hospital.CNS.cns_backend.model.atencion;

import java.time.LocalDate;
import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-15: datos autocompletados de la cita en atencion (afiliado, ficha, fecha, hora y consulta).
 *
 * <p>{@code consulta} es la especialidad de la cita: una basica (Medico General, Odontologia, Pediatria)
 * o una interna (las 10 especialidades de derivacion); {@code categoriaConsulta} dice cual de las dos.
 * {@code puedeDerivar} habilita el boton «Generar ticket interno» (solo Medico General y Pediatria).</p>
 */
public record DatosAtencionDTO(long citaId, long pacienteId, String nombrePaciente, String numeroAfiliado,
                               int numeroFicha, LocalDate fecha, LocalTime hora, TipoEspecialidad consulta,
                               TipoEspecialidad.Categoria categoriaConsulta, int consultorio, String medico,
                               boolean puedeDerivar) {
}
