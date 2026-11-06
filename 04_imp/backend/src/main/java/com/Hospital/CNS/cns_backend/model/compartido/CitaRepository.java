package com.Hospital.CNS.cns_backend.model.compartido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository {
    long nextIdentity();

    /** false si viola UNIQUE(medico, fecha, hora). */
    boolean store(CitaDTO cita);

    Optional<CitaDTO> porId(long citaId);

    /** Todas las del dia, cualquier estado. */
    List<CitaDTO> delDia(LocalDate fecha);

    List<CitaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha);

    List<CitaDTO> delPaciente(long pacienteId);

    /** Sin contar las CANCELADA. */
    int contarDelMedicoEnFecha(long medicoId, LocalDate fecha);

    /** Sin contar las CANCELADA. */
    boolean pacienteTieneCitaEnFecha(long pacienteId, LocalDate fecha);

    boolean medicoTienePacienteEnAgenda(long medicoId, long pacienteId);

    int contarProgramadasDelMedico(long medicoId);

    int contarProgramadasEnConsultorio(int consultorioId);

    /** Citas PROGRAMADA con fecha+hora menor o igual al limite. */
    List<CitaDTO> programadasHasta(LocalDateTime limite);

    /** Atomico: UPDATE ... WHERE estado = desde. */
    boolean cambiarEstado(long citaId, EstadoCita desde, EstadoCita hasta);

    /** ASISTENCIA_CONFIRMADA -> ATENDIDA. */
    boolean registrarFin(long citaId, LocalDateTime horaFin);
}
