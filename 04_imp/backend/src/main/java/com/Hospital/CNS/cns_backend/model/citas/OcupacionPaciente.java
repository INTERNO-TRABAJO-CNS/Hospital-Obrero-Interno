package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * T100-24: el paciente ya esta ocupado en ese intervalo por una ficha interna (ASIGNADA o ATENDIDA) o una cirugia
 * (no CANCELADA). Las citas comunes no van aqui: esas las controla la regla de una cita por dia (RN-10).
 * Implementacion: {@code PgOcupacionPaciente}.
 */
@FunctionalInterface
public interface OcupacionPaciente {

    /** Nunca ocupado (pruebas con Fakes que no usan fichas internas ni cirugias). */
    OcupacionPaciente NINGUNA = (pacienteId, fecha, inicio, fin) -> false;

    /** {@code true} si algo del paciente se cruza con {@code [inicio, fin)} ese dia. */
    boolean ocupado(long pacienteId, LocalDate fecha, LocalTime inicio, LocalTime fin);
}
