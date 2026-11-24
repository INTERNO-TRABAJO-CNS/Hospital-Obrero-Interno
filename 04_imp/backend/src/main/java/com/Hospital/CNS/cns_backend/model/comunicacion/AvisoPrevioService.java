package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

@Service
public class AvisoPrevioService {
    private static final int VENTANA_MINUTOS = 45;
    private static final String MENSAJE =
            "Su cita será cancelada si no confirma su asistencia en el hospital hasta 30 min antes";

    private final CitaRepository citas;
    private final NotificacionPublicador notificaciones;
    private final NotificacionRepository repository;
    private final Reloj reloj;

    public AvisoPrevioService(
            CitaRepository citas,
            NotificacionPublicador notificaciones,
            NotificacionRepository repository,
            Reloj reloj) {
        this.citas = Objects.requireNonNull(citas);
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.repository = Objects.requireNonNull(repository);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public int generarAvisosT45() {
        LocalDateTime ahora = reloj.ahora();
        LocalDateTime limite = ahora.plusMinutes(VENTANA_MINUTOS);
        int generados = 0;
        for (CitaDTO cita : citas.programadasHasta(limite)) {
            LocalDateTime inicio = cita.fecha().atTime(cita.hora());
            if (cita.estado() != EstadoCita.PROGRAMADA
                    || !inicio.isAfter(ahora)
                    || inicio.isAfter(limite)
                    || repository.avisoYaEnviado(cita.citaId())) {
                continue;
            }
            notificaciones.publicar(new NotificacionDTO(
                    0,
                    cita.pacienteId(),
                    cita.citaId(),
                    null,
                    null,
                    "AVISO_T45",
                    MENSAJE,
                    ahora));
            generados++;
        }
        return generados;
    }
}