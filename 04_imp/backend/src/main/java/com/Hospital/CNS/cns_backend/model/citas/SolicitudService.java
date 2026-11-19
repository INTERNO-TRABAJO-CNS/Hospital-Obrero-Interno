package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoSolicitud;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudRepository;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

public class SolicitudService {

    private final SolicitudRepository solicitudes;
    private final CitaRepository citas;
    private final CalendarioHabil calendario;
    private final Reloj reloj;

    public SolicitudService(
            SolicitudRepository s,
            CitaRepository c,
            CalendarioHabil cal,
            Reloj reloj) {

        this.solicitudes = s;
        this.citas = c;
        this.calendario = cal;
        this.reloj = reloj;
    }

    public SolicitudDTO solicitar(
            long pacienteId,
            TipoEspecialidad especialidad,
            LocalDate dia) {

        validarDatos(pacienteId, especialidad, dia);
        validarDia(dia);
        validarRango(dia);
        validarDisponibilidadPaciente(pacienteId, dia);

        long id = solicitudes.nextIdentity();

        SolicitudDTO solicitud = new SolicitudDTO(
                id,
                pacienteId,
                especialidad,
                dia,
                1,
                id,
                EstadoSolicitud.PENDIENTE,
                null
        );

        solicitudes.store(solicitud);

        return solicitud;
    }

    public SolicitudDTO solicitarReprogramada(
            long pacienteId,
            TipoEspecialidad especialidad,
            LocalDate dia) {

        validarDatos(pacienteId, especialidad, dia);
        validarDia(dia);
        validarRango(dia);
        validarDisponibilidadPaciente(pacienteId, dia);

        long id = solicitudes.nextIdentity();

        SolicitudDTO solicitud = new SolicitudDTO(
                id,
                pacienteId,
                especialidad,
                dia,
                2,
                id,
                EstadoSolicitud.PENDIENTE,
                null
        );

        solicitudes.store(solicitud);

        return solicitud;
    }

    public List<SolicitudDTO> listarPorPaciente(long pacienteId) {

        if (pacienteId <= 0) {
            throw new DominioException(
                    "DATOS_INVALIDOS",
                    "El identificador del paciente no es valido"
            );
        }

        return solicitudes.porPaciente(pacienteId);
    }

    private void validarDatos(
            long pacienteId,
            TipoEspecialidad especialidad,
            LocalDate dia) {

        if (pacienteId <= 0 || especialidad == null || dia == null) {
            throw new DominioException(
                    "DATOS_INVALIDOS",
                    "Debe proporcionar un paciente, especialidad y dia validos"
            );
        }

        if (!especialidad.esBasica()) {
            throw new DominioException(
                    "ESPECIALIDAD_INVALIDA",
                    "Solo se permiten Atencion General, Odontologia y Pediatria"
            );
        }
    }

    private void validarDia(LocalDate dia) {

        if (!calendario.esHabil(dia)) {
            throw new DominioException(
                    "DIA_NO_HABIL",
                    "El dia seleccionado no es habil"
            );
        }
    }

    private void validarRango(LocalDate dia) {

        LocalDate hoy = reloj.ahora().toLocalDate();

        int diasHabiles = 0;
        LocalDate fecha = hoy;

        while (diasHabiles < 15) {

            fecha = fecha.plusDays(1);

            if (calendario.esHabil(fecha)) {
                diasHabiles++;
            }

            if (fecha.equals(dia)) {
                return;
            }
        }

        throw new DominioException(
                "FUERA_DE_RANGO",
                "La fecha debe estar dentro de los proximos 15 dias habiles"
        );
    }

    private void validarDisponibilidadPaciente(
            long pacienteId,
            LocalDate dia) {

        if (citas.pacienteTieneCitaEnFecha(pacienteId, dia)
                || solicitudes.pacienteTieneSolicitudActivaEnDia(pacienteId, dia)) {

            throw new DominioException(
                    "YA_TIENE_CITA_ESE_DIA",
                    "El paciente ya tiene una cita o solicitud activa para ese dia"
            );
        }
    }

    public SolicitudDTO consultar(
            long pacienteId,
            long solicitudId) {

        if (pacienteId <= 0 || solicitudId <= 0) {
            throw new DominioException(
                    "DATOS_INVALIDOS",
                    "Los identificadores deben ser validos"
            );
        }

        SolicitudDTO solicitud = solicitudes.porId(solicitudId)
                .orElseThrow(() -> new DominioException(
                        "NO_ENCONTRADA",
                        "No se encontro la solicitud"
                ));

        if (solicitud.pacienteId() != pacienteId) {
            throw new DominioException(
                    "NO_ES_DEL_PACIENTE",
                    "No tiene permiso para consultar esta solicitud"
            );
        }

        return solicitud;
    }
}