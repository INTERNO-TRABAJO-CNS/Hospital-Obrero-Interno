package com.Hospital.CNS.cns_backend.model.citas;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * CU-08 (T100-07): el paciente ve SOLO sus citas (RNF-01), de la mas reciente a la mas antigua, y el detalle de
 * una de ellas. [2a] sin citas: lista vacia. [4a] cita ajena: NO_ES_DEL_PACIENTE.
 */
public class ConsultaCitasPacienteService {

    private static final Comparator<CitaDTO> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(CitaDTO::fecha).thenComparing(CitaDTO::hora).reversed();

    private final CitaRepository citas;

    public ConsultaCitasPacienteService(CitaRepository citas) {
        this.citas = Objects.requireNonNull(citas, "citas");
    }

    public List<CitaDTO> misCitas(long pacienteId) {
        validarId(pacienteId);
        return citas.delPaciente(pacienteId).stream()
                .filter(c -> c.pacienteId() == pacienteId)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    public CitaDTO detalle(long citaId, long pacienteId) {
        validarId(pacienteId);
        CitaDTO cita = citas.porId(citaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "No existe esa cita"));
        if (cita.pacienteId() != pacienteId) {
            throw new DominioException("NO_ES_DEL_PACIENTE", "La cita no pertenece al paciente autenticado");
        }
        return cita;
    }

    private static void validarId(long pacienteId) {
        if (pacienteId <= 0) {
            throw new DominioException("DATOS_INVALIDOS", "El identificador del paciente no es valido");
        }
    }
}
