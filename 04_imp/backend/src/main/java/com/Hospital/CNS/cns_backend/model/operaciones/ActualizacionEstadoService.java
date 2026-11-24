package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.*;
import org.springframework.stereotype.Service;
import com.Hospital.CNS.cns_backend.controller.UsuarioAutenticado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ActualizacionEstadoService implements FinalizacionCita {

    private final CitaRepository citasRepository;
    private final CambioCitaRepository cambiosRepository;
    private final AgendaEventos agendaEventos;
    private final Reloj reloj;

    public ActualizacionEstadoService(
            CitaRepository citas,
            CambioCitaRepository cambios,
            AgendaEventos eventos,
            Reloj reloj) {
        this.citasRepository = citas;
        this.cambiosRepository = cambios;
        this.agendaEventos = eventos;
        this.reloj = reloj;
    }

    public ResultadoConfirmacion confirmarAsistencia(long citaId, UsuarioAutenticado u) {
        // Validar rol exclusivo de enfermería usando el enum directamente
        if (u == null || u.rol() == null || u.rol() != RolUsuario.ENFERMERIA) {
            return ResultadoConfirmacion.ROL_NO_AUTORIZADO;
        }

        Optional<CitaDTO> citaOpt = citasRepository.porId(citaId);
        if (citaOpt.isEmpty()) {
            return ResultadoConfirmacion.NO_ENCONTRADA;
        }

        CitaDTO cita = citaOpt.get();
        LocalDate hoy = reloj.ahora().toLocalDate();

        // Validar que la cita sea estrictamente de hoy
        if (!cita.fecha().equals(hoy)) {
            return ResultadoConfirmacion.NO_ES_DE_HOY;
        }

        // Validar estado inicial PROGRAMADA
        if (cita.estado() != EstadoCita.PROGRAMADA) {
            return ResultadoConfirmacion.ESTADO_INVALIDO;
        }

        // Validar regla de tiempo: ahora <= horaCita - 30 min
        LocalDateTime fechaHoraCita = LocalDateTime.of(cita.fecha(), cita.hora());
        LocalDateTime limiteT30 = fechaHoraCita.minusMinutes(30);
        LocalDateTime ahora = reloj.ahora();

        if (ahora.isAfter(limiteT30)) {
            return ResultadoConfirmacion.FUERA_DE_PLAZO;
        }

        // Cambio de estado atómico
        boolean actualizado = citasRepository.cambiarEstado(citaId, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA);
        if (!actualizado) {
            return ResultadoConfirmacion.ESTADO_INVALIDO;
        }

        cambiosRepository.registrar(new CambioCitaDTO(0L, citaId, "ASISTENCIA_CONFIRMADA", "enfermeria", reloj.ahora(), null));
        agendaEventos.citaCambio(cita);

        return ResultadoConfirmacion.CONFIRMADA;
    }

    @Override
    public ResultadoFinalizacion finalizar(long citaId, long medicoId) {
        Optional<CitaDTO> citaOpt = citasRepository.porId(citaId);
        if (citaOpt.isEmpty()) {
            return ResultadoFinalizacion.NO_ENCONTRADA;
        }

        CitaDTO cita = citaOpt.get();

        // Validar que sea el médico dueño de la cita
        if (cita.medicoId() != medicoId) {
            return ResultadoFinalizacion.NO_ES_DE_SU_AGENDA;
        }

        // Validar estado actual ASISTENCIA_CONFIRMADA
        if (cita.estado() != EstadoCita.ASISTENCIA_CONFIRMADA) {
            return ResultadoFinalizacion.ESTADO_NO_PERMITE;
        }

        boolean actualizado = citasRepository.cambiarEstado(citaId, EstadoCita.ASISTENCIA_CONFIRMADA, EstadoCita.ATENDIDA);
        if (!actualizado) {
            return ResultadoFinalizacion.ESTADO_NO_PERMITE;
        }

        citasRepository.registrarFin(citaId, reloj.ahora());
        cambiosRepository.registrar(new CambioCitaDTO(0L, citaId, "ATENDIDA", "medico", reloj.ahora(), null));
        agendaEventos.citaCambio(cita);

        return ResultadoFinalizacion.FINALIZADA;
    }
}