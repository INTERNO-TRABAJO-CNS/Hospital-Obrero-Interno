package com.Hospital.CNS.cns_backend.model.comunicacion;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class AvisoPrevioService {
    private static final Duration VENTANA = Duration.ofMinutes(45);
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
        Instant ahora = reloj.ahora();
        int generados = 0;
        for (CitaProgramada cita : citas.programadasEntre(ahora, ahora.plus(VENTANA))) {
            if (cita.asistenciaConfirmada() || repository.avisoYaEnviado(cita.citaId())) {
                continue;
            }
            notificaciones.publicar(new NotificacionDTO(
                    cita.pacienteId(),
                    cita.citaId(),
                    "AVISO_T45",
                    MENSAJE,
                    ahora));
            repository.marcarAvisoEnviado(cita.citaId());
            generados++;
        }
        return generados;
    }
}