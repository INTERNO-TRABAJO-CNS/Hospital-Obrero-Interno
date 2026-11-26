package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.util.Optional;

/**
 * T100-24: fecha de nacimiento del paciente, para la regla de Pediatria (solo menores de 10 anios). En produccion la
 * da {@code PacienteRepository}; en las pruebas, un mapa o {@link #DESCONOCIDA}.
 */
@FunctionalInterface
public interface EdadPaciente {

    /** Sin datos de edad: la regla de Pediatria no se puede aplicar (solo para pruebas antiguas con Fakes). */
    EdadPaciente DESCONOCIDA = pacienteId -> Optional.empty();

    Optional<LocalDate> fechaNacimiento(long pacienteId);
}
