package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalDate;
import java.util.Objects;

import com.Hospital.CNS.cns_backend.model.compartido.CancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;
import com.Hospital.CNS.cns_backend.model.compartido.SolicitudDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * CU-10 (T100-07): reprogramar = cancelar la cita PROGRAMADA (con {@link CancelacionCita}, nunca tocando el estado
 * directo) + crear una solicitud de la misma especialidad con prioridad 2 para el siguiente dia habil despues de la
 * cita (desde hoy si la cita ya paso), saltando los dias en que el paciente ya tiene otra cita o solicitud. Luego
 * la cola (CU-05) la asigna; si no hay cupo queda EN_ESPERA hasta 15 dias habiles [3a].
 *
 * <p>La confirmacion previa [2a] la pide la pantalla. El dia se busca ANTES de cancelar: si no hay dia posible
 * en la ventana, la cita se conserva (SIN_DIA_DISPONIBLE). Cancelar y crear la solicitud van en una transaccion.</p>
 */
public class ReprogramacionService {

    private final CitaRepository citas;
    private final CancelacionCita cancelacion;
    private final SolicitudService solicitudes;
    private final CalendarioHabil calendario;
    private final Reloj reloj;
    private final Transaccion transaccion;

    public ReprogramacionService(CitaRepository c, CancelacionCita cancelacion, SolicitudService solicitudes,
                                 CalendarioHabil cal, Reloj reloj) {
        this(c, cancelacion, solicitudes, cal, reloj, Transaccion.DIRECTA);
    }

    public ReprogramacionService(CitaRepository c, CancelacionCita cancelacion, SolicitudService solicitudes,
                                 CalendarioHabil cal, Reloj reloj, Transaccion transaccion) {
        this.citas = Objects.requireNonNull(c, "citas");
        this.cancelacion = Objects.requireNonNull(cancelacion, "cancelacion");
        this.solicitudes = Objects.requireNonNull(solicitudes, "solicitudes");
        this.calendario = Objects.requireNonNull(cal, "calendario");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
        this.transaccion = Objects.requireNonNull(transaccion, "transaccion");
    }

    /** Errores: NO_ENCONTRADA, NO_ES_DEL_PACIENTE, ESTADO_NO_PERMITE [1a], SIN_DIA_DISPONIBLE. */
    public SolicitudDTO reprogramar(long citaId, long pacienteId) {
        CitaDTO cita = citas.porId(citaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe esa cita"));
        if (cita.pacienteId() != pacienteId) {
            throw new DominioException("NO_ES_DEL_PACIENTE", "La cita no pertenece al paciente autenticado");
        }
        if (cita.estado() != EstadoCita.PROGRAMADA) {
            throw estadoNoPermite();
        }
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate base = cita.fecha().isBefore(hoy) ? hoy : cita.fecha();
        LocalDate dia = solicitudes.primerDiaReprogramable(pacienteId, calendario.siguienteDiaHabil(base))
                .orElseThrow(() -> new DominioException("SIN_DIA_DISPONIBLE",
                        "No hay un dia habil libre en los proximos 15 dias habiles para reprogramar; "
                                + "su cita se conserva"));
        return transaccion.ejecutar(() -> {
            ResultadoCancelacion resultado = cancelacion.cancelarPorPaciente(citaId, pacienteId);
            switch (resultado) {
                case CANCELADA -> {
                    // sigue: crea la nueva solicitud
                }
                case NO_ENCONTRADA -> throw new DominioException("NO_ENCONTRADA", "No existe esa cita");
                case NO_ES_DEL_PACIENTE ->
                    throw new DominioException("NO_ES_DEL_PACIENTE", "La cita no pertenece al paciente autenticado");
                case ESTADO_NO_PERMITE -> throw estadoNoPermite();
            }
            return solicitudes.solicitarReprogramada(pacienteId, cita.especialidad(), dia);
        });
    }

    private static DominioException estadoNoPermite() {
        return new DominioException("ESTADO_NO_PERMITE", "Solo se puede reprogramar una cita PROGRAMADA");
    }
}
