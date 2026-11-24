package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** Cirugias (tabla quirofano.cirugia, migracion V8). Implementacion: PgCirugiaRepository. */
public interface CirugiaRepository {

    long nextIdentity();

    /** false si la base la rechaza por choque (EXCLUDE de quirofano, cirujano o paciente). */
    boolean insertar(CirugiaDTO cirugia);

    /** Mueve una cirugia PROGRAMADA. false si el nuevo horario choca o si ya no estaba PROGRAMADA. */
    boolean reprogramar(long cirugiaId, int quirofanoId, LocalDateTime nuevoInicio);

    Optional<CirugiaDTO> porId(long cirugiaId);

    /** Atomico: UPDATE ... WHERE estado = desde. */
    boolean cambiarEstado(long cirugiaId, EstadoCirugia desde, EstadoCirugia hasta);

    /** Cirugias que no estan CANCELADA y tocan ese dia (por quirofanos de la especialidad, cirujano o paciente). */
    List<CirugiaDTO> deEspecialidadEnFecha(TipoEspecialidad especialidad, LocalDate fecha);

    List<CirugiaDTO> delCirujanoEnFecha(long cirujanoId, LocalDate fecha);

    List<CirugiaDTO> delPaciente(long pacienteId);

    /** PROGRAMADA de la especialidad con inicio entre esas horas: candidatas a desplazar. */
    List<CirugiaDTO> programadasEntre(TipoEspecialidad especialidad, LocalDateTime desde, LocalDateTime hasta);

    /** PROGRAMADA de ese quirofano con inicio desde {@code desde}. */
    int contarProgramadasDesde(int quirofanoId, LocalDateTime desde);
}
