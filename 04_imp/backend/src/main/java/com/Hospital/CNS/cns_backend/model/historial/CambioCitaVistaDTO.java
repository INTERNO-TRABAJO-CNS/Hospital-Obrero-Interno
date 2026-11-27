package com.Hospital.CNS.cns_backend.model.historial;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una fila de "Historial de cambios" (CU-18): el cambio y los datos de la cita para reconocerla
 * ("Ficha 3 · Dr. Ramos"). Si la cita ya no se encuentra, {@code numeroFicha} es 0 y los textos van vacios.
 */
public record CambioCitaVistaDTO(long cambioId, long citaId, int numeroFicha, String medico, String paciente,
                                 LocalDate fechaCita, String accion, String usuario, LocalDateTime fechaHora,
                                 String motivo) {
}
