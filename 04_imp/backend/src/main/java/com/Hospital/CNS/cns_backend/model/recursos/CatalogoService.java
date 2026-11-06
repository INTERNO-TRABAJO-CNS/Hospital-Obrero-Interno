package com.Hospital.CNS.cns_backend.model.recursos;

import java.time.LocalDate;
import java.util.List;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-02: el administrador mantiene los catalogos (medicos, consultorios, horarios, dias no habiles).
 *
 * <p>Un caso de uso = un metodo. Las reglas viven aqui, no en el controller ni en el repository.
 * Codigos de error (DominioException): DATOS_INVALIDOS, VALIDACION_CUPO, VALIDACION_DURACION,
 * VALIDACION_FECHA, NO_ENCONTRADA, CAMBIO_AFECTA_CITAS, HORARIO_SOLAPADO, CONSULTORIO_DUPLICADO,
 * DIA_NO_HABIL_DUPLICADO. Las bajas son logicas (activo=false): nada se borra fisicamente.</p>
 */
public class CatalogoService {

    private final MedicoRepository medicos;
    private final ConsultorioRepository consultorios;
    private final HorarioRepository horarios;
    private final DiaNoHabilRepository diasNoHabiles;
    private final CitaRepository citas;
    private final Reloj reloj;

    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas) {
        this(m, c, h, d, citas, new RelojSistema());
    }

    /** Igual que el anterior pero con reloj propio (las pruebas usan RelojFijo). */
    public CatalogoService(MedicoRepository m, ConsultorioRepository c, HorarioRepository h,
                           DiaNoHabilRepository d, CitaRepository citas, Reloj reloj) {
        this.medicos = m;
        this.consultorios = c;
        this.horarios = h;
        this.diasNoHabiles = d;
        this.citas = citas;
        this.reloj = reloj;
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
        medicos.store(actual.conActivo(false).aDTO());
    }

    public List<MedicoDTO> listarMedicos(TipoEspecialidad filtro) {
        return medicos.listar(filtro);
    }

    // ------------------------------------------------------------- consultorios

    public ConsultorioDTO crearConsultorio(ConsultorioDTO d) {
        if (d == null || !Consultorio.desde(d).isValid()) {
            throw new DominioException("DATOS_INVALIDOS", "El consultorio necesita un nombre");
        }
        String nombre = d.nombre().trim();
        if (consultorios.existeActivoConNombre(nombre)) {
            throw new DominioException("CONSULTORIO_DUPLICADO", "Ya existe un consultorio con ese nombre");
        }
        Consultorio nuevo = new Consultorio(consultorios.nextIdentity(), nombre, true);
        consultorios.store(nuevo.aDTO());
        return nuevo.aDTO();
    }

    public void darDeBajaConsultorio(int id) {
        Consultorio actual = Consultorio.desde(consultorios.porId(id)
                .orElseThrow(() -> noEncontrada("Consultorio no encontrado")));
        if (!actual.isActivo()) {
            return;
        }
        if (citas.contarProgramadasEnConsultorio(id) > 0) {
            throw afectaCitas("El consultorio tiene citas programadas: no se puede dar de baja");
        }
        consultorios.store(actual.conActivo(false).aDTO());
    }

    public List<ConsultorioDTO> listarConsultorios() {
        return consultorios.listar();
    }

    // ----------------------------------------------------------------- horarios

    public HorarioDTO definirHorario(HorarioDTO d) {
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
            verificarSinSolape(nuevo, existente, "El medico ya tiene un horario que se cruza con ese");
        }
        for (HorarioDTO existente : horarios.delConsultorio(nuevo.getConsultorioId())) {
            verificarSinSolape(nuevo, existente, "El consultorio ya esta ocupado en ese horario");
        }

        HorarioMedico guardado = new HorarioMedico(horarios.nextIdentity(), nuevo.getMedicoId(),
                nuevo.getConsultorioId(), d.diaSemana(), d.horaInicio(), d.horaFin(), true);
        horarios.store(guardado.aDTO());
        return guardado.aDTO();
    }

    public List<HorarioDTO> listarHorarios() {
        return horarios.listar();
    }

    // ---------------------------------------------------------- dias no habiles

    public DiaNoHabilDTO registrarDiaNoHabil(DiaNoHabilDTO d) {
        if (d == null || !DiaNoHabil.desde(d).datosBasicosValidos()) {
            throw new DominioException("DATOS_INVALIDOS", "El dia no habil necesita fecha y descripcion");
        }
        DiaNoHabil nuevo = DiaNoHabil.desde(d);
        LocalDate hoy = reloj.ahora().toLocalDate();
        if (!nuevo.fechaFutura(hoy)) {
            throw new DominioException("VALIDACION_FECHA", "El dia no habil debe ser una fecha futura");
        }
        if (diasNoHabiles.existeActivoEnFecha(nuevo.getFecha())) {
            throw new DominioException("DIA_NO_HABIL_DUPLICADO", "Esa fecha ya esta registrada como dia no habil");
        }
        boolean hayProgramadas = citas.delDia(nuevo.getFecha()).stream()
                .anyMatch(c -> c.estado() == EstadoCita.PROGRAMADA);
        if (hayProgramadas) {
            throw afectaCitas("Hay citas programadas ese dia: no se puede marcar como no habil");
        }
        DiaNoHabilDTO guardado = nuevo.conId(diasNoHabiles.nextIdentity()).aDTO();
        DiaNoHabilDTO activo = new DiaNoHabilDTO(guardado.diaNoHabilId(), guardado.fecha(), guardado.descripcion(), true);
        diasNoHabiles.store(activo);
        return activo;
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

    private static void verificarSinSolape(HorarioMedico nuevo, HorarioDTO existente, String mensaje) {
        if (existente.activo() && nuevo.seSolapaCon(HorarioMedico.desde(existente))) {
            throw new DominioException("HORARIO_SOLAPADO", mensaje);
        }
    }

    private static DominioException noEncontrada(String mensaje) {
        return new DominioException("NO_ENCONTRADA", mensaje);
    }

    private static DominioException afectaCitas(String mensaje) {
        return new DominioException("CAMBIO_AFECTA_CITAS", mensaje);
    }
}
