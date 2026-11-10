package com.Hospital.CNS.cns_backend.model.compartido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * HistorialRepository en memoria. Publicos para que los tests verifiquen:
 * {@code historiales} (pacientes con historial), {@code recetas}, y para preparar datos
 * {@code nombres} (id de paciente -> nombre) y {@code citasConocidas} (id de cita -> cita,
 * para llenar fecha, hora, especialidad y medico de cada atencion).
 */
public class FakeHistorialRepository implements HistorialRepository {

    public final Set<Long> historiales = new LinkedHashSet<>();
    public final List<RecetaDTO> recetas = new ArrayList<>();
    public final Map<Long, String> nombres = new HashMap<>();
    public final Map<Long, CitaDTO> citasConocidas = new HashMap<>();

    private long ultimaReceta = 0;

    @Override
    public synchronized void crearSiNoExiste(long pacienteId) {
        historiales.add(pacienteId);
    }

    @Override
    public synchronized Optional<HistorialDTO> porPaciente(long pacienteId) {
        if (!historiales.contains(pacienteId)) {
            return Optional.empty();
        }
        Map<Long, List<RecetaDTO>> porCita = new LinkedHashMap<>();
        for (RecetaDTO r : recetas) {
            if (r.pacienteId() == pacienteId) {
                porCita.computeIfAbsent(r.citaId(), k -> new ArrayList<>()).add(r);
            }
        }
        List<AtencionDTO> atenciones = new ArrayList<>();
        for (Map.Entry<Long, List<RecetaDTO>> e : porCita.entrySet()) {
            CitaDTO cita = citasConocidas.get(e.getKey());
            RecetaDTO primera = e.getValue().get(0);
            atenciones.add(new AtencionDTO(
                    cita != null ? cita.fecha() : primera.fechaHora().toLocalDate(),
                    cita != null ? cita.hora() : primera.fechaHora().toLocalTime(),
                    cita != null ? cita.especialidad().name() : "",
                    cita != null ? cita.nombreMedico() : "",
                    List.copyOf(e.getValue())));
        }
        return Optional.of(new HistorialDTO(pacienteId, nombres.getOrDefault(pacienteId, ""), atenciones));
    }

    /** Asigna recetaId si viene en 0 o negativo, crea el historial si no existe y devuelve la receta guardada. */
    @Override
    public synchronized RecetaDTO agregarReceta(RecetaDTO receta) {
        crearSiNoExiste(receta.pacienteId());
        long id = receta.recetaId() > 0 ? receta.recetaId() : ultimaReceta + 1;
        ultimaReceta = Math.max(ultimaReceta, id);
        RecetaDTO guardada = new RecetaDTO(id, receta.citaId(), receta.pacienteId(), receta.medicacion(),
                receta.fechaHora(), receta.numeroFicha());
        recetas.add(guardada);
        return guardada;
    }

    @Override
    public synchronized void eliminarReceta(long recetaId) {
        recetas.removeIf(r -> r.recetaId() == recetaId);
    }
}
