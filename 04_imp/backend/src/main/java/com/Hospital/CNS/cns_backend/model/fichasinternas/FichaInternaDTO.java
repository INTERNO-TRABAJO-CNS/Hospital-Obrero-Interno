package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.PrioridadClinica;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Una ficha interna (CU-21/CU-22). Vive en la tabla {@code fichas_internas.<especialidad>[_pediatria]}; el
 * {@code fichaId} es el N° de ticket y es unico en las 20 tablas. Los campos de nombre (paciente, medicos,
 * consultorio) y la fecha de nacimiento solo se leen (JOIN); {@code store} guarda las columnas de la tabla.
 * Mientras esta EN_ESPERA no tiene medico, consultorio, fecha ni hora.
 */
public record FichaInternaDTO(long fichaId, TipoEspecialidad especialidad, boolean pediatrica,
                              long pacienteId, String nombrePaciente, String numeroAfiliado, LocalDate fechaNacimiento,
                              Long citaOrigenId, long medicoDerivanteId, String nombreDerivante,
                              TipoEspecialidad especialidadDerivante,
                              Long medicoId, String nombreMedico, Integer consultorioId, String nombreConsultorio,
                              LocalDate fecha, LocalTime hora, int duracionMin,
                              Gravedad gravedad, boolean urgencia, boolean adultoMayor, long ordenLlegada,
                              String motivo, String observaciones, EstadoFichaInterna estado, LocalDateTime creadaEn) {

    public PrioridadClinica prioridad() {
        return new PrioridadClinica(urgencia, gravedad, adultoMayor, ordenLlegada);
    }

    public boolean activa() {
        return estado == EstadoFichaInterna.EN_ESPERA || estado == EstadoFichaInterna.ASIGNADA;
    }

    /** null mientras esta EN_ESPERA. */
    public LocalDateTime inicio() {
        return fecha == null ? null : LocalDateTime.of(fecha, hora);
    }

    public LocalDateTime fin() {
        return fecha == null ? null : inicio().plusMinutes(duracionMin);
    }

    /** La misma ficha ASIGNADA a ese medico, consultorio, dia y hora. */
    public FichaInternaDTO asignada(long medico, String nombre, Integer consultorio, String consultorioNombre,
                                    LocalDate dia, LocalTime horaInicio, int duracion) {
        return new FichaInternaDTO(fichaId, especialidad, pediatrica, pacienteId, nombrePaciente, numeroAfiliado,
                fechaNacimiento, citaOrigenId, medicoDerivanteId, nombreDerivante, especialidadDerivante, medico, nombre,
                consultorio, consultorioNombre, dia, horaInicio, duracion, gravedad, urgencia, adultoMayor,
                ordenLlegada, motivo, observaciones, EstadoFichaInterna.ASIGNADA, creadaEn);
    }

    /** La misma ficha EN_ESPERA (sin medico ni horario). */
    public FichaInternaDTO enEspera() {
        return new FichaInternaDTO(fichaId, especialidad, pediatrica, pacienteId, nombrePaciente, numeroAfiliado,
                fechaNacimiento, citaOrigenId, medicoDerivanteId, nombreDerivante, especialidadDerivante, null, null,
                null, null, null, null, duracionMin, gravedad, urgencia, adultoMayor, ordenLlegada, motivo,
                observaciones, EstadoFichaInterna.EN_ESPERA, creadaEn);
    }

    public FichaInternaDTO conEstado(EstadoFichaInterna nuevo, String nuevasObservaciones) {
        return new FichaInternaDTO(fichaId, especialidad, pediatrica, pacienteId, nombrePaciente, numeroAfiliado,
                fechaNacimiento, citaOrigenId, medicoDerivanteId, nombreDerivante, especialidadDerivante, medicoId,
                nombreMedico, consultorioId, nombreConsultorio, fecha, hora, duracionMin, gravedad, urgencia,
                adultoMayor, ordenLlegada, motivo, nuevasObservaciones, nuevo, creadaEn);
    }
}
