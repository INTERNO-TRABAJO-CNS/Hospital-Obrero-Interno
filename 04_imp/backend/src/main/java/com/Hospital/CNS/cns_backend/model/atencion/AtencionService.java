package com.Hospital.CNS.cns_backend.model.atencion;

import java.time.Period;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;

/**
 * CU-15: el medico registra la receta y el fin de la atencion.
 *
 * <ul>
 *   <li>[1a] la cita no es de su agenda o no esta en ASISTENCIA_CONFIRMADA -> no se registra nada.</li>
 *   <li>[2a] receta vacia o de mas de 2000 caracteres -> RECETA_INVALIDA.</li>
 * </ul>
 *
 * <p>Orden: guardar la receta y luego {@link FinalizacionCita#finalizar} (CU-12 guarda la hora de fin y
 * pasa la cita a ATENDIDA). Si finalizar falla, la receta se quita (compensacion): nunca queda una
 * receta de una cita sin finalizar. El medico no registra el inicio, solo el fin.</p>
 */
public class AtencionService {

    public static final int MAX_RECETA = 2000;

    private final CitaRepository citas;
    private final HistorialRepository historiales;
    private final FinalizacionCita finalizacion;
    private final Reloj reloj;
    private final Transaccion transaccion;
    private final PacienteRepository pacientes;
    private final ConsultorioRepository consultorios;

    public AtencionService(CitaRepository c, HistorialRepository h, FinalizacionCita fin, Reloj reloj) {
        this(c, h, fin, reloj, Transaccion.DIRECTA, null, null);
    }

    public AtencionService(CitaRepository c, HistorialRepository h, FinalizacionCita fin, Reloj reloj,
                           Transaccion transaccion) {
        this(c, h, fin, reloj, transaccion, null, null);
    }

    /**
     * Con {@link Transaccion} real, receta + fin (y su compensacion) se guardan juntos: si algo lanza una
     * excepcion no queda nada a medias, y nadie ve la receta antes de que la cita quede ATENDIDA.
     *
     * @param pacientes    puede ser null; sirve para mostrar edad y si es titular (Figma de la atencion)
     * @param consultorios puede ser null; sirve para mostrar el nombre del consultorio y no su id
     */
    public AtencionService(CitaRepository c, HistorialRepository h, FinalizacionCita fin, Reloj reloj,
                           Transaccion transaccion, PacienteRepository pacientes, ConsultorioRepository consultorios) {
        this.citas = c;
        this.historiales = h;
        this.finalizacion = fin;
        this.reloj = reloj;
        this.transaccion = transaccion;
        this.pacientes = pacientes;
        this.consultorios = consultorios;
    }

    /** Datos autocompletados. Errores como DominioException: NO_ENCONTRADA, NO_ES_DE_SU_AGENDA, ESTADO_NO_PERMITE. */
    public DatosAtencionDTO datosParaAtencion(long citaId, long medicoId) {
        CitaDTO cita = citas.porId(citaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "La cita no existe"));
        ResultadoAtencion permiso = permiso(cita, medicoId);
        if (permiso != null) {
            throw new DominioException(permiso.name(), mensaje(permiso));
        }
        Optional<PacienteDTO> paciente = pacientes == null ? Optional.empty() : pacientes.porId(cita.pacienteId());
        Integer edad = paciente.map(p -> Period.between(p.fechaNacimiento(), reloj.ahora().toLocalDate()).getYears())
                .orElse(null);
        Boolean titular = paciente.map(PacienteDTO::titular).orElse(null);
        String nombreConsultorio = consultorios == null ? null
                : consultorios.porId(cita.consultorio()).map(ConsultorioDTO::nombre).orElse(null);
        return new DatosAtencionDTO(cita.citaId(), cita.pacienteId(), cita.nombrePaciente(), cita.numeroAfiliado(),
                cita.numeroFicha(), cita.fecha(), cita.hora(), cita.especialidad(), cita.especialidad().categoria(),
                cita.consultorio(), cita.nombreMedico(), cita.especialidad().puedeDerivar(), cita.estado(), edad,
                titular, nombreConsultorio);
    }

    public ResultadoAtencion registrar(long citaId, long medicoId, String medicacion) {
        Optional<CitaDTO> encontrada = citas.porId(citaId);
        if (encontrada.isEmpty()) {
            return ResultadoAtencion.NO_ENCONTRADA;
        }
        CitaDTO cita = encontrada.get();
        ResultadoAtencion permiso = permiso(cita, medicoId);
        if (permiso != null) {
            return permiso;
        }
        if (!recetaValida(medicacion)) {
            return ResultadoAtencion.RECETA_INVALIDA;
        }
        return transaccion.ejecutar(() -> guardarRecetaYFinalizar(cita, medicoId, medicacion.strip()));
    }

    private ResultadoAtencion guardarRecetaYFinalizar(CitaDTO cita, long medicoId, String medicacion) {
        RecetaDTO guardada = historiales.agregarReceta(new RecetaDTO(0, cita.citaId(), cita.pacienteId(),
                medicacion, reloj.ahora(), cita.numeroFicha()));
        ResultadoFinalizacion fin;
        try {
            fin = finalizacion.finalizar(cita.citaId(), medicoId);
        } catch (RuntimeException e) {
            historiales.eliminarReceta(guardada.recetaId());
            throw e;
        }
        if (fin != ResultadoFinalizacion.FINALIZADA) {
            historiales.eliminarReceta(guardada.recetaId());
            return switch (fin) {
                case NO_ENCONTRADA -> ResultadoAtencion.NO_ENCONTRADA;
                case NO_ES_DE_SU_AGENDA -> ResultadoAtencion.NO_ES_DE_SU_AGENDA;
                default -> ResultadoAtencion.ESTADO_NO_PERMITE;
            };
        }
        return ResultadoAtencion.REGISTRADA;
    }

    /** Mensaje para el usuario de cada resultado que no es REGISTRADA. */
    public static String mensaje(ResultadoAtencion r) {
        return switch (r) {
            case REGISTRADA -> "Atencion registrada";
            case RECETA_INVALIDA -> "La receta no puede estar vacia ni pasar de " + MAX_RECETA + " caracteres";
            case NO_ES_DE_SU_AGENDA -> "La cita no es de su agenda";
            case ESTADO_NO_PERMITE -> "La cita debe tener la asistencia confirmada";
            case NO_ENCONTRADA -> "La cita no existe";
        };
    }

    /** [1a]: null si el medico puede atender la cita. */
    private static ResultadoAtencion permiso(CitaDTO cita, long medicoId) {
        if (cita.medicoId() != medicoId) {
            return ResultadoAtencion.NO_ES_DE_SU_AGENDA;
        }
        if (cita.estado() != EstadoCita.ASISTENCIA_CONFIRMADA) {
            return ResultadoAtencion.ESTADO_NO_PERMITE;
        }
        return null;
    }

    private static boolean recetaValida(String medicacion) {
        return medicacion != null && !medicacion.isBlank() && medicacion.strip().length() <= MAX_RECETA;
    }
}
