package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * Persistencia de las fichas internas (20 tablas + vista {@code v_todas}). Implementacion PostgreSQL:
 * PgFichaInternaRepository. Las listas vienen sin orden de prioridad: lo decide el model.
 */
public interface FichaInternaRepository {

    /** Siguiente N° de ticket (secuencia unica para las 20 tablas). */
    long nextIdentity();

    /** Inserta la ficha en la tabla de su especialidad (general o pediatrica). */
    void insertar(FichaInternaDTO ficha);

    /** Guarda medico, consultorio, fecha, hora, duracion y estado (asignar, desplazar, volver a EN_ESPERA). */
    void guardarAsignacion(FichaInternaDTO ficha);

    /** Atomico: UPDATE ... WHERE estado = desde. false si la ficha ya no estaba en {@code desde}. */
    boolean cambiarEstado(long fichaId, EstadoFichaInterna desde, EstadoFichaInterna hasta, String observaciones);

    /** Una sola derivacion por cita: false si esa cita ya tiene una ficha. */
    boolean registrarDerivacion(long citaOrigenId, long fichaId);

    Optional<FichaInternaDTO> porId(long fichaId);

    Optional<FichaInternaDTO> deCita(long citaOrigenId);

    /** EN_ESPERA o ASIGNADA en cualquiera de las 20 tablas. */
    boolean pacienteTieneFichaActiva(long pacienteId);

    /** Todas las fichas del medico ese dia (general + pediatrica), cualquier estado. */
    List<FichaInternaDTO> delMedicoEnFecha(long medicoId, LocalDate fecha);

    /** Fichas ASIGNADA de la especialidad entre esas fechas (ambas incluidas): candidatas a desplazar. */
    List<FichaInternaDTO> asignadasEntre(TipoEspecialidad especialidad, LocalDate desde, LocalDate hasta);

    List<FichaInternaDTO> enEspera(TipoEspecialidad especialidad);

    /** EN_ESPERA y ASIGNADA de una tabla (cola de prioridad). */
    List<FichaInternaDTO> activas(TipoEspecialidad especialidad, boolean pediatrica);

    List<FichaInternaDTO> delPaciente(long pacienteId);

    /** El medico derivo o atiende alguna ficha de ese paciente. */
    boolean medicoTieneFichaConPaciente(long medicoId, long pacienteId);

    /**
     * Impide que dos asignaciones de la misma especialidad se crucen (lo libera el fin de la transaccion).
     * Fuera de una transaccion no hace nada.
     */
    void bloquearEspecialidad(TipoEspecialidad especialidad);
}
