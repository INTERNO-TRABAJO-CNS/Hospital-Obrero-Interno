package com.Hospital.CNS.cns_backend.model.fichasinternas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.ComparadorPrioridad;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * CU-22: asigna medico, dia y hora a una ficha interna segun la prioridad (T100-20). El sistema calcula y
 * guarda; nunca borra una ficha.
 *
 * <ul>
 *   <li><b>Normal:</b> dias habiles desde manana, hasta 15. Cada dia, primero el especialista con menos fichas
 *       (empate: menor id), franjas de su horario con la duracion de su ficha. Exige cupo (general + pediatrica),
 *       medico libre y paciente libre (cualquier cruce con citas comunes o fichas cuenta).</li>
 *   <li><b>Sin hueco:</b> si hay una ficha ASIGNADA de menor prioridad (no urgente y que no empezo), la nueva toma
 *       su lugar y la desplazada pasa a EN_ESPERA. Si no, la nueva queda EN_ESPERA.</li>
 *   <li><b>Urgencia:</b> hoy desde ahora. Sin hueco, desplaza a la de hoy de menor prioridad que no empezo; la
 *       desplazada busca el siguiente hueco libre (sin desplazar a nadie) y, si no lo hay, queda EN_ESPERA.</li>
 *   <li><b>Terapia Intensiva:</b> sin agenda ni cupo: ya, con el medico de TI que se libere antes.</li>
 * </ul>
 * Lo llama el Service dentro de su transaccion, con la especialidad bloqueada.
 */
public class AsignadorFichaInterna {

    public static final int DIAS_HABILES_MAXIMOS = 15;

    /** Como termino la asignacion. */
    public enum Tipo { ASIGNADA, ASIGNADA_DESPLAZANDO, EN_ESPERA }

    /** {@code desplazada} es la ficha que perdio su horario (ya guardada en su nuevo estado), o null. */
    public record Resultado(Tipo tipo, FichaInternaDTO ficha, FichaInternaDTO desplazada) {
    }

    private record Intervalo(LocalDateTime inicio, LocalDateTime fin) {
        boolean cruza(LocalDateTime otroInicio, LocalDateTime otroFin) {
            return inicio.isBefore(otroFin) && otroInicio.isBefore(fin);
        }
    }

    private record Hueco(MedicoDTO medico, Integer consultorioId, LocalDate dia, LocalTime hora) {
    }

    private final FichaInternaRepository fichas;
    private final MedicoRepository medicos;
    private final HorarioRepository horarios;
    private final ConsultorioRepository consultorios;
    private final CitaRepository citas;
    private final DiaNoHabilRepository diasNoHabiles;
    private final OcupacionExtra extra;
    private final Reloj reloj;

    /**
     * Otras ocupaciones del paciente o del medico que no son citas ni fichas (las cirugias, T100-23).
     * Devuelve los intervalos [inicio, fin) de ese dia.
     */
    public interface OcupacionExtra {
        OcupacionExtra NINGUNA = new OcupacionExtra() {
            @Override
            public List<LocalDateTime[]> delPaciente(long pacienteId, LocalDate dia) {
                return List.of();
            }

            @Override
            public List<LocalDateTime[]> delMedico(long medicoId, LocalDate dia) {
                return List.of();
            }
        };

        List<LocalDateTime[]> delPaciente(long pacienteId, LocalDate dia);

        List<LocalDateTime[]> delMedico(long medicoId, LocalDate dia);
    }

    public AsignadorFichaInterna(FichaInternaRepository fichas, MedicoRepository medicos, HorarioRepository horarios,
                                 ConsultorioRepository consultorios, CitaRepository citas,
                                 DiaNoHabilRepository diasNoHabiles, OcupacionExtra extra, Reloj reloj) {
        this.fichas = fichas;
        this.medicos = medicos;
        this.horarios = horarios;
        this.consultorios = consultorios;
        this.citas = citas;
        this.diasNoHabiles = diasNoHabiles;
        this.extra = extra == null ? OcupacionExtra.NINGUNA : extra;
        this.reloj = reloj;
    }

    /** Asigna una ficha que esta EN_ESPERA (recien creada). Guarda el resultado. */
    public Resultado asignar(FichaInternaDTO ficha) {
        if (ficha.especialidad().soloUrgencia()) {
            return asignarTerapiaIntensiva(ficha);
        }
        LocalDateTime ahora = reloj.ahora();
        if (ficha.urgencia()) {
            Optional<Hueco> hoy = buscarHueco(ficha, ahora.toLocalDate(), desdeAhora(ahora), Set.of());
            if (hoy.isPresent()) {
                return new Resultado(Tipo.ASIGNADA, guardar(ficha, hoy.get()), null);
            }
            return desplazar(ficha, ahora.toLocalDate(), ahora.toLocalDate(), true)
                    .orElseGet(() -> new Resultado(Tipo.EN_ESPERA, ficha, null));
        }
        Optional<Hueco> hueco = buscarEnDiasHabiles(ficha, ahora.toLocalDate(), Set.of());
        if (hueco.isPresent()) {
            return new Resultado(Tipo.ASIGNADA, guardar(ficha, hueco.get()), null);
        }
        LocalDate desde = siguienteHabil(ahora.toLocalDate());
        LocalDate hasta = desde;
        for (int i = 1; i < DIAS_HABILES_MAXIMOS; i++) {
            hasta = siguienteHabil(hasta);
        }
        return desplazar(ficha, desde, hasta, false).orElseGet(() -> new Resultado(Tipo.EN_ESPERA, ficha, null));
    }

    /**
     * Fichas EN_ESPERA de todas las especialidades (salvo Terapia Intensiva), en orden de prioridad, buscan un
     * hueco libre SIN desplazar a nadie. Devuelve las que se asignaron (ya guardadas).
     */
    public List<FichaInternaDTO> procesarEnEspera() {
        List<FichaInternaDTO> asignadas = new ArrayList<>();
        LocalDate hoy = reloj.ahora().toLocalDate();
        for (TipoEspecialidad esp : TipoEspecialidad.values()) {
            if (!esp.esInterna() || esp.soloUrgencia()) {
                continue;
            }
            fichas.bloquearEspecialidad(esp);
            List<FichaInternaDTO> espera = new ArrayList<>(fichas.enEspera(esp));
            espera.sort(Comparator.comparing(FichaInternaDTO::prioridad, ComparadorPrioridad.INSTANCIA));
            for (FichaInternaDTO f : espera) {
                buscarEnDiasHabiles(f, hoy, Set.of()).ifPresent(h -> asignadas.add(guardar(f, h)));
            }
        }
        return asignadas;
    }

    /** Busca desde el dia habil siguiente a {@code desdeDia}, hasta 15 dias habiles. */
    private Optional<Hueco> buscarEnDiasHabiles(FichaInternaDTO ficha, LocalDate desdeDia, Set<Long> ignorar) {
        LocalDate dia = desdeDia;
        for (int i = 0; i < DIAS_HABILES_MAXIMOS; i++) {
            dia = siguienteHabil(dia);
            Optional<Hueco> hueco = buscarHueco(ficha, dia, null, ignorar);
            if (hueco.isPresent()) {
                return hueco;
            }
        }
        return Optional.empty();
    }

    /**
     * Desplaza a la ficha ASIGNADA de menor prioridad de la especialidad entre {@code desde} y {@code hasta} que
     * no sea urgente ni haya empezado, si la nueva tiene mayor prioridad y su paciente esta libre a esa hora.
     */
    private Optional<Resultado> desplazar(FichaInternaDTO nueva, LocalDate desde, LocalDate hasta, boolean urgencia) {
        LocalDateTime ahora = reloj.ahora();
        List<FichaInternaDTO> candidatas = new ArrayList<>();
        for (FichaInternaDTO f : fichas.asignadasEntre(nueva.especialidad(), desde, hasta)) {
            boolean menorPrioridad = ComparadorPrioridad.INSTANCIA.compare(nueva.prioridad(), f.prioridad()) < 0;
            if (f.estado() == EstadoFichaInterna.ASIGNADA && !f.urgencia() && f.pediatrica() == nueva.pediatrica()
                    && f.inicio().isAfter(ahora) && f.pacienteId() != nueva.pacienteId() && menorPrioridad) {
                candidatas.add(f);
            }
        }
        // La de menor prioridad primero.
        candidatas.sort(Comparator.comparing(FichaInternaDTO::prioridad, ComparadorPrioridad.INSTANCIA).reversed());
        for (FichaInternaDTO victima : candidatas) {
            List<Intervalo> paciente = ocupacionPaciente(nueva.pacienteId(), victima.fecha(), Set.of(victima.fichaId()));
            if (libre(paciente, victima.inicio(), victima.fin())) {
                MedicoDTO medico = medicos.porId(victima.medicoId()).orElseThrow();
                FichaInternaDTO tomada = nueva.asignada(medico.medicoId(), nombre(medico), victima.consultorioId(),
                        victima.nombreConsultorio(), victima.fecha(), victima.hora(), victima.duracionMin());
                fichas.guardarAsignacion(tomada);
                FichaInternaDTO desplazada = victima.enEspera();
                if (urgencia) {
                    LocalDate hoy = ahora.toLocalDate();
                    Optional<Hueco> otro = buscarHueco(desplazada, hoy, desdeAhora(ahora), Set.of());
                    if (otro.isEmpty()) {
                        otro = buscarEnDiasHabiles(desplazada, hoy, Set.of());
                    }
                    if (otro.isPresent()) {
                        return Optional.of(new Resultado(Tipo.ASIGNADA_DESPLAZANDO, tomada, guardar(desplazada, otro.get())));
                    }
                }
                fichas.guardarAsignacion(desplazada);
                return Optional.of(new Resultado(Tipo.ASIGNADA_DESPLAZANDO, tomada, desplazada));
            }
        }
        return Optional.empty();
    }

    private Resultado asignarTerapiaIntensiva(FichaInternaDTO ficha) {
        LocalDateTime ahora = reloj.ahora().truncatedTo(ChronoUnit.MINUTES);
        MedicoDTO elegido = null;
        LocalDateTime mejor = null;
        for (MedicoDTO m : activos(TipoEspecialidad.TERAPIA_INTENSIVA)) {
            LocalDateTime libre = ahora;
            List<FichaInternaDTO> deHoy = new ArrayList<>(fichas.delMedicoEnFecha(m.medicoId(), ahora.toLocalDate()));
            deHoy.removeIf(f -> f.estado() != EstadoFichaInterna.ASIGNADA);
            deHoy.sort(Comparator.comparing(FichaInternaDTO::inicio));
            for (FichaInternaDTO f : deHoy) {
                if (!f.inicio().isAfter(libre) && f.fin().isAfter(libre)) {
                    libre = f.fin();
                }
            }
            if (mejor == null || libre.isBefore(mejor)) {
                mejor = libre;
                elegido = m;
            }
        }
        if (elegido == null) {
            return new Resultado(Tipo.EN_ESPERA, ficha, null);
        }
        Integer consultorio = horariosActivos(elegido.medicoId()).stream().findFirst()
                .map(HorarioDTO::consultorioId).orElse(null);
        Hueco hueco = new Hueco(elegido, consultorio, mejor.toLocalDate(), mejor.toLocalTime());
        return new Resultado(Tipo.ASIGNADA, guardar(ficha, hueco), null);
    }

    /** Primer hueco de ese dia: primero el especialista con menos fichas (empate: menor id). */
    private Optional<Hueco> buscarHueco(FichaInternaDTO ficha, LocalDate dia, LocalTime desde, Set<Long> ignorar) {
        List<Intervalo> paciente = ocupacionPaciente(ficha.pacienteId(), dia, conMas(ignorar, ficha.fichaId()));
        Map<Long, List<FichaInternaDTO>> delDia = new HashMap<>();
        List<MedicoDTO> candidatos = activos(ficha.especialidad());
        for (MedicoDTO m : candidatos) {
            List<FichaInternaDTO> lista = new ArrayList<>(fichas.delMedicoEnFecha(m.medicoId(), dia));
            lista.removeIf(f -> f.estado() == EstadoFichaInterna.CANCELADA || ignorar.contains(f.fichaId())
                    || f.fichaId() == ficha.fichaId());
            delDia.put(m.medicoId(), lista);
        }
        candidatos.sort(Comparator.comparingInt((MedicoDTO m) -> delDia.get(m.medicoId()).size())
                .thenComparingLong(MedicoDTO::medicoId));
        for (MedicoDTO m : candidatos) {
            List<FichaInternaDTO> suyas = delDia.get(m.medicoId());
            if (suyas.size() >= m.cupoMaximoDia()) {
                continue;
            }
            List<Intervalo> medico = ocupacionMedico(m.medicoId(), dia, suyas);
            int duracion = m.duracionCitaMin();
            List<HorarioDTO> franjas = new ArrayList<>(horariosActivos(m.medicoId()));
            franjas.removeIf(h -> h.diaSemana() != dia.getDayOfWeek());
            franjas.sort(Comparator.comparing(HorarioDTO::horaInicio));
            for (HorarioDTO franja : franjas) {
                LocalDateTime t = LocalDateTime.of(dia, franja.horaInicio());
                LocalDateTime cierre = LocalDateTime.of(dia, franja.horaFin());
                while (!t.plusMinutes(duracion).isAfter(cierre)) {
                    LocalDateTime fin = t.plusMinutes(duracion);
                    boolean aTiempo = desde == null || !t.toLocalTime().isBefore(desde);
                    if (aTiempo && libre(medico, t, fin) && libre(paciente, t, fin)) {
                        return Optional.of(new Hueco(m, franja.consultorioId(), dia, t.toLocalTime()));
                    }
                    t = fin;
                }
            }
        }
        return Optional.empty();
    }

    private FichaInternaDTO guardar(FichaInternaDTO ficha, Hueco h) {
        String nombreConsultorio = h.consultorioId() == null ? null
                : consultorios.porId(h.consultorioId()).map(ConsultorioDTO::nombre).orElse(null);
        FichaInternaDTO asignada = ficha.asignada(h.medico().medicoId(), nombre(h.medico()), h.consultorioId(),
                nombreConsultorio, h.dia(), h.hora(), h.medico().duracionCitaMin());
        fichas.guardarAsignacion(asignada);
        return asignada;
    }

    private List<Intervalo> ocupacionPaciente(long pacienteId, LocalDate dia, Set<Long> ignorar) {
        List<Intervalo> lista = new ArrayList<>();
        for (CitaDTO c : citas.delPaciente(pacienteId)) {
            if (c.fecha().equals(dia) && c.estado() != EstadoCita.CANCELADA) {
                int duracion = medicos.porId(c.medicoId()).map(MedicoDTO::duracionCitaMin).orElse(30);
                LocalDateTime inicio = LocalDateTime.of(dia, c.hora());
                lista.add(new Intervalo(inicio, inicio.plusMinutes(duracion)));
            }
        }
        for (FichaInternaDTO f : fichas.delPaciente(pacienteId)) {
            if (dia.equals(f.fecha()) && !ignorar.contains(f.fichaId())
                    && (f.estado() == EstadoFichaInterna.ASIGNADA || f.estado() == EstadoFichaInterna.ATENDIDA)) {
                lista.add(new Intervalo(f.inicio(), f.fin()));
            }
        }
        for (LocalDateTime[] o : extra.delPaciente(pacienteId, dia)) {
            lista.add(new Intervalo(o[0], o[1]));
        }
        return lista;
    }

    private List<Intervalo> ocupacionMedico(long medicoId, LocalDate dia, List<FichaInternaDTO> suyas) {
        List<Intervalo> lista = new ArrayList<>();
        for (FichaInternaDTO f : suyas) {
            if (f.fecha() != null) {
                lista.add(new Intervalo(f.inicio(), f.fin()));
            }
        }
        int duracion = medicos.porId(medicoId).map(MedicoDTO::duracionCitaMin).orElse(30);
        for (CitaDTO c : citas.delMedicoEnFecha(medicoId, dia)) {
            if (c.estado() != EstadoCita.CANCELADA) {
                LocalDateTime inicio = LocalDateTime.of(dia, c.hora());
                lista.add(new Intervalo(inicio, inicio.plusMinutes(duracion)));
            }
        }
        for (LocalDateTime[] o : extra.delMedico(medicoId, dia)) {
            lista.add(new Intervalo(o[0], o[1]));
        }
        return lista;
    }

    private static boolean libre(List<Intervalo> ocupados, LocalDateTime inicio, LocalDateTime fin) {
        for (Intervalo o : ocupados) {
            if (o.cruza(inicio, fin)) {
                return false;
            }
        }
        return true;
    }

    private List<MedicoDTO> activos(TipoEspecialidad esp) {
        List<MedicoDTO> lista = new ArrayList<>(medicos.listar(esp));
        lista.removeIf(m -> !m.activo() || m.especialidad() != esp);
        return lista;
    }

    private List<HorarioDTO> horariosActivos(long medicoId) {
        List<HorarioDTO> lista = new ArrayList<>(horarios.delMedico(medicoId));
        lista.removeIf(h -> !h.activo());
        return lista;
    }

    /** Lunes a viernes que no estan en el catalogo de dias no habiles. */
    public boolean esHabil(LocalDate dia) {
        return dia.getDayOfWeek() != DayOfWeek.SATURDAY && dia.getDayOfWeek() != DayOfWeek.SUNDAY
                && !diasNoHabiles.existeActivoEnFecha(dia);
    }

    private LocalDate siguienteHabil(LocalDate dia) {
        LocalDate d = dia.plusDays(1);
        for (int i = 0; i < 60 && !esHabil(d); i++) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Ahora redondeado a los siguientes 5 minutos (una urgencia no se agenda en el pasado). */
    private static LocalTime desdeAhora(LocalDateTime ahora) {
        LocalDateTime t = ahora.truncatedTo(ChronoUnit.MINUTES);
        int resto = t.getMinute() % 5;
        if (resto != 0 || ahora.getSecond() > 0) {
            t = t.plusMinutes(5 - resto);
        }
        return t.toLocalDate().isAfter(ahora.toLocalDate()) ? LocalTime.MAX : t.toLocalTime();
    }

    private static String nombre(MedicoDTO m) {
        return (m.nombres() + " " + m.apellidos()).trim();
    }

    private static Set<Long> conMas(Set<Long> base, long id) {
        java.util.HashSet<Long> todos = new java.util.HashSet<>(base);
        todos.add(id);
        return todos;
    }
}
