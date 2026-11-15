package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.PrioridadClinica;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Una cirugia (CU-24, tabla quirofano.cirugia). Los nombres (paciente, cirujano, quirofano) y la fecha de
 * nacimiento solo se leen (JOIN). El quirofano queda ocupado hasta {@link #finConLimpieza()}.
 */
public record CirugiaDTO(long cirugiaId, long fichaInternaId, long pacienteId, String nombrePaciente,
                         String numeroAfiliado, LocalDate fechaNacimiento, long cirujanoId, String nombreCirujano,
                         int quirofanoId, String nombreQuirofano, TipoEspecialidad especialidad, LocalDateTime inicio,
                         int duracionMin, int limpiezaMin, Gravedad gravedad, boolean urgencia, boolean adultoMayor,
                         long ordenLlegada, EstadoCirugia estado, LocalDateTime creadaEn) {

    public LocalDateTime fin() {
        return inicio.plusMinutes(duracionMin);
    }

    public LocalDateTime finConLimpieza() {
        return fin().plusMinutes(limpiezaMin);
    }

    public PrioridadClinica prioridad() {
        return new PrioridadClinica(urgencia, gravedad, adultoMayor, ordenLlegada);
    }
}
