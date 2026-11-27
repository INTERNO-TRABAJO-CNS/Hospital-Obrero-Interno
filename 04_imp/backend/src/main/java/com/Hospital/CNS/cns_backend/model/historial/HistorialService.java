package com.Hospital.CNS.cns_backend.model.historial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.AtencionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialDTO;
import com.Hospital.CNS.cns_backend.model.compartido.HistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteRepository;

/**
 * CU-14: el medico consulta el historial de un paciente de su agenda. Solo lectura.
 *
 * <ul>
 *   <li>[1a] el paciente no tiene cita (no CANCELADA) con ese medico -> PACIENTE_FUERA_DE_AGENDA (403).</li>
 *   <li>[3a] solo se devuelven datos permitidos al MEDICO ({@link HistorialPacienteDTO} no lleva CI ni
 *       contacto). Que mas ve un especialista con ficha interna: {@code // PENDIENTE PV-22}.</li>
 * </ul>
 *
 * <p>Cada atencion se completa con la fecha, hora, especialidad y medico de su cita, y las citas
 * pasadas son las de fecha y hora anteriores a ahora segun el {@link Reloj}.</p>
 */
public class HistorialService {

    private static final Comparator<AtencionDTO> ATENCION_MAS_RECIENTE =
            Comparator.comparing(AtencionDTO::fecha).thenComparing(AtencionDTO::hora).reversed();

    private final HistorialRepository historiales;
    private final CitaRepository citas;
    private final Reloj reloj;
    private final PacienteRepository pacientes;

    public HistorialService(HistorialRepository h, CitaRepository c) {
        this(h, c, new RelojSistema());
    }

    /** Igual que el anterior pero con reloj propio (las pruebas usan RelojFijo). */
    public HistorialService(HistorialRepository h, CitaRepository c, Reloj reloj) {
        this(h, c, reloj, null);
    }

    /**
     * @param pacientes puede ser null; con el se muestran afiliacion, titular y edad, y funciona el buscador por
     *                  numero de afiliado (Figma del historial)
     */
    public HistorialService(HistorialRepository h, CitaRepository c, Reloj reloj, PacienteRepository pacientes) {
        this.historiales = h;
        this.citas = c;
        this.reloj = reloj;
        this.pacientes = pacientes;
    }

    /**
     * Buscador del historial: pacientes de esa afiliacion que tienen (o tuvieron) cita no cancelada con el medico.
     * Nunca devuelve pacientes ajenos a su agenda ([1a]).
     */
    public List<PacienteDeAgendaDTO> buscarEnAgenda(long medicoId, String numeroAfiliado) {
        if (pacientes == null || numeroAfiliado == null || numeroAfiliado.isBlank()) {
            return List.of();
        }
        LocalDate hoy = reloj.ahora().toLocalDate();
        return pacientes.deLaAfiliacion(numeroAfiliado.trim()).stream()
                .filter(p -> citas.medicoTienePacienteEnAgenda(medicoId, p.pacienteId()))
                .map(p -> new PacienteDeAgendaDTO(p.pacienteId(), p.nombreCompleto(), p.numeroAfiliado(), p.titular(),
                        Period.between(p.fechaNacimiento(), hoy).getYears()))
                .toList();
    }

    public HistorialPacienteDTO verHistorial(long medicoId, long pacienteId) {
        if (!citas.medicoTienePacienteEnAgenda(medicoId, pacienteId)) {
            throw new DominioException("PACIENTE_FUERA_DE_AGENDA",
                    "Solo puede ver el historial de pacientes que tienen cita en su agenda");
        }
        List<CitaDTO> delPaciente = citas.delPaciente(pacienteId);
        Optional<HistorialDTO> historial = historiales.porPaciente(pacienteId);

        LocalDateTime ahora = reloj.ahora();
        List<CitaPasadaDTO> pasadas = delPaciente.stream()
                .filter(c -> LocalDateTime.of(c.fecha(), c.hora()).isBefore(ahora))
                .sorted(Comparator.comparing(CitaDTO::fecha).thenComparing(CitaDTO::hora).reversed())
                .map(c -> new CitaPasadaDTO(c.citaId(), c.fecha(), c.hora(), c.especialidad(), c.nombreMedico(),
                        c.numeroFicha(), c.estado()))
                .toList();

        List<AtencionDTO> atenciones = historial.map(HistorialDTO::atenciones).orElse(List.of()).stream()
                .map(this::completarConSuCita)
                .sorted(ATENCION_MAS_RECIENTE)
                .toList();

        Optional<PacienteDTO> paciente = pacientes == null ? Optional.empty() : pacientes.porId(pacienteId);
        return new HistorialPacienteDTO(pacienteId, nombre(historial, delPaciente), pasadas, atenciones,
                paciente.map(PacienteDTO::numeroAfiliado).orElse(null),
                paciente.map(PacienteDTO::titular).orElse(null),
                paciente.map(p -> Period.between(p.fechaNacimiento(), ahora.toLocalDate()).getYears()).orElse(null));
    }

    /** La BD guarda la receta con su cita; aqui se agregan fecha, hora, especialidad y medico de esa cita. */
    private AtencionDTO completarConSuCita(AtencionDTO a) {
        if (a.recetas() == null || a.recetas().isEmpty()) {
            return a;
        }
        RecetaDTO primera = a.recetas().get(0);
        return citas.porId(primera.citaId())
                .map(c -> new AtencionDTO(c.fecha(), c.hora(), c.especialidad().name(), c.nombreMedico(), a.recetas()))
                .orElse(a);
    }

    private static String nombre(Optional<HistorialDTO> historial, List<CitaDTO> delPaciente) {
        String delHistorial = historial.map(HistorialDTO::nombrePaciente).orElse("");
        if (delHistorial != null && !delHistorial.isBlank()) {
            return delHistorial;
        }
        return delPaciente.isEmpty() ? "" : delPaciente.get(0).nombrePaciente();
    }
}
