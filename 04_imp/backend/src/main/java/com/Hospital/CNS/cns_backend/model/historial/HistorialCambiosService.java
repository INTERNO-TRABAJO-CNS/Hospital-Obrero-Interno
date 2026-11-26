package com.Hospital.CNS.cns_backend.model.historial;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * CU-18 (T100-13): quien cambio que cita, cuando y por que. Solo lectura: los registros se agregan desde los otros
 * casos de uso ({@code CambioCitaRepository.registrar}) y nunca se modifican ni se borran. El control de rol
 * (ENFERMERIA y ADMINISTRADOR) lo hace el controller.
 *
 * <p>Filtros opcionales: cita, usuario (contiene, sin distinguir mayusculas) y rango de fechas (incluye ambos
 * extremos). Resultado del mas nuevo al mas viejo.</p>
 */
public class HistorialCambiosService {

    private static final Comparator<CambioCitaDTO> MAS_NUEVO_PRIMERO =
            Comparator.comparing(CambioCitaDTO::fechaHora).thenComparingLong(CambioCitaDTO::cambioId).reversed();

    private final CambioCitaRepository cambios;
    private final CitaRepository citas;

    public HistorialCambiosService(CambioCitaRepository r) {
        this(r, null);
    }

    /** Con {@code citas} se completa cada fila con la ficha, el medico y el paciente. */
    public HistorialCambiosService(CambioCitaRepository r, CitaRepository citas) {
        this.cambios = Objects.requireNonNull(r, "cambios");
        this.citas = citas;
    }

    public List<CambioCitaDTO> consultar(Long citaId, String usuario, LocalDate desde, LocalDate hasta) {
        if (citaId != null && citaId <= 0) {
            throw new DominioException("DATOS_INVALIDOS", "El numero de cita no es valido");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DominioException("DATOS_INVALIDOS", "La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        String usuarioBuscado = usuario == null || usuario.isBlank() ? null : usuario.trim();
        return cambios.buscar(citaId, usuarioBuscado, desde, hasta).stream()
                .sorted(MAS_NUEVO_PRIMERO)
                .toList();
    }

    /** Igual que {@link #consultar}, con los datos de la cita en cada fila (pantalla del Figma). */
    public List<CambioCitaVistaDTO> consultarConCita(Long citaId, String usuario, LocalDate desde, LocalDate hasta) {
        Map<Long, Optional<CitaDTO>> cache = new HashMap<>();
        return consultar(citaId, usuario, desde, hasta).stream()
                .map(c -> vista(c, cache.computeIfAbsent(c.citaId(), this::buscarCita)))
                .toList();
    }

    private Optional<CitaDTO> buscarCita(long citaId) {
        return citas == null ? Optional.empty() : citas.porId(citaId);
    }

    private static CambioCitaVistaDTO vista(CambioCitaDTO c, Optional<CitaDTO> cita) {
        return new CambioCitaVistaDTO(c.cambioId(), c.citaId(),
                cita.map(CitaDTO::numeroFicha).orElse(0),
                cita.map(CitaDTO::nombreMedico).orElse(""),
                cita.map(CitaDTO::nombrePaciente).orElse(""),
                cita.map(CitaDTO::fecha).orElse(null),
                c.accion(), c.usuario(), c.fechaHora(), c.motivo());
    }
}
