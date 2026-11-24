package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * CU-02: el administrador mantiene los catalogos (medicos, consultorios, horarios, dias no habiles).
 *
 * <p>Un caso de uso = un metodo. Las reglas viven aqui, no en el controller ni en el repository.
 * Codigos de error (DominioException): DATOS_INVALIDOS, VALIDACION_CUPO, VALIDACION_DURACION,
 * VALIDACION_FECHA, NO_ENCONTRADA, CAMBIO_AFECTA_CITAS, HORARIO_SOLAPADO, CONSULTORIO_DUPLICADO,
 * DIA_NO_HABIL_DUPLICADO. Las bajas son logicas (activo=false): nada se borra fisicamente.</p>
 *
 * <p>Dar de baja un medico o un consultorio tambien da de baja sus horarios (en la misma transaccion), para que
 * la asignacion automatica no use franjas de recursos que ya no atienden.</p>
 */
public class CatalogoService {

    public static final int NOMBRE_MEDICO_MAXIMO = 100;
    public static final int NOMBRE_CONSULTORIO_MAXIMO = 80;
    public static final int DESCRIPCION_DIA_MAXIMA = 200;

    private final MedicoRepository medicos;
    private final ConsultorioRepository consultorios;
    private final HorarioRepository horarios;
    private final DiaNoHabilRepository diasNoHabiles;
    private final CitaRepository citas;
    private final Reloj reloj;
    private final Transaccion transaccion;

    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas) {
        this(m, c, h, d, citas, new RelojSistema());
    }

    /** Igual que el anterior pero con reloj propio (las pruebas usan RelojFijo). */
    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas, Reloj reloj) {
        this(m, c, h, d, citas, reloj, Transaccion.DIRECTA);
    }

    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas, Reloj reloj, Transaccion transaccion) {
        this.medicos = m;
        this.consultorios = c;
        this.horarios = h;
        this.diasNoHabiles = d;
        this.citas = citas;
        this.reloj = reloj;
        this.transaccion = transaccion;
    }

    // ------------------------------------------------------------------ medicos

    public MedicoDTO crearMedico(MedicoDTO datos) {
        Medico validado = medicoValido(datos);
        Medico nuevo = validado.conId(medicos.nextIdentity()).conActivo(true);
        medicos.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    public MedicoDTO modificarMedico(long id, MedicoDTO d) {
        Medico actual = Medico.desde(medicoExistente(id));
        Medico nuevo = medicoValido(d).conId(id).conActivo(actual.isActivo());
        if (nuevo.cambiaLaAgenda(actual) && citas.contarProgramadasDelMedico(id) > 0) {
            throw afectaCitas("El medico tiene citas programadas: no se puede cambiar su especialidad, duracion ni cupo");
        }
        medicos.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    public void darDeBajaMedico(long id) {
        Medico actual = Medico.desde(medicoExistente(id));
        if (!actual.isActivo()) {
            return;
        }
        if (citas.contarProgramadasDelMedico(id) > 0) {
            throw afectaCitas("El medico tiene citas programadas: no se puede dar de baja");
        }
        transaccion.ejecutar(() -> {
            medicos.store(actual.conActivo(false).aDTO());
            darDeBajaHorarios(horarios.delMedico(id));
        });
    }

    public List<MedicoDTO> listarMedicos(TipoEspecialidad filtro) {
        return medicos.listar(filtro);
    }

    // ------------------------------------------------------------- consultorios

    public ConsultorioDTO crearConsultorio(ConsultorioDTO d) {
        if (d == null || !Consultorio.desde(d).isValid()) {
            throw new DominioException("DATOS_INVALIDOS", "El consultorio necesita un nombre");
        }
        String nombre = nombreConsultorio(d);
        if (consultorios.existeActivoConNombre(nombre)) {
            throw new DominioException("CONSULTORIO_DUPLICADO", "Ya existe un consultorio con ese nombre");
        }
        Consultorio nuevo = new Consultorio(consultorios.nextIdentity(), nombre, true);
        consultorios.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    /** Solo cambia el nombre: no toca la agenda, por eso no se revisan citas. */
    public ConsultorioDTO modificarConsultorio(int id, ConsultorioDTO d) {
        Consultorio actual = Consultorio.desde(consultorioExistente(id));
        if (d == null || !Consultorio.desde(d).isValid()) {
            throw new DominioException("DATOS_INVALIDOS", "El consultorio necesita un nombre");
        }
        String nombre = nombreConsultorio(d);
        boolean cambiaNombre = !nombre.equalsIgnoreCase(actual.getNombre());
        if (cambiaNombre && consultorios.existeActivoConNombre(nombre)) {
            throw new DominioException("CONSULTORIO_DUPLICADO", "Ya existe un consultorio con ese nombre");
        }
        Consultorio nuevo = new Consultorio(id, nombre, actual.isActivo());
        consultorios.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    public void darDeBajaConsultorio(int id) {
        Consultorio actual = Consultorio.desde(consultorioExistente(id));
        if (!actual.isActivo()) {
            return;
        }
        if (citas.contarProgramadasEnConsultorio(id) > 0) {
            throw afectaCitas("El consultorio tiene citas programadas: no se puede dar de baja");
        }
        transaccion.ejecutar(() -> {
            consultorios.store(actual.conActivo(false).aDTO());
            darDeBajaHorarios(horarios.delConsultorio(id));
        });
    }

    public List<ConsultorioDTO> listarConsultorios() {
        return consultorios.listar();
    }

    // ----------------------------------------------------------------- horarios

    public HorarioDTO definirHorario(HorarioDTO d) {
        HorarioMedico nuevo = horarioValido(d, 0);
        HorarioMedico guardado = nuevo.conId(horarios.nextIdentity()).conActivo(true);
        horarios.store(guardado.aDTO());
        return guardado.aDTO();
    }

    /** [3b] No se cambia una franja mientras su medico o su consultorio tengan citas PROGRAMADAS. */
    public HorarioDTO modificarHorario(long id, HorarioDTO d) {
        HorarioMedico actual = HorarioMedico.desde(horarioExistente(id));
        HorarioMedico nuevo = horarioValido(d, id).conId(id).conActivo(actual.isActivo());
        verificarHorarioSinCitas(actual, "El horario tiene citas programadas: no se puede modificar");
        horarios.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    public void darDeBajaHorario(long id) {
        HorarioMedico actual = HorarioMedico.desde(horarioExistente(id));
        if (!actual.isActivo()) {
            return;
        }
        verificarHorarioSinCitas(actual, "El horario tiene citas programadas: no se puede dar de baja");
        horarios.store(actual.conActivo(false).aDTO());
    }

    public List<HorarioDTO> listarHorarios() {
        return horarios.listar();
    }

    // ---------------------------------------------------------- dias no habiles

    public DiaNoHabilDTO registrarDiaNoHabil(DiaNoHabilDTO d) {
        DiaNoHabil nuevo = diaNoHabilValido(d, true);
        DiaNoHabilDTO activo = nuevo.conId(diasNoHabiles.nextIdentity()).conActivo(true).aDTO();
        diasNoHabiles.store(activo);
        return activo;
    }

    /** Si cambia la fecha se revalida como un registro nuevo (futura, sin duplicado y sin citas ese dia). */
    public DiaNoHabilDTO modificarDiaNoHabil(long id, DiaNoHabilDTO d) {
        DiaNoHabilDTO actual = diasNoHabiles.porId(id).orElseThrow(() -> noEncontrada("Dia no habil no encontrado"));
        boolean cambiaFecha = d != null && d.fecha() != null && !d.fecha().equals(actual.fecha());
        DiaNoHabil nuevo = diaNoHabilValido(d, cambiaFecha);
        DiaNoHabilDTO guardado = nuevo.conId(id).conActivo(actual.activo()).aDTO();
        diasNoHabiles.store(guardado);
        return guardado;
    }

    /** Quitar un dia no habil solo agrega disponibilidad: no afecta citas programadas. */
    public void darDeBajaDiaNoHabil(long id) {
        DiaNoHabilDTO actual = diasNoHabiles.porId(id).orElseThrow(() -> noEncontrada("Dia no habil no encontrado"));
        if (actual.activo()) {
            diasNoHabiles.store(DiaNoHabil.desde(actual).conActivo(false).aDTO());
        }
    }

    public List<DiaNoHabilDTO> listarDiasNoHabiles() {
        return diasNoHabiles.listar();
    }

    // ------------------------------------------------------------------ interno

    /** Valida y devuelve la entidad; cada fallo lleva su propio codigo ([3a] del diagrama). */
    private Medico medicoValido(MedicoDTO datos) {
        if (datos == null || !Medico.desde(datos).datosBasicosValidos()) {
            throw new DominioException("DATOS_INVALIDOS", "El medico necesita nombres, apellidos y especialidad");
        }
        if (datos.nombres().trim().length() > NOMBRE_MEDICO_MAXIMO
                || datos.apellidos().trim().length() > NOMBRE_MEDICO_MAXIMO) {
            throw new DominioException("DATOS_INVALIDOS",
                    "Nombres y apellidos no pueden pasar de " + NOMBRE_MEDICO_MAXIMO + " caracteres");
        }
        Medico medico = Medico.desde(datos);
        if (!medico.cupoValido()) {
            throw new DominioException("VALIDACION_CUPO",
                    "El cupo diario debe estar entre " + Medico.CUPO_MINIMO + " y " + Medico.CUPO_MAXIMO);
        }
        if (!medico.duracionValida()) {
            throw new DominioException("VALIDACION_DURACION",
                    "La duracion de la ficha debe estar entre " + datos.especialidad().duracionMinMinutos()
                            + " y " + datos.especialidad().duracionMaxMinutos() + " minutos");
        }
        return medico;
    }

    private MedicoDTO medicoExistente(long id) {
        return medicos.porId(id).orElseThrow(() -> noEncontrada("Medico no encontrado"));
    }

    private ConsultorioDTO consultorioExistente(int id) {
        return consultorios.porId(id).orElseThrow(() -> noEncontrada("Consultorio no encontrado"));
    }

    private HorarioDTO horarioExistente(long id) {
        return horarios.porId(id).orElseThrow(() -> noEncontrada("Horario no encontrado"));
    }

    /**
     * Datos completos, medico y consultorio activos y sin cruce con otra franja activa.
     * {@code idPropio} es el horario que se modifica (0 al crear) y no cuenta como cruce consigo mismo.
     */
    private HorarioMedico horarioValido(HorarioDTO d, long idPropio) {
        if (d == null || !HorarioMedico.desde(d).isValid()) {
            throw new DominioException("DATOS_INVALIDOS",
                    "El horario necesita medico, consultorio, dia y una hora de inicio anterior a la de fin");
        }
        HorarioMedico nuevo = HorarioMedico.desde(d);
        medicos.porId(nuevo.getMedicoId()).filter(MedicoDTO::activo)
                .orElseThrow(() -> noEncontrada("Medico no encontrado o dado de baja"));
        consultorios.porId(nuevo.getConsultorioId()).filter(ConsultorioDTO::activo)
                .orElseThrow(() -> noEncontrada("Consultorio no encontrado o dado de baja"));

        for (HorarioDTO existente : horarios.delMedico(nuevo.getMedicoId())) {
            verificarSinSolape(nuevo, existente, idPropio, "El medico ya tiene un horario que se cruza con ese");
        }
        for (HorarioDTO existente : horarios.delConsultorio(nuevo.getConsultorioId())) {
            verificarSinSolape(nuevo, existente, idPropio, "El consultorio ya esta ocupado en ese horario");
        }
        return nuevo;
    }

    private void verificarHorarioSinCitas(HorarioMedico horario, String mensaje) {
        if (citas.contarProgramadasDelMedico(horario.getMedicoId()) > 0
                || citas.contarProgramadasEnConsultorio(horario.getConsultorioId()) > 0) {
            throw afectaCitas(mensaje);
        }
    }

    /** {@code revisarFecha}: al crear o al cambiar la fecha se exige futura, sin duplicado y sin citas. */
    private DiaNoHabil diaNoHabilValido(DiaNoHabilDTO d, boolean revisarFecha) {
        if (d == null || !DiaNoHabil.desde(d).datosBasicosValidos()) {
            throw new DominioException("DATOS_INVALIDOS", "El dia no habil necesita fecha y descripcion");
        }
        if (d.descripcion().trim().length() > DESCRIPCION_DIA_MAXIMA) {
            throw new DominioException("DATOS_INVALIDOS",
                    "La descripcion no puede pasar de " + DESCRIPCION_DIA_MAXIMA + " caracteres");
        }
        DiaNoHabil dia = DiaNoHabil.desde(d);
        if (!revisarFecha) {
            return dia;
        }
        LocalDate hoy = reloj.ahora().toLocalDate();
        if (!dia.fechaFutura(hoy)) {
            throw new DominioException("VALIDACION_FECHA", "El dia no habil debe ser una fecha futura");
        }
        if (diasNoHabiles.existeActivoEnFecha(dia.getFecha())) {
            throw new DominioException("DIA_NO_HABIL_DUPLICADO", "Esa fecha ya esta registrada como dia no habil");
        }
        boolean hayProgramadas = citas.delDia(dia.getFecha()).stream()
                .anyMatch(c -> c.estado() == EstadoCita.PROGRAMADA);
        if (hayProgramadas) {
            throw afectaCitas("Hay citas programadas ese dia: no se puede marcar como no habil");
        }
        return dia;
    }

    private static void verificarSinSolape(HorarioMedico nuevo, HorarioDTO existente, long idPropio, String mensaje) {
        if (existente.activo() && existente.horarioId() != idPropio
                && nuevo.seSolapaCon(HorarioMedico.desde(existente))) {
            throw new DominioException("HORARIO_SOLAPADO", mensaje);
        }
    }

    private static String nombreConsultorio(ConsultorioDTO d) {
        String nombre = d.nombre().trim();
        if (nombre.length() > NOMBRE_CONSULTORIO_MAXIMO) {
            throw new DominioException("DATOS_INVALIDOS",
                    "El nombre del consultorio no puede pasar de " + NOMBRE_CONSULTORIO_MAXIMO + " caracteres");
        }
        return nombre;
    }

    private void darDeBajaHorarios(List<HorarioDTO> lista) {
        for (HorarioDTO h : lista) {
            if (h.activo()) {
                horarios.store(HorarioMedico.desde(h).conActivo(false).aDTO());
            }
        }
    }

    private static DominioException noEncontrada(String mensaje) {
        return new DominioException("NO_ENCONTRADA", mensaje);
    }

    private static DominioException afectaCitas(String mensaje) {
        return new DominioException("CAMBIO_AFECTA_CITAS", mensaje);
    }
}
