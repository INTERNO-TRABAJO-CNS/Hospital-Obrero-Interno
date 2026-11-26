package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.controller.UsuarioAutenticado;
import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;

/**
 * CU-12 (T100-10): enfermeria confirma la llegada del paciente (PROGRAMADA -> ASISTENCIA_CONFIRMADA) hasta 30
 * minutos antes de la cita, y el medico registra el fin de la atencion (ASISTENCIA_CONFIRMADA -> ATENDIDA). Cada
 * cambio queda en el historial (CU-18) y se avisa por WebSocket.
 */
public class ActualizacionEstadoService implements FinalizacionCita {

    static final long MINUTOS_ANTES_PARA_CONFIRMAR = 30;
    static final String MOTIVO_CONFIRMACION = "Paciente llegó al hospital";
    static final String MOTIVO_FIN = "Atención finalizada";

    private final CitaRepository citasRepository;
    private final CambioCitaRepository cambiosRepository;
    private final AgendaEventos agendaEventos;
    private final Reloj reloj;

    public ActualizacionEstadoService(CitaRepository citas, CambioCitaRepository cambios, AgendaEventos eventos,
                                      Reloj reloj) {
        this.citasRepository = citas;
        this.cambiosRepository = cambios;
        this.agendaEventos = eventos;
        this.reloj = reloj;
    }

    public ResultadoConfirmacion confirmarAsistencia(long citaId, UsuarioAutenticado u) {
        if (u == null || u.rol() != RolUsuario.ENFERMERIA) {
            return ResultadoConfirmacion.ROL_NO_AUTORIZADO;
        }
        Optional<CitaDTO> citaOpt = citasRepository.porId(citaId);
        if (citaOpt.isEmpty()) {
            return ResultadoConfirmacion.NO_ENCONTRADA;
        }
        CitaDTO cita = citaOpt.get();
        LocalDateTime ahora = reloj.ahora();
        LocalDate hoy = ahora.toLocalDate();
        if (!cita.fecha().equals(hoy)) {
            return ResultadoConfirmacion.NO_ES_DE_HOY;
        }
        if (cita.estado() != EstadoCita.PROGRAMADA) {
            return ResultadoConfirmacion.ESTADO_INVALIDO;
        }
        LocalDateTime limite = LocalDateTime.of(cita.fecha(), cita.hora()).minusMinutes(MINUTOS_ANTES_PARA_CONFIRMAR);
        if (ahora.isAfter(limite)) {
            return ResultadoConfirmacion.FUERA_DE_PLAZO;
        }
        if (!citasRepository.cambiarEstado(citaId, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA)) {
            return ResultadoConfirmacion.ESTADO_INVALIDO;
        }
        cambiosRepository.registrar(new CambioCitaDTO(0L, citaId, EstadoCita.ASISTENCIA_CONFIRMADA.name(),
                usuarioConRol("Enfermería", u.nombre()), ahora, MOTIVO_CONFIRMACION));
        agendaEventos.citaCambio(conEstado(cita, EstadoCita.ASISTENCIA_CONFIRMADA));
        return ResultadoConfirmacion.CONFIRMADA;
    }

    @Override
    public ResultadoFinalizacion finalizar(long citaId, long medicoId) {
        Optional<CitaDTO> citaOpt = citasRepository.porId(citaId);
        if (citaOpt.isEmpty()) {
            return ResultadoFinalizacion.NO_ENCONTRADA;
        }
        CitaDTO cita = citaOpt.get();
        if (cita.medicoId() != medicoId) {
            return ResultadoFinalizacion.NO_ES_DE_SU_AGENDA;
        }
        if (cita.estado() != EstadoCita.ASISTENCIA_CONFIRMADA) {
            return ResultadoFinalizacion.ESTADO_NO_PERMITE;
        }
        LocalDateTime ahora = reloj.ahora();
        // registrarFin es el UPDATE atomico ASISTENCIA_CONFIRMADA -> ATENDIDA que ademas guarda la hora de fin.
        if (!citasRepository.registrarFin(citaId, ahora)) {
            return ResultadoFinalizacion.ESTADO_NO_PERMITE;
        }
        cambiosRepository.registrar(new CambioCitaDTO(0L, citaId, EstadoCita.ATENDIDA.name(),
                usuarioConRol("Médico", cita.nombreMedico()), ahora, MOTIVO_FIN));
        agendaEventos.citaCambio(conEstado(cita, EstadoCita.ATENDIDA));
        return ResultadoFinalizacion.FINALIZADA;
    }

    /** "Enfermería · M. Flores", o solo el rol si no hay nombre. */
    static String usuarioConRol(String rol, String nombre) {
        return nombre == null || nombre.isBlank() ? rol : rol + " · " + nombre.trim();
    }

    private static CitaDTO conEstado(CitaDTO cita, EstadoCita estado) {
        return new CitaDTO(cita.citaId(), cita.pacienteId(), cita.nombrePaciente(), cita.numeroAfiliado(),
                cita.medicoId(), cita.nombreMedico(), cita.especialidad(), cita.consultorio(), cita.fecha(),
                cita.hora(), cita.numeroFicha(), estado);
    }
}
