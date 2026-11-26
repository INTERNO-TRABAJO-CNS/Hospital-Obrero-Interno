package com.Hospital.CNS.cns_backend.model.operaciones;

import java.time.LocalDateTime;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.AgendaEventos;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

/**
 * CU-11: cancela citas a peticion del paciente o automaticamente al llegar a T-30.
 */
public class CancelacionService implements CancelacionCita {

    private static final long MINUTOS_ANTES_DE_CITA = 30;
    private static final String MOTIVO_PACIENTE = "Cancelada por el paciente";
    private static final String MOTIVO_AUTOMATICO = "No confirmó asistencia";

    private final CitaRepository citas;
    private final CambioCitaRepository cambios;
    private final NotificacionPublicador notificaciones;
    private final AgendaEventos eventos;
    private final Reloj reloj;

    public CancelacionService(CitaRepository c, CambioCitaRepository cambios, NotificacionPublicador n,
                              AgendaEventos e, Reloj reloj) {
        this.citas = c;
        this.cambios = cambios;
        this.notificaciones = n;
        this.eventos = e;
        this.reloj = reloj;
    }

    @Override
    public ResultadoCancelacion cancelarPorPaciente(long citaId, long pacienteId) {
        CitaDTO cita = citas.porId(citaId).orElse(null);
        if (cita == null) {
            return ResultadoCancelacion.NO_ENCONTRADA;
        }
        if (cita.pacienteId() != pacienteId) {
            return ResultadoCancelacion.NO_ES_DEL_PACIENTE;
        }
        if (cita.estado() != EstadoCita.PROGRAMADA) {
            return ResultadoCancelacion.ESTADO_NO_PERMITE;
        }
        return cancelar(cita, ActualizacionEstadoService.usuarioConRol("Paciente", cita.nombrePaciente()),
                MOTIVO_PACIENTE, reloj.ahora())
                ? ResultadoCancelacion.CANCELADA
                : ResultadoCancelacion.ESTADO_NO_PERMITE;
    }

    public int cancelarAutomaticas() {
        LocalDateTime ahora = reloj.ahora();
        LocalDateTime limite = ahora.plusMinutes(MINUTOS_ANTES_DE_CITA);
        List<CitaDTO> candidatas = citas.programadasHasta(limite);
        int canceladas = 0;
        for (CitaDTO cita : candidatas) {
            if (cita.estado() == EstadoCita.PROGRAMADA
                    && cancelar(cita, "Sistema", MOTIVO_AUTOMATICO, ahora)) {
                canceladas++;
            }
        }
        return canceladas;
    }

    private boolean cancelar(CitaDTO cita, String usuario, String motivo, LocalDateTime ahora) {
        if (!citas.cambiarEstado(cita.citaId(), EstadoCita.PROGRAMADA, EstadoCita.CANCELADA)) {
            return false;
        }
        CitaDTO cancelada = conEstado(cita, EstadoCita.CANCELADA);
        cambios.registrar(new CambioCitaDTO(0, cita.citaId(), EstadoCita.CANCELADA.name(), usuario, ahora, motivo));
        notificaciones.publicar(new NotificacionDTO(0, cita.pacienteId(), cita.citaId(), null, null,
                "CITA_CANCELADA", "Su cita N° " + cita.numeroFicha() + " fue cancelada. Motivo: " + motivo + ".",
                ahora));
        eventos.citaCambio(cancelada);
        return true;
    }

    private static CitaDTO conEstado(CitaDTO cita, EstadoCita estado) {
        return new CitaDTO(cita.citaId(), cita.pacienteId(), cita.nombrePaciente(), cita.numeroAfiliado(),
                cita.medicoId(), cita.nombreMedico(), cita.especialidad(), cita.consultorio(), cita.fecha(),
                cita.hora(), cita.numeroFicha(), estado);
    }
}
