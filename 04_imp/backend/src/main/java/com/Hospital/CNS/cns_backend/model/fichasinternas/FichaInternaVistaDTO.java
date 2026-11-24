package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Ficha interna como la ve la app. {@code ticket} es el N° de ticket; {@code hora} "HH:mm"; {@code tabla}
 * GENERAL o PEDIATRIA. {@code gravedad} solo va para el medico general o el pediatra (null para el especialista
 * y el administrador): nunca sale en el ticket ni en la notificacion.
 */
public record FichaInternaVistaDTO(long ticket, Long citaOrigenId, long pacienteId, String nombrePaciente,
                                   String numeroAfiliado, int edad, TipoEspecialidad especialidad, String tabla,
                                   boolean urgencia, Gravedad gravedad, String motivo, EstadoFichaInterna estado,
                                   LocalDate fecha, String hora, int duracionMin, String medico, String consultorio,
                                   String derivadoPor, TipoEspecialidad especialidadOrigen, LocalDateTime llegada,
                                   String observaciones) {
}
