package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;

/**
 * Agenda del medico para CU-06 (tablas citas.agenda_dia y citas.cita, migraciones V5 y V9).
 * Implementacion: PgAgendaRepository.
 */
public interface AgendaRepository {

    /**
     * Toma la agenda de ese medico en ese dia hasta que termine la transaccion en curso
     * ({@code SELECT ... FOR UPDATE SKIP LOCKED}). No espera: devuelve false si otro proceso la tiene.
     */
    boolean intentarBloquear(long medicoId, LocalDate fecha);

    /** Citas del medico ese dia que ocupan horario (sin contar CANCELADA), ordenadas por hora. */
    List<CitaDTO> ocupacionDelMedico(long medicoId, LocalDate fecha);

    /**
     * true si alguna cita (no CANCELADA) de cualquier medico usa ese consultorio en un intervalo que se cruza con
     * {@code [inicio, fin)}. La duracion de cada cita es la del medico que la atiende.
     */
    boolean consultorioOcupado(int consultorioId, LocalDate fecha, LocalTime inicio, LocalTime fin);
}
