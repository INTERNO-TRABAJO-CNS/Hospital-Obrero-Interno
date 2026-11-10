package com.Hospital.CNS.cns_backend.model.atencion;

import java.util.Objects;

public class AtencionService {
    private static final int MAX_MEDICACION = 2000;

    private final CitaRepository citas;
    private final HistorialRepository historial;
    private final FinalizacionCita finalizacion;
    private final Reloj reloj;

    public AtencionService(
            CitaRepository citas,
            HistorialRepository historial,
            FinalizacionCita finalizacion,
            Reloj reloj) {
        this.citas = Objects.requireNonNull(citas);
        this.historial = Objects.requireNonNull(historial);
        this.finalizacion = Objects.requireNonNull(finalizacion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public DatosAtencionDTO datosParaAtencion(long citaId, long medicoId) {
        DatosAtencionDTO datos = citas.buscarParaAtencion(citaId)
                .orElseThrow(() -> new AtencionException("Cita no encontrada."));
        if (datos.medicoId() != medicoId) {
            throw new AtencionException(ResultadoAtencion.NO_ES_DE_SU_AGENDA.name());
        }
        if (datos.estado() != EstadoCita.ASISTENCIA_CONFIRMADA) {
            throw new AtencionException(ResultadoAtencion.ESTADO_NO_PERMITE.name());
        }
        return datos;
    }

    public ResultadoAtencion registrar(long citaId, long medicoId, String medicacion) {
        if (medicacion == null || medicacion.isBlank() || medicacion.length() > MAX_MEDICACION) {
            return ResultadoAtencion.RECETA_INVALIDA;
        }

        DatosAtencionDTO datos = citas.buscarParaAtencion(citaId)
                .orElseThrow(() -> new AtencionException("Cita no encontrada."));
        if (datos.medicoId() != medicoId) {
            return ResultadoAtencion.NO_ES_DE_SU_AGENDA;
        }
        if (datos.estado() != EstadoCita.ASISTENCIA_CONFIRMADA) {
            return ResultadoAtencion.ESTADO_NO_PERMITE;
        }

        historial.agregarReceta(citaId, medicacion, reloj.ahora());
        if (finalizacion.finalizar(citaId, medicoId)) {
            return ResultadoAtencion.REGISTRADA;
        }

        historial.quitarReceta(citaId, medicacion);
        throw new AtencionException("No se pudo finalizar la cita.");
    }
}