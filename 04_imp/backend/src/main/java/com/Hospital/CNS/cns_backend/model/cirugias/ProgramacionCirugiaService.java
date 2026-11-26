package com.Hospital.CNS.cns_backend.model.cirugias;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.ComparadorPrioridad;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.PrioridadClinica;
import com.Hospital.CNS.cns_backend.model.compartido.ReglasEdad;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.CambioFichaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoRepository;

/**
 * CU-24 (T100-23): programar una cirugia en un quirofano de la especialidad sin ningun choque.
 *
 * <ul>
 *   <li>Solo el especialista que ATENDIO la ficha (sera el cirujano); su especialidad opera
 *       (ESPECIALIDAD_NO_OPERA); duracion dentro del rango (DURACION_FUERA_DE_RANGO).</li>
 *   <li>Cero choques: quirofano [inicio, fin + limpieza), cirujano [inicio, fin) libre de cirugias, fichas
 *       internas y citas, paciente libre. La base lo garantiza con EXCLUDE (CHOQUE_DE_HORARIO al confirmar).</li>
 *   <li>Normal: dias habiles desde manana, hasta 15, dentro de apertura y cierre (fin + limpieza &lt;= cierre).
 *       Urgente: desde ahora, tambien fuera de horario.</li>
 *   <li>Sin hueco: desplaza la PROGRAMADA de menor prioridad (no urgente, que no empezo) si en su lugar entra la
 *       nueva; la desplazada se reprograma al siguiente hueco libre (hasta 60 dias habiles, sin desplazar a otra).</li>
 *   <li>Dos pasos: {@link #proponer} no guarda; {@link #confirmar} guarda todo en una transaccion.</li>
 * </ul>
 */
public class ProgramacionCirugiaService {

    public static final int DIAS_HABILES_NORMAL = 15;
    public static final int DIAS_HABILES_REPROGRAMAR = 60;
    private static final int PASO_MIN = 10;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Un horario que todavia no esta guardado pero hay que contar como ocupado (la nueva cirugia). */
    private record Reserva(int quirofanoId, long cirujanoId, long pacienteId, LocalDateTime inicio, LocalDateTime fin,
                           LocalDateTime finConLimpieza) {
    }

    private record Hueco(QuirofanoDTO quirofano, LocalDateTime inicio) {
    }

    private final CirugiaRepository cirugias;
    private final FichaInternaRepository fichas;
    private final QuirofanoRepository quirofanos;
    private final MedicoRepository medicos;
    private final CitaRepository citas;
    private final DiaNoHabilRepository diasNoHabiles;
    private final CambioFichaRepository cambios;
    private final NotificacionPublicador avisos;
    private final Transaccion transaccion;
    private final Reloj reloj;

    public ProgramacionCirugiaService(CirugiaRepository cirugias, FichaInternaRepository fichas,
                                      QuirofanoRepository quirofanos, MedicoRepository medicos, CitaRepository citas,
                                      DiaNoHabilRepository diasNoHabiles, CambioFichaRepository cambios,
                                      NotificacionPublicador avisos, Transaccion transaccion, Reloj reloj) {
        this.cirugias = cirugias;
        this.fichas = fichas;
        this.quirofanos = quirofanos;
        this.medicos = medicos;
        this.citas = citas;
        this.diasNoHabiles = diasNoHabiles;
        this.cambios = cambios;
        this.avisos = avisos;
        this.transaccion = transaccion;
        this.reloj = reloj;
    }

    /** "Proponer horario": calcula el primer hueco sin choques (o a quien desplazaria). No guarda nada. */
    public CirugiaDTOs.Propuesta proponer(CirugiaDTOs.Solicitud s, long cirujanoId) {
        MedicoDTO cirujano = validar(s, cirujanoId);
        FichaInternaDTO ficha = fichaAtendida(s.fichaTicket(), cirujanoId);
        PrioridadClinica prioridad = new PrioridadClinica(s.urgente(), s.gravedad(), adultoMayor(ficha), Long.MAX_VALUE);
        LocalDateTime ahora = reloj.ahora();
        List<QuirofanoDTO> suyos = quirofanos.activosDe(cirujano.especialidad());
        if (suyos.isEmpty()) {
            throw new DominioException("SIN_QUIROFANO", "Su especialidad no tiene quirofanos activos");
        }
        Optional<Hueco> hueco;
        LocalDateTime desde;
        LocalDateTime hasta;
        if (s.urgente()) {
            LocalDateTime inicio = redondear(ahora);
            hueco = buscarEnDia(suyos, inicio.toLocalDate(), inicio, true, s.duracionMin(), cirujanoId,
                    ficha.pacienteId(), null, null);
            desde = ahora;
            hasta = ahora.toLocalDate().atTime(LocalTime.MAX);
        } else {
            hueco = buscarEnDiasHabiles(suyos, ahora.toLocalDate(), DIAS_HABILES_NORMAL, s.duracionMin(), cirujanoId,
                    ficha.pacienteId(), null, null);
            desde = ahora;
            hasta = sumarHabiles(ahora.toLocalDate(), DIAS_HABILES_NORMAL).atTime(LocalTime.MAX);
        }
        if (hueco.isPresent()) {
            return propuesta(hueco.get(), s.duracionMin(), null);
        }
        return desplazando(suyos, s, cirujanoId, ficha, prioridad, desde, hasta)
                .orElseThrow(() -> new DominioException("SIN_HUECO", s.urgente()
                        ? "No hay quirofano libre ni cirugia que se pueda desplazar hoy"
                        : "No hay quirofano libre en los proximos " + DIAS_HABILES_NORMAL + " dias habiles"));
    }

    /** "Confirmar": guarda la propuesta (y mueve a la desplazada) en una transaccion; si choca, CHOQUE_DE_HORARIO. */
    public CirugiaDTOs.Vista confirmar(CirugiaDTOs.Confirmacion c, long cirujanoId) {
        MedicoDTO cirujano = validar(c.solicitud(), cirujanoId);
        FichaInternaDTO ficha = fichaAtendida(c.fichaTicket(), cirujanoId);
        QuirofanoDTO quirofano = quirofanos.porId(c.quirofanoId())
                .filter(q -> q.activo() && q.especialidad() == cirujano.especialidad())
                .orElseThrow(() -> new DominioException("DATOS_INVALIDOS", "Ese quirofano no es de su especialidad"));
        if (c.fecha() == null || c.inicio() == null) {
            throw new DominioException("DATOS_INVALIDOS", "Falta la fecha u hora de la propuesta");
        }
        LocalDateTime inicio = LocalDateTime.of(c.fecha(), c.inicio());
        LocalDateTime ahora = reloj.ahora();
        if (inicio.isBefore(ahora.truncatedTo(ChronoUnit.MINUTES))) {
            throw choque(quirofano);
        }
        long id = cirugias.nextIdentity();
        CirugiaDTO nueva = new CirugiaDTO(id, ficha.fichaId(), ficha.pacienteId(), ficha.nombrePaciente(),
                ficha.numeroAfiliado(), ficha.fechaNacimiento(), cirujanoId, nombre(cirujano), quirofano.quirofanoId(),
                quirofano.nombre(), cirujano.especialidad(), inicio, c.duracionMin(), quirofano.limpiezaMin(),
                c.gravedad(), c.urgente(), adultoMayor(ficha), id, EstadoCirugia.PROGRAMADA, ahora);
        transaccion.ejecutar(() -> {
            CirugiaDTO movida = null;
            if (c.desplazaNumero() != null) {
                CirugiaDTO victima = cirugias.porId(c.desplazaNumero()).orElseThrow(() -> choque(quirofano));
                boolean menor = ComparadorPrioridad.INSTANCIA.compare(nueva.prioridad(), victima.prioridad()) < 0;
                if (victima.estado() != EstadoCirugia.PROGRAMADA || victima.urgencia() || !menor
                        || c.desplazaFecha() == null || c.desplazaInicio() == null
                        || !cirugias.reprogramar(victima.cirugiaId(), victima.quirofanoId(),
                                LocalDateTime.of(c.desplazaFecha(), c.desplazaInicio()))) {
                    throw choque(quirofano);
                }
                movida = cirugias.porId(victima.cirugiaId()).orElse(victima);
            }
            boolean libres = libre(ocupacionPersona(cirujanoId, ficha.pacienteId(), inicio.toLocalDate(), null, true),
                    inicio, nueva.fin())
                    && libre(ocupacionPersona(cirujanoId, ficha.pacienteId(), inicio.toLocalDate(), null, false),
                    inicio, nueva.fin());
            if (!libres || !cirugias.insertar(nueva)) {
                throw choque(quirofano);
            }
            cambios.registrar("CIRUGIA", id, "PROGRAMADA", "Medico " + cirujanoId, "Ficha N° " + ficha.fichaId());
            avisos.publicar(NotificacionDTO.deCirugia(nueva.pacienteId(), id, "CIRUGIA_PROGRAMADA",
                    "Cirugia N° " + id + " programada: " + inicio.format(FECHA) + " a las " + inicio.format(HORA)
                            + " en el Quirofano " + quirofano.nombre() + ". Ayuno de 8 horas; llegue 1 hora antes.",
                    ahora));
            if (movida != null) {
                cambios.registrar("CIRUGIA", movida.cirugiaId(), "REPROGRAMADA", "Sistema",
                        "Por la cirugia N° " + id + " de mayor prioridad");
                avisos.publicar(NotificacionDTO.deCirugia(movida.pacienteId(), movida.cirugiaId(),
                        "CIRUGIA_REPROGRAMADA", "Su cirugia N° " + movida.cirugiaId()
                                + " se reprogramo por un caso de mayor prioridad: " + movida.inicio().format(FECHA)
                                + " a las " + movida.inicio().format(HORA) + ". Pida que le reimpriman el ticket.",
                        ahora));
            }
            return movida;
        });
        return vista(cirugias.porId(id).orElse(nueva), false);
    }

    /** Cirugias del dia en los quirofanos de la especialidad del especialista. */
    public List<CirugiaDTOs.Vista> delDia(long medicoId, LocalDate fechaONull) {
        MedicoDTO medico = especialistaQueOpera(medicoId);
        LocalDate fecha = fechaONull != null ? fechaONull : reloj.ahora().toLocalDate();
        return cirugias.deEspecialidadEnFecha(medico.especialidad(), fecha).stream()
                .sorted(Comparator.comparing(CirugiaDTO::inicio)).map(c -> vista(c, false)).toList();
    }

    /** Nombres de los quirofanos activos de su especialidad (filas de la linea de tiempo). */
    public List<String> quirofanosDe(long medicoId) {
        MedicoDTO medico = especialistaQueOpera(medicoId);
        return quirofanos.activosDe(medico.especialidad()).stream().map(QuirofanoDTO::nombre).sorted().toList();
    }

    /** Historial: cirugias del paciente si el medico lo tiene en su agenda, en una ficha o en una cirugia. */
    public List<CirugiaDTOs.Vista> delPaciente(long pacienteId, long medicoId) {
        List<CirugiaDTO> lista = cirugias.delPaciente(pacienteId);
        boolean suyo = citas.medicoTienePacienteEnAgenda(medicoId, pacienteId)
                || fichas.medicoTieneFichaConPaciente(medicoId, pacienteId)
                || lista.stream().anyMatch(c -> c.cirujanoId() == medicoId);
        if (!suyo) {
            throw new DominioException("PACIENTE_FUERA_DE_AGENDA", "Solo puede ver las cirugias de pacientes de su agenda");
        }
        boolean gravedad = medicos.porId(medicoId).map(m -> m.especialidad().puedeDerivar()).orElse(false);
        return lista.stream().sorted(Comparator.comparing(CirugiaDTO::inicio).reversed())
                .map(c -> vista(c, !gravedad)).toList();
    }

    public CirugiaDTOs.Vista porId(long cirugiaId) {
        return vista(cirugias.porId(cirugiaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "La cirugia no existe")), false);
    }

    /** "Realizada" o "No se presento" (CANCELADA, el horario no se reasigna). Solo el cirujano. */
    public CirugiaDTOs.Vista marcar(long cirugiaId, long cirujanoId, EstadoCirugia estado) {
        if (estado != EstadoCirugia.REALIZADA && estado != EstadoCirugia.CANCELADA) {
            throw new DominioException("DATOS_INVALIDOS", "Solo se marca REALIZADA o CANCELADA");
        }
        CirugiaDTO c = cirugias.porId(cirugiaId)
                .orElseThrow(() -> new DominioException("NO_ENCONTRADA", "La cirugia no existe"));
        if (c.cirujanoId() != cirujanoId) {
            throw new DominioException("NO_ES_DE_SU_AGENDA", "Solo el cirujano marca su cirugia");
        }
        transaccion.ejecutar(() -> {
            if (!cirugias.cambiarEstado(cirugiaId, EstadoCirugia.PROGRAMADA, estado)) {
                throw new DominioException("ESTADO_NO_PERMITE", "La cirugia ya no esta programada");
            }
            cambios.registrar("CIRUGIA", cirugiaId, estado == EstadoCirugia.REALIZADA ? "REALIZADA" : "NO_SE_PRESENTO",
                    "Medico " + cirujanoId, null);
        });
        return porId(cirugiaId);
    }

    // ---------------------------------------------------------------------------------------------------------

    private Optional<CirugiaDTOs.Propuesta> desplazando(List<QuirofanoDTO> suyos, CirugiaDTOs.Solicitud s,
                                                        long cirujanoId, FichaInternaDTO ficha,
                                                        PrioridadClinica prioridad, LocalDateTime desde,
                                                        LocalDateTime hasta) {
        List<CirugiaDTO> candidatas = new ArrayList<>();
        for (CirugiaDTO c : cirugias.programadasEntre(ficha.especialidad(), desde, hasta)) {
            if (c.estado() == EstadoCirugia.PROGRAMADA && !c.urgencia() && c.inicio().isAfter(reloj.ahora())
                    && c.pacienteId() != ficha.pacienteId()
                    && ComparadorPrioridad.INSTANCIA.compare(prioridad, c.prioridad()) < 0) {
                candidatas.add(c);
            }
        }
        candidatas.sort(Comparator.comparing(CirugiaDTO::prioridad, ComparadorPrioridad.INSTANCIA).reversed());
        for (CirugiaDTO victima : candidatas) {
            QuirofanoDTO q = suyos.stream().filter(x -> x.quirofanoId() == victima.quirofanoId()).findFirst().orElse(null);
            if (q == null) {
                continue;
            }
            LocalDateTime inicio = victima.inicio();
            if (!cabe(q, inicio, s.duracionMin(), s.urgente(), cirujanoId, ficha.pacienteId(), victima.cirugiaId(), null)) {
                continue;
            }
            LocalDateTime fin = inicio.plusMinutes(s.duracionMin());
            Reserva nueva = new Reserva(q.quirofanoId(), cirujanoId, ficha.pacienteId(), inicio, fin,
                    fin.plusMinutes(q.limpiezaMin()));
            Optional<Hueco> otro = buscarEnDiasHabiles(List.of(q), inicio.toLocalDate(), DIAS_HABILES_REPROGRAMAR,
                    victima.duracionMin(), victima.cirujanoId(), victima.pacienteId(), victima.cirugiaId(), nueva);
            if (otro.isPresent()) {
                CirugiaDTOs.Desplazamiento d = new CirugiaDTOs.Desplazamiento(vista(victima, true),
                        otro.get().inicio().toLocalDate(), otro.get().inicio().toLocalTime().format(HORA));
                return Optional.of(propuesta(new Hueco(q, inicio), s.duracionMin(), d));
            }
        }
        return Optional.empty();
    }

    /** Busca desde el dia habil siguiente a {@code desde}. */
    private Optional<Hueco> buscarEnDiasHabiles(List<QuirofanoDTO> suyos, LocalDate desde, int dias, int duracion,
                                                long cirujanoId, long pacienteId, Long ignorar, Reserva reserva) {
        LocalDate dia = desde;
        for (int i = 0; i < dias; i++) {
            dia = siguienteHabil(dia);
            Optional<Hueco> h = buscarEnDia(suyos, dia, null, false, duracion, cirujanoId, pacienteId, ignorar, reserva);
            if (h.isPresent()) {
                return h;
            }
        }
        return Optional.empty();
    }

    /** El hueco mas temprano del dia entre los quirofanos (empate: el primero por nombre). */
    private Optional<Hueco> buscarEnDia(List<QuirofanoDTO> suyos, LocalDate dia, LocalDateTime desde, boolean urgente,
                                        int duracion, long cirujanoId, long pacienteId, Long ignorar, Reserva reserva) {
        Hueco mejor = null;
        List<QuirofanoDTO> ordenados = new ArrayList<>(suyos);
        ordenados.sort(Comparator.comparing(QuirofanoDTO::nombre));
        for (QuirofanoDTO q : ordenados) {
            LocalDateTime t = urgente ? desde : LocalDateTime.of(dia, q.apertura());
            if (!urgente && desde != null && desde.isAfter(t)) {
                t = desde;
            }
            LocalDateTime limite = urgente ? dia.atTime(23, 59) : LocalDateTime.of(dia, q.cierre());
            while (!t.plusMinutes(duracion + (long) q.limpiezaMin()).isAfter(limite)) {
                if (mejor != null && !t.isBefore(mejor.inicio())) {
                    break;
                }
                if (cabe(q, t, duracion, urgente, cirujanoId, pacienteId, ignorar, reserva)) {
                    mejor = new Hueco(q, t);
                    break;
                }
                t = t.plusMinutes(PASO_MIN);
            }
        }
        return Optional.ofNullable(mejor);
    }

    /** ¿Entra una cirugia de {@code duracion} en ese quirofano a esa hora, sin ningun choque? */
    private boolean cabe(QuirofanoDTO q, LocalDateTime inicio, int duracion, boolean urgente, long cirujanoId,
                         long pacienteId, Long ignorar, Reserva reserva) {
        LocalDate dia = inicio.toLocalDate();
        LocalDateTime fin = inicio.plusMinutes(duracion);
        LocalDateTime finLimpieza = fin.plusMinutes(q.limpiezaMin());
        if (!urgente && (inicio.toLocalTime().isBefore(q.apertura()) || finLimpieza.toLocalTime().isAfter(q.cierre())
                || !finLimpieza.toLocalDate().equals(dia))) {
            return false;
        }
        List<LocalDateTime[]> sala = new ArrayList<>();
        for (CirugiaDTO c : cirugias.deEspecialidadEnFecha(q.especialidad(), dia)) {
            if (c.quirofanoId() == q.quirofanoId() && !Long.valueOf(c.cirugiaId()).equals(ignorar)) {
                sala.add(new LocalDateTime[] {c.inicio(), c.finConLimpieza()});
            }
        }
        if (reserva != null && reserva.quirofanoId() == q.quirofanoId()) {
            sala.add(new LocalDateTime[] {reserva.inicio(), reserva.finConLimpieza()});
        }
        if (!libre(sala, inicio, finLimpieza)) {
            return false;
        }
        List<LocalDateTime[]> cirujano = ocupacionPersona(cirujanoId, pacienteId, dia, ignorar, true);
        List<LocalDateTime[]> paciente = ocupacionPersona(cirujanoId, pacienteId, dia, ignorar, false);
        if (reserva != null) {
            if (reserva.cirujanoId() == cirujanoId) {
                cirujano.add(new LocalDateTime[] {reserva.inicio(), reserva.fin()});
            }
            if (reserva.pacienteId() == pacienteId) {
                paciente.add(new LocalDateTime[] {reserva.inicio(), reserva.fin()});
            }
        }
        return libre(cirujano, inicio, fin) && libre(paciente, inicio, fin);
    }

    /** Ocupacion del cirujano ({@code delCirujano}) o del paciente ese dia: cirugias, fichas internas y citas. */
    private List<LocalDateTime[]> ocupacionPersona(long cirujanoId, long pacienteId, LocalDate dia, Long ignorar,
                                                   boolean delCirujano) {
        List<LocalDateTime[]> lista = new ArrayList<>();
        List<CirugiaDTO> suyas = delCirujano ? cirugias.delCirujanoEnFecha(cirujanoId, dia)
                : cirugias.delPaciente(pacienteId).stream()
                        .filter(c -> c.inicio().toLocalDate().equals(dia) && c.estado() != EstadoCirugia.CANCELADA)
                        .toList();
        for (CirugiaDTO c : suyas) {
            if (!Long.valueOf(c.cirugiaId()).equals(ignorar)) {
                lista.add(new LocalDateTime[] {c.inicio(), c.fin()});
            }
        }
        List<FichaInternaDTO> f = delCirujano ? fichas.delMedicoEnFecha(cirujanoId, dia) : fichas.delPaciente(pacienteId);
        for (FichaInternaDTO x : f) {
            if (dia.equals(x.fecha()) && x.estado() == EstadoFichaInterna.ASIGNADA) {
                lista.add(new LocalDateTime[] {x.inicio(), x.fin()});
            }
        }
        List<CitaDTO> c = delCirujano ? citas.delMedicoEnFecha(cirujanoId, dia) : citas.delPaciente(pacienteId);
        for (CitaDTO x : c) {
            if (x.fecha().equals(dia) && x.estado() != EstadoCita.CANCELADA) {
                int duracion = medicos.porId(x.medicoId()).map(MedicoDTO::duracionCitaMin).orElse(30);
                LocalDateTime inicio = LocalDateTime.of(dia, x.hora());
                lista.add(new LocalDateTime[] {inicio, inicio.plusMinutes(duracion)});
            }
        }
        return lista;
    }

    private static boolean libre(List<LocalDateTime[]> ocupados, LocalDateTime inicio, LocalDateTime fin) {
        for (LocalDateTime[] o : ocupados) {
            if (o[0].isBefore(fin) && inicio.isBefore(o[1])) {
                return false;
            }
        }
        return true;
    }

    private MedicoDTO validar(CirugiaDTOs.Solicitud s, long cirujanoId) {
        MedicoDTO cirujano = especialistaQueOpera(cirujanoId);
        if (s.gravedad() == null) {
            throw new DominioException("DATOS_INVALIDOS", "Elija la gravedad");
        }
        TipoEspecialidad e = cirujano.especialidad();
        if (s.duracionMin() < e.cirugiaMinMinutos() || s.duracionMin() > e.cirugiaMaxMinutos()) {
            throw new DominioException("DURACION_FUERA_DE_RANGO", "La duracion debe estar entre "
                    + e.cirugiaMinMinutos() + " y " + e.cirugiaMaxMinutos() + " minutos");
        }
        return cirujano;
    }

    private MedicoDTO especialistaQueOpera(long medicoId) {
        MedicoDTO m = medicos.porId(medicoId).filter(MedicoDTO::activo)
                .orElseThrow(() -> new DominioException("ACCESO_DENEGADO", "Medico no encontrado o dado de baja"));
        if (!m.especialidad().operaEnQuirofano()) {
            throw new DominioException("ESPECIALIDAD_NO_OPERA", "Su especialidad no opera en quirofano");
        }
        return m;
    }

    private FichaInternaDTO fichaAtendida(long fichaId, long cirujanoId) {
        FichaInternaDTO f = fichas.porId(fichaId).orElse(null);
        if (f == null || f.estado() != EstadoFichaInterna.ATENDIDA || !Long.valueOf(cirujanoId).equals(f.medicoId())) {
            throw new DominioException("FICHA_NO_ATENDIDA",
                    "Solo el especialista que atendio la ficha puede programar la cirugia");
        }
        return f;
    }

    private boolean adultoMayor(FichaInternaDTO f) {
        return ReglasEdad.esAdultoMayor(ReglasEdad.edadEn(f.fechaNacimiento(), reloj.ahora().toLocalDate()));
    }

    private CirugiaDTOs.Propuesta propuesta(Hueco h, int duracion, CirugiaDTOs.Desplazamiento desplaza) {
        LocalDateTime fin = h.inicio().plusMinutes(duracion);
        QuirofanoDTO q = h.quirofano();
        return new CirugiaDTOs.Propuesta(q.quirofanoId(), q.nombre(), q.especialidad(), h.inicio().toLocalDate(),
                h.inicio().format(HORA), fin.format(HORA), fin.plusMinutes(q.limpiezaMin()).format(HORA),
                q.limpiezaMin(), desplaza);
    }

    private CirugiaDTOs.Vista vista(CirugiaDTO c, boolean sinGravedad) {
        return new CirugiaDTOs.Vista(c.cirugiaId(), c.fichaInternaId(), c.pacienteId(), c.nombrePaciente(),
                c.numeroAfiliado(), ReglasEdad.edadEn(c.fechaNacimiento(), reloj.ahora().toLocalDate()),
                c.especialidad(), c.nombreQuirofano(), c.nombreCirujano(), c.inicio().toLocalDate(),
                c.inicio().format(HORA), c.fin().format(HORA), c.finConLimpieza().format(HORA), c.duracionMin(),
                c.urgencia(), sinGravedad ? null : c.gravedad(), c.estado());
    }

    private static DominioException choque(QuirofanoDTO q) {
        return new DominioException("CHOQUE_DE_HORARIO", "Otro especialista confirmo ese horario del Quirofano "
                + q.nombre() + " mientras revisabas la propuesta. No se guardo nada: pide una nueva propuesta.");
    }

    private static LocalDateTime redondear(LocalDateTime ahora) {
        LocalDateTime t = ahora.truncatedTo(ChronoUnit.MINUTES);
        int resto = t.getMinute() % PASO_MIN;
        return resto == 0 && ahora.getSecond() == 0 ? t : t.plusMinutes(PASO_MIN - resto);
    }

    private boolean esHabil(LocalDate dia) {
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

    private LocalDate sumarHabiles(LocalDate dia, int n) {
        LocalDate d = dia;
        for (int i = 0; i < n; i++) {
            d = siguienteHabil(d);
        }
        return d;
    }

    private static String nombre(MedicoDTO m) {
        return (m.nombres() + " " + m.apellidos()).trim();
    }
}
