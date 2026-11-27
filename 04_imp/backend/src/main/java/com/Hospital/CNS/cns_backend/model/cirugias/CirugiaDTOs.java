package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.LocalDate;
import java.time.LocalTime;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Datos que viajan entre la app y el backend para CU-24 (programar cirugia). */
public final class CirugiaDTOs {

    private CirugiaDTOs() {
    }

    /** "Proponer horario": la ficha ATENDIDA, la duracion estimada, la gravedad y si es urgente. */
    public record Solicitud(long fichaTicket, int duracionMin, Gravedad gravedad, boolean urgente) {
    }

    /** "Confirmar": la solicitud mas la propuesta que vio el especialista. */
    public record Confirmacion(long fichaTicket, int duracionMin, Gravedad gravedad, boolean urgente, int quirofanoId,
                               LocalDate fecha, LocalTime inicio, Long desplazaNumero, LocalDate desplazaFecha,
                               LocalTime desplazaInicio) {

        public Solicitud solicitud() {
            return new Solicitud(fichaTicket, duracionMin, gravedad, urgente);
        }
    }

    /** Cirugia como la ve la app ({@code inicio}, {@code fin}, {@code limpiezaHasta} en "HH:mm"). */
    public record Vista(long numero, long fichaTicket, long pacienteId, String nombrePaciente, String numeroAfiliado,
                        int edad, TipoEspecialidad especialidad, String quirofano, String cirujano, LocalDate fecha,
                        String inicio, String fin, String limpiezaHasta, int duracionMin, boolean urgente,
                        Gravedad gravedad, EstadoCirugia estado) {
    }

    /** Cirugia no urgente que la propuesta desplazaria, con su nuevo horario. */
    public record Desplazamiento(Vista cirugia, LocalDate nuevaFecha, String nuevoInicio) {
    }

    /** Lo que calcula "Proponer horario" (no guarda nada). {@code desplaza} es null si no desplaza a nadie. */
    public record Propuesta(int quirofanoId, String quirofano, TipoEspecialidad especialidad, LocalDate fecha,
                            String inicio, String fin, String limpiezaHasta, int limpiezaMin, Desplazamiento desplaza) {
    }
}
