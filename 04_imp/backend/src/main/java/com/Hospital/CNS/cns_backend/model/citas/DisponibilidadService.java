package com.Hospital.CNS.cns_backend.model.citas;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;

/**
 * CU-06 (T100-02): dice si una combinacion (paciente, medico, consultorio, fecha, hora) se puede confirmar.
 *
 * <p>Orden de comprobacion (se devuelve el PRIMER motivo que falla): paciente con una cita por dia
 * &rarr; medico completo (7) &rarr; medico libre a esa hora &rarr; consultorio libre &rarr; sin solapamiento
 * &rarr; bloqueo de la agenda.</p>
 *
 * <p>Concurrencia (RNF-04): el bloqueo de la agenda se PIDE antes de leer, para que las lecturas ya se hagan con
 * la agenda tomada y nadie pueda colarse entre la validacion y el registro de la cita. Si otro proceso la tiene, se
 * siguen revisando las reglas en el orden de arriba y, si todas pasan, se responde BLOQUEADA_POR_OTRO. Quien llama
 * (T100-03) debe usar {@code validar} y guardar la cita dentro de la MISMA {@code Transaccion}: el bloqueo dura hasta
 * el commit. El UNIQUE parcial (medico, fecha, hora) de la base es el respaldo final.</p>
 *
 * <p>Todas las citas de un medico duran lo mismo (duracion fija del medico, CU-06), por eso el solapamiento con sus
 * otras citas se calcula con {@link CombinacionCandidata#duracionMin()}.</p>
 */
public class DisponibilidadService {

    /** Una cita mas es posible solo si el medico tiene MENOS de este numero ese dia (RU-07). */
    public static final int MAXIMO_CITAS_POR_DIA = 7;

    private final AgendaRepository agenda;
    private final CitaRepository citas;

    public DisponibilidadService(AgendaRepository agenda, CitaRepository citas) {
        this.agenda = Objects.requireNonNull(agenda, "agenda");
        this.citas = Objects.requireNonNull(citas, "citas");
    }

    public ResultadoDisponibilidad validar(CombinacionCandidata c) {
        Objects.requireNonNull(c, "combinacion");
        boolean agendaTomada = agenda.intentarBloquear(c.medicoId(), c.fecha());

        if (citas.pacienteTieneCitaEnFecha(c.pacienteId(), c.fecha())) {
            return ResultadoDisponibilidad.PACIENTE_YA_TIENE_CITA;
        }
        if (citas.contarDelMedicoEnFecha(c.medicoId(), c.fecha()) >= MAXIMO_CITAS_POR_DIA) {
            return ResultadoDisponibilidad.MEDICO_COMPLETO;
        }
        List<CitaDTO> ocupacion = agenda.ocupacionDelMedico(c.medicoId(), c.fecha());
        if (ocupacion.stream().anyMatch(otra -> otra.hora().equals(c.hora()))) {
            return ResultadoDisponibilidad.MEDICO_OCUPADO;
        }
        if (agenda.consultorioOcupado(c.consultorioId(), c.fecha(), c.hora(), c.fin())) {
            return ResultadoDisponibilidad.CONSULTORIO_OCUPADO;
        }
        if (ocupacion.stream().anyMatch(otra -> seCruzan(otra.hora(), c.duracionMin(), c.hora(), c.fin()))) {
            return ResultadoDisponibilidad.SOLAPADA;
        }
        if (!agendaTomada) {
            return ResultadoDisponibilidad.BLOQUEADA_POR_OTRO;
        }
        return ResultadoDisponibilidad.DISPONIBLE;
    }

    /** [inicioOtra, inicioOtra + duracion) se cruza con [inicio, fin). Terminar justo cuando empieza la otra no cruza. */
    private static boolean seCruzan(LocalTime inicioOtra, int duracion, LocalTime inicio, LocalTime fin) {
        LocalTime finOtra = inicioOtra.plusMinutes(duracion);
        return inicioOtra.isBefore(fin) && finOtra.isAfter(inicio);
    }
}
