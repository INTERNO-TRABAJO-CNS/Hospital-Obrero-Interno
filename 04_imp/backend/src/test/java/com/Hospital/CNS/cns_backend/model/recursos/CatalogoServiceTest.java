package com.Hospital.CNS.cns_backend.model.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** CU-02: cada flujo alternativo del diagrama ([3a] datos invalidos, [3b] afecta citas) tiene su prueba. */
class CatalogoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    private FakeMedicoRepository medicos;
    private FakeConsultorioRepository consultorios;
    private FakeHorarioRepository horarios;
    private FakeDiaNoHabilRepository dias;
    private FakeCitaRepository citas;
    private CatalogoService service;

    @BeforeEach
    void preparar() {
        medicos = new FakeMedicoRepository();
        consultorios = new FakeConsultorioRepository();
        horarios = new FakeHorarioRepository();
        dias = new FakeDiaNoHabilRepository();
        citas = new FakeCitaRepository();
        service = new CatalogoService(medicos, consultorios, horarios, dias, citas,
                new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(8, 0))));
    }

    // ------------------------------------------------------------ medicos [3a]

    @Test
    void crearMedico_ok_quedaActivoYConId() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));

        assertEquals(1, creado.medicoId());
        assertTrue(creado.activo());
        assertEquals(List.of(creado), medicos.medicos);
    }

    @Test
    void crearMedico_cupo8_rechaza() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearMedico(medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, 30, 8)));

        assertEquals("VALIDACION_CUPO", e.getCodigo());
        assertTrue(medicos.medicos.isEmpty());
    }

    @Test
    void crearMedico_cupo4_rechaza() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearMedico(medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, 30, 4)));

        assertEquals("VALIDACION_CUPO", e.getCodigo());
    }

    @Test
    void crearMedico_cupoEnLosLimites5y7_acepta() {
        service.crearMedico(medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, 30, 5));
        service.crearMedico(medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, 30, 7));

        assertEquals(2, medicos.medicos.size());
    }

    @Test
    void crearMedico_duracionFueraDeRango() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearMedico(medico(TipoEspecialidad.CONSULTORIO_FAMILIAR, 50, 6)));

        assertEquals("VALIDACION_DURACION", e.getCodigo());
        assertTrue(medicos.medicos.isEmpty());
    }

    @Test
    void crearMedico_duracionDependeDeLaEspecialidad() {
        service.crearMedico(medico(TipoEspecialidad.ODONTOLOGIA, 60, 6));
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearMedico(medico(TipoEspecialidad.ODONTOLOGIA, 44, 6)));

        assertEquals("VALIDACION_DURACION", e.getCodigo());
    }

    @Test
    void crearMedico_sinNombre_datosInvalidos() {
        MedicoDTO sinNombre = new MedicoDTO(0, " ", "Perez", TipoEspecialidad.PEDIATRIA, 30, 6, true);

        DominioException e = assertThrows(DominioException.class, () -> service.crearMedico(sinNombre));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    // -------------------------------------------------------- medicos [3b]

    @Test
    void modificarMedico_sinCitas_ok() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));

        MedicoDTO nuevo = service.modificarMedico(creado.medicoId(), medico(TipoEspecialidad.PEDIATRIA, 40, 7));

        assertEquals(40, nuevo.duracionCitaMin());
        assertEquals(7, medicos.porId(creado.medicoId()).orElseThrow().cupoMaximoDia());
    }

    @Test
    void modificarMedico_conCitasProgramadas() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));
        citas.store(cita(1, creado.medicoId(), 1, EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarMedico(creado.medicoId(), medico(TipoEspecialidad.PEDIATRIA, 40, 6)));

        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertEquals(30, medicos.porId(creado.medicoId()).orElseThrow().duracionCitaMin());
    }

    @Test
    void modificarMedico_soloElNombreConCitas_noAfectaLaAgenda() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));
        citas.store(cita(1, creado.medicoId(), 1, EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));
        MedicoDTO soloNombre = new MedicoDTO(0, "Ana Maria", "Perez", TipoEspecialidad.PEDIATRIA, 30, 6, true);

        MedicoDTO nuevo = service.modificarMedico(creado.medicoId(), soloNombre);

        assertEquals("Ana Maria", nuevo.nombres());
    }

    @Test
    void modificarMedico_citasCanceladasNoCuentan() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));
        citas.store(cita(1, creado.medicoId(), 1, EstadoCita.CANCELADA, LocalTime.of(9, 0)));

        MedicoDTO nuevo = service.modificarMedico(creado.medicoId(), medico(TipoEspecialidad.PEDIATRIA, 45, 6));

        assertEquals(45, nuevo.duracionCitaMin());
    }

    @Test
    void modificarMedico_conValoresInvalidos_noGuarda() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));

        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarMedico(creado.medicoId(), medico(TipoEspecialidad.PEDIATRIA, 30, 9)));

        assertEquals("VALIDACION_CUPO", e.getCodigo());
        assertEquals(6, medicos.porId(creado.medicoId()).orElseThrow().cupoMaximoDia());
    }

    @Test
    void modificarMedico_noExiste() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarMedico(99, medico(TipoEspecialidad.PEDIATRIA, 30, 6)));

        assertEquals("NO_ENCONTRADA", e.getCodigo());
    }

    @Test
    void darDeBajaMedico_esLogica_noBorra() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));

        service.darDeBajaMedico(creado.medicoId());

        assertEquals(1, medicos.medicos.size());
        assertFalse(medicos.porId(creado.medicoId()).orElseThrow().activo());
    }

    @Test
    void darDeBajaMedico_conCitasProgramadas() {
        MedicoDTO creado = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));
        citas.store(cita(1, creado.medicoId(), 1, EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.darDeBajaMedico(creado.medicoId()));

        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertTrue(medicos.porId(creado.medicoId()).orElseThrow().activo());
    }

    @Test
    void listarMedicos_filtraPorEspecialidad() {
        service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6));
        service.crearMedico(medico(TipoEspecialidad.ODONTOLOGIA, 50, 6));

        assertEquals(2, service.listarMedicos(null).size());
        assertEquals(1, service.listarMedicos(TipoEspecialidad.ODONTOLOGIA).size());
    }

    // ------------------------------------------------------------ consultorios

    @Test
    void crearConsultorio_ok_yDuplicadoRechaza() {
        ConsultorioDTO creado = service.crearConsultorio(new ConsultorioDTO(0, "Consultorio 1", true));

        assertEquals(1, creado.consultorioId());
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearConsultorio(new ConsultorioDTO(0, "consultorio 1", true)));
        assertEquals("CONSULTORIO_DUPLICADO", e.getCodigo());
    }

    @Test
    void crearConsultorio_sinNombre_datosInvalidos() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.crearConsultorio(new ConsultorioDTO(0, "", true)));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void darDeBajaConsultorio_logica_ySinCitas() {
        ConsultorioDTO c = service.crearConsultorio(new ConsultorioDTO(0, "Consultorio 1", true));

        service.darDeBajaConsultorio(c.consultorioId());

        assertEquals(1, consultorios.consultorios.size());
        assertFalse(consultorios.porId(c.consultorioId()).orElseThrow().activo());
    }

    @Test
    void darDeBajaConsultorio_conCitasProgramadas() {
        ConsultorioDTO c = service.crearConsultorio(new ConsultorioDTO(0, "Consultorio 1", true));
        citas.store(cita(1, 5, c.consultorioId(), EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.darDeBajaConsultorio(c.consultorioId()));

        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertTrue(consultorios.porId(c.consultorioId()).orElseThrow().activo());
    }

    @Test
    void darDeBajaConsultorio_noExiste() {
        DominioException e = assertThrows(DominioException.class, () -> service.darDeBajaConsultorio(99));

        assertEquals("NO_ENCONTRADA", e.getCodigo());
    }

    // ---------------------------------------------------------------- horarios

    @Test
    void definirHorario_ok() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();

        HorarioDTO h = service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));

        assertEquals(1, h.horarioId());
        assertTrue(h.activo());
        assertEquals(List.of(h), horarios.horarios);
    }

    @Test
    void definirHorario_solapado() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));

        DominioException e = assertThrows(DominioException.class,
                () -> service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 11, 14)));

        assertEquals("HORARIO_SOLAPADO", e.getCodigo());
        assertEquals(1, horarios.horarios.size());
    }

    @Test
    void definirHorario_consultorioOcupadoPorOtroMedico_solapado() {
        long medicoA = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        long medicoB = service.crearMedico(medico(TipoEspecialidad.ODONTOLOGIA, 50, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        service.definirHorario(horario(medicoA, consultorio, DayOfWeek.TUESDAY, 8, 12));

        DominioException e = assertThrows(DominioException.class,
                () -> service.definirHorario(horario(medicoB, consultorio, DayOfWeek.TUESDAY, 10, 13)));

        assertEquals("HORARIO_SOLAPADO", e.getCodigo());
    }

    @Test
    void definirHorario_franjasContiguasOEnOtroDia_noSolapan() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));

        service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 12, 14));
        service.definirHorario(horario(medico, consultorio, DayOfWeek.TUESDAY, 8, 12));

        assertEquals(3, horarios.horarios.size());
    }

    @Test
    void definirHorario_inicioNoAnteriorAFin_datosInvalidos() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();

        DominioException e = assertThrows(DominioException.class,
                () -> service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 12, 12)));

        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void definirHorario_medicoOConsultorioInexistente_noEncontrada() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();

        DominioException e = assertThrows(DominioException.class,
                () -> service.definirHorario(horario(medico, 77, DayOfWeek.MONDAY, 8, 12)));

        assertEquals("NO_ENCONTRADA", e.getCodigo());
    }

    // ----------------------------------------------------------- dias no habiles

    @Test
    void registrarDiaNoHabil_ok() {
        DiaNoHabilDTO d = service.registrarDiaNoHabil(
                new DiaNoHabilDTO(0, HOY.plusDays(10), "Feriado", true));

        assertEquals(1, d.diaNoHabilId());
        assertTrue(d.activo());
        assertEquals(List.of(d), dias.dias);
    }

    @Test
    void registrarDiaNoHabil_fechaHoyOPasada_rechaza() {
        DominioException hoy = assertThrows(DominioException.class,
                () -> service.registrarDiaNoHabil(new DiaNoHabilDTO(0, HOY, "Feriado", true)));
        DominioException ayer = assertThrows(DominioException.class,
                () -> service.registrarDiaNoHabil(new DiaNoHabilDTO(0, HOY.minusDays(1), "Feriado", true)));

        assertEquals("VALIDACION_FECHA", hoy.getCodigo());
        assertEquals("VALIDACION_FECHA", ayer.getCodigo());
        assertTrue(dias.dias.isEmpty());
    }

    @Test
    void registrarDiaNoHabil_duplicado_rechaza() {
        LocalDate fecha = HOY.plusDays(10);
        service.registrarDiaNoHabil(new DiaNoHabilDTO(0, fecha, "Feriado", true));

        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarDiaNoHabil(new DiaNoHabilDTO(0, fecha, "Otro", true)));

        assertEquals("DIA_NO_HABIL_DUPLICADO", e.getCodigo());
    }

    @Test
    void registrarDiaNoHabil_conCitasProgramadasEseDia_afectaCitas() {
        LocalDate fecha = HOY.plusDays(3);
        citas.store(new CitaDTO(1, 1, "Paciente", "A1", 5, "Dr", TipoEspecialidad.PEDIATRIA, 1,
                fecha, LocalTime.of(9, 0), 1, EstadoCita.PROGRAMADA));

        DominioException e = assertThrows(DominioException.class,
                () -> service.registrarDiaNoHabil(new DiaNoHabilDTO(0, fecha, "Feriado", true)));

        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertTrue(dias.dias.isEmpty());
    }

    // ------------------------------------------- modificar / dar de baja (resto)

    @Test
    void modificarConsultorio_renombra_yDuplicadoRechaza() {
        ConsultorioDTO c1 = service.crearConsultorio(new ConsultorioDTO(0, "C1", true));
        service.crearConsultorio(new ConsultorioDTO(0, "C2", true));

        ConsultorioDTO renombrado = service.modificarConsultorio(c1.consultorioId(), new ConsultorioDTO(0, "C1 bis", false));
        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarConsultorio(c1.consultorioId(), new ConsultorioDTO(0, "c2", true)));

        assertEquals("C1 bis", renombrado.nombre());
        assertTrue(renombrado.activo());
        assertEquals("CONSULTORIO_DUPLICADO", e.getCodigo());
    }

    @Test
    void modificarConsultorio_mismoNombreOtraMayuscula_ok() {
        ConsultorioDTO c = service.crearConsultorio(new ConsultorioDTO(0, "Consultorio 1", true));

        ConsultorioDTO r = service.modificarConsultorio(c.consultorioId(), new ConsultorioDTO(0, "CONSULTORIO 1", true));

        assertEquals("CONSULTORIO 1", r.nombre());
    }

    @Test
    void modificarHorario_ok_noChocaConsigoMismo() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        HorarioDTO h = service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));

        HorarioDTO m = service.modificarHorario(h.horarioId(), horario(medico, consultorio, DayOfWeek.MONDAY, 9, 13));

        assertEquals(h.horarioId(), m.horarioId());
        assertEquals(LocalTime.of(9, 0), m.horaInicio());
        assertEquals(1, horarios.horarios.size());
    }

    @Test
    void modificarHorario_conCitasProgramadas() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        HorarioDTO h = service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));
        citas.store(cita(1, medico, consultorio, EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));

        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarHorario(h.horarioId(), horario(medico, consultorio, DayOfWeek.MONDAY, 9, 13)));

        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertEquals(LocalTime.of(8, 0), horarios.porId(h.horarioId()).orElseThrow().horaInicio());
    }

    @Test
    void modificarHorario_solapadoConOtro() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));
        HorarioDTO tarde = service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 14, 16));

        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarHorario(tarde.horarioId(), horario(medico, consultorio, DayOfWeek.MONDAY, 11, 15)));

        assertEquals("HORARIO_SOLAPADO", e.getCodigo());
    }

    @Test
    void darDeBajaHorario_logica_yConCitasRechaza() {
        long medico = service.crearMedico(medico(TipoEspecialidad.PEDIATRIA, 30, 6)).medicoId();
        int consultorio = service.crearConsultorio(new ConsultorioDTO(0, "C1", true)).consultorioId();
        HorarioDTO lunes = service.definirHorario(horario(medico, consultorio, DayOfWeek.MONDAY, 8, 12));
        HorarioDTO martes = service.definirHorario(horario(medico, consultorio, DayOfWeek.TUESDAY, 8, 12));

        service.darDeBajaHorario(lunes.horarioId());
        citas.store(cita(1, medico, consultorio, EstadoCita.PROGRAMADA, LocalTime.of(9, 0)));
        DominioException e = assertThrows(DominioException.class, () -> service.darDeBajaHorario(martes.horarioId()));

        assertEquals(2, horarios.horarios.size());
        assertFalse(horarios.porId(lunes.horarioId()).orElseThrow().activo());
        assertEquals("CAMBIO_AFECTA_CITAS", e.getCodigo());
        assertTrue(horarios.porId(martes.horarioId()).orElseThrow().activo());
    }

    @Test
    void modificarDiaNoHabil_soloDescripcion_ok_yNuevaFechaPasadaRechaza() {
        DiaNoHabilDTO d = service.registrarDiaNoHabil(new DiaNoHabilDTO(0, HOY.plusDays(10), "Feriado", true));

        DiaNoHabilDTO m = service.modificarDiaNoHabil(d.diaNoHabilId(), new DiaNoHabilDTO(0, HOY.plusDays(10), "Asueto", true));
        DominioException e = assertThrows(DominioException.class,
                () -> service.modificarDiaNoHabil(d.diaNoHabilId(), new DiaNoHabilDTO(0, HOY, "Asueto", true)));

        assertEquals("Asueto", m.descripcion());
        assertEquals("VALIDACION_FECHA", e.getCodigo());
    }

    @Test
    void darDeBajaDiaNoHabil_esLogica() {
        DiaNoHabilDTO d = service.registrarDiaNoHabil(new DiaNoHabilDTO(0, HOY.plusDays(10), "Feriado", true));

        service.darDeBajaDiaNoHabil(d.diaNoHabilId());

        assertEquals(1, dias.dias.size());
        assertFalse(dias.porId(d.diaNoHabilId()).orElseThrow().activo());
    }

    @Test
    void darDeBajaDiaNoHabil_noExiste() {
        DominioException e = assertThrows(DominioException.class, () -> service.darDeBajaDiaNoHabil(99));

        assertEquals("NO_ENCONTRADA", e.getCodigo());
    }

    // ----------------------------------------------------------------- ayudas

    private static MedicoDTO medico(TipoEspecialidad especialidad, int duracion, int cupo) {
        return new MedicoDTO(0, "Juan", "Perez", especialidad, duracion, cupo, true);
    }

    private static HorarioDTO horario(long medicoId, int consultorioId, DayOfWeek dia, int desde, int hasta) {
        return new HorarioDTO(0, medicoId, consultorioId, dia, LocalTime.of(desde, 0), LocalTime.of(hasta, 0), true);
    }

    private static CitaDTO cita(long id, long medicoId, int consultorio, EstadoCita estado, LocalTime hora) {
        return new CitaDTO(id, 1, "Paciente", "A1", medicoId, "Dr", TipoEspecialidad.PEDIATRIA, consultorio,
                HOY.plusDays(1), hora, 1, estado);
    }
}
