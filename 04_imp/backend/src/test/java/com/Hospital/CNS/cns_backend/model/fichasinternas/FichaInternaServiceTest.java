package com.Hospital.CNS.cns_backend.model.fichasinternas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeHorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;

/** CU-21/22/23/25 con Fakes: lunes 16/11/2026 a las 09:00. */
class FichaInternaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final LocalDate MANANA = HOY.plusDays(1);
    private static final long RAMOS = 1;
    private static final long QUISPE = 2;
    private static final long ODONTO = 3;
    private static final long VARGAS = 10;
    private static final long ARCE = 11;
    private static final long LIMA = 20;
    private static final long LIMA_2 = 21;
    private static final long ELENA = 100;
    private static final long NINO = 101;
    private static final long ABUELO = 102;
    private static final long CITA_HOY = 500;

    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(9, 0)));
    private final FakeFichaInternaRepository fichas = new FakeFichaInternaRepository();
    private final FakeCitaRepository citas = new FakeCitaRepository();
    private final FakeMedicoRepository medicos = new FakeMedicoRepository();
    private final FakeHorarioRepository horarios = new FakeHorarioRepository();
    private final FakeConsultorioRepository consultorios = new FakeConsultorioRepository();
    private final FakePacienteRepository pacientes = new FakePacienteRepository();
    private final FakeNotificacionPublicador avisos = new FakeNotificacionPublicador();
    private final List<String> cambios = new ArrayList<>();
    private FichaInternaService service;

    @BeforeEach
    void preparar() {
        medico(RAMOS, "Carlos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 7);
        medico(QUISPE, "Lucia", TipoEspecialidad.PEDIATRIA, 35, 6);
        medico(ODONTO, "Jorge", TipoEspecialidad.ODONTOLOGIA, 50, 5);
        medico(VARGAS, "Raul", TipoEspecialidad.GASTROENTEROLOGIA, 25, 6);
        medico(ARCE, "Patricia", TipoEspecialidad.GASTROENTEROLOGIA, 25, 5);
        medico(LIMA, "Hugo", TipoEspecialidad.TERAPIA_INTENSIVA, 45, 5);
        consultorios.store(new ConsultorioDTO(12, "Consultorio 12", true));
        consultorios.store(new ConsultorioDTO(14, "Consultorio 14", true));
        horarioLunesAViernes(VARGAS, 12, "08:00", "10:00");
        horarioLunesAViernes(ARCE, 14, "08:00", "10:00");
        paciente(ELENA, "Elena", LocalDate.of(1990, 2, 10));
        paciente(NINO, "Sofia", LocalDate.of(2020, 5, 1));
        paciente(ABUELO, "Pedro", LocalDate.of(1950, 1, 1));
        citas.store(cita(CITA_HOY, ELENA, RAMOS, HOY, "09:30", EstadoCita.ASISTENCIA_CONFIRMADA));
        AsignadorFichaInterna asignador = new AsignadorFichaInterna(fichas, medicos, horarios, consultorios, citas,
                new FakeDiaNoHabilRepository(), null, reloj);
        service = new FichaInternaService(fichas, citas, medicos, pacientes, asignador,
                (objeto, id, accion, usuario, motivo) -> cambios.add(objeto + " " + id + " " + accion), avisos,
                Transaccion.DIRECTA, reloj);
    }

    @Test
    void generar_desdeCitaDeHoy_asignaManana_conElEspecialistaDeMenosFichas() {
        FichaInternaVistaDTO f = service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), RAMOS);

        assertEquals(1001, f.ticket());
        assertEquals(EstadoFichaInterna.ASIGNADA, f.estado());
        assertEquals(MANANA, f.fecha());
        assertEquals("08:00", f.hora());
        assertEquals("Raul Prueba", f.medico());
        assertEquals("Consultorio 12", f.consultorio());
        assertEquals("GENERAL", f.tabla());
        assertEquals(Gravedad.MEDIA, f.gravedad(), "el medico que deriva si ve la gravedad");
        assertEquals(1001L, fichas.derivaciones.get(CITA_HOY));
        assertEquals("FICHA_INTERNA_ASIGNADA", avisos.publicadas.get(0).tipo());
        assertTrue(!avisos.publicadas.get(0).mensaje().contains("MEDIA"), "la gravedad no va en el aviso");
    }

    @Test
    void generar_odontologo_MEDICO_NO_DERIVA() {
        assertCodigo("MEDICO_NO_DERIVA", () -> service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), ODONTO));
    }

    @Test
    void generar_citaAjena_o_deAyer_CITA_NO_VALIDA() {
        citas.store(cita(501, ABUELO, RAMOS, HOY.minusDays(1), "09:00", EstadoCita.ATENDIDA));

        assertCodigo("CITA_NO_VALIDA", () -> service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), QUISPE));
        assertCodigo("CITA_NO_VALIDA", () -> service.generar(solicitud(501L, null, Gravedad.MEDIA, false), RAMOS));
    }

    @Test
    void generar_dosVecesMismaCita_CITA_YA_DERIVADA() {
        service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), RAMOS);

        assertCodigo("CITA_YA_DERIVADA", () -> service.generar(solicitud(CITA_HOY, null, Gravedad.ALTA, false), RAMOS));
    }

    @Test
    void generar_pediatra_paciente10anios_EDAD_NO_PEDIATRICA() {
        assertCodigo("EDAD_NO_PEDIATRICA", () -> service.generar(urgencia(ELENA, TipoEspecialidad.GASTROENTEROLOGIA), QUISPE));
    }

    @Test
    void generar_terapiaSinUrgencia_TERAPIA_SOLO_URGENCIA() {
        SolicitudFichaInterna s = new SolicitudFichaInterna(CITA_HOY, null, TipoEspecialidad.TERAPIA_INTENSIVA,
                Gravedad.ALTA, false, "Paro");

        assertCodigo("TERAPIA_SOLO_URGENCIA", () -> service.generar(s, RAMOS));
    }

    @Test
    void generar_motivoVacio_y_pacienteConFichaActiva() {
        assertCodigo("MOTIVO_INVALIDO", () -> service.generar(
                new SolicitudFichaInterna(CITA_HOY, null, TipoEspecialidad.CARDIOLOGIA, Gravedad.BAJA, false, "  "),
                RAMOS));
        service.generar(urgencia(ABUELO, TipoEspecialidad.GASTROENTEROLOGIA), RAMOS);

        assertCodigo("PACIENTE_CON_FICHA_ACTIVA",
                () -> service.generar(urgencia(ABUELO, TipoEspecialidad.GASTROENTEROLOGIA), RAMOS));
    }

    @Test
    void pediatra_derivaAlaTablaPediatrica() {
        FichaInternaVistaDTO f = service.generar(urgencia(NINO, TipoEspecialidad.GASTROENTEROLOGIA), QUISPE);

        assertEquals("PEDIATRIA", f.tabla());
    }

    @Test
    void asigna_pacienteConCitaComunCruzada_saltaEseHorario() {
        citas.store(cita(502, ELENA, RAMOS, MANANA, "08:00", EstadoCita.PROGRAMADA));

        FichaInternaVistaDTO f = service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), RAMOS);

        assertEquals(MANANA, f.fecha());
        assertEquals("08:50", f.hora(), "08:00 y 08:25 cruzan con la cita de 08:00 a 08:40");
    }

    @Test
    void asigna_respetaCupo_contandoGeneralYPediatrica() {
        medicos.store(new MedicoDTO(ARCE, "Patricia", "Prueba", TipoEspecialidad.GASTROENTEROLOGIA, 25, 5, false));
        medicos.store(new MedicoDTO(VARGAS, "Raul", "Prueba", TipoEspecialidad.GASTROENTEROLOGIA, 25, 5, true));
        horarios.horarios.clear();
        horarioLunesAViernes(VARGAS, 12, "08:00", "12:00");
        for (int i = 0; i < 4; i++) {
            fichas.insertar(asignada(2000 + i, 300 + i, MANANA, LocalTime.of(8, 0).plusMinutes(25L * i), false,
                    Gravedad.ALTA));
        }
        fichas.insertar(asignada(2010, 310, MANANA, LocalTime.of(10, 0), true, Gravedad.ALTA));

        FichaInternaVistaDTO f = service.generar(solicitud(CITA_HOY, null, Gravedad.MEDIA, false), RAMOS);

        assertEquals(MANANA.plusDays(1), f.fecha(), "con 5 fichas (4 generales + 1 pediatrica) el cupo esta lleno");
    }

    @Test
    void sinHueco15dias_desplazaLaDeMenorPrioridad_yLaDesplazadaQuedaEnEspera() {
        llenarQuinceDias(Gravedad.BAJA);

        FichaInternaVistaDTO f = service.generar(solicitud(CITA_HOY, null, Gravedad.ALTA, false), RAMOS);

        assertEquals(EstadoFichaInterna.ASIGNADA, f.estado());
        FichaInternaDTO desplazada = fichas.porId(3014).orElseThrow();
        assertEquals(EstadoFichaInterna.EN_ESPERA, desplazada.estado(), "la ultima en llegar con BAJA pierde su lugar");
        assertNull(desplazada.fecha());
        assertTrue(avisos.publicadas.stream().anyMatch(n -> n.tipo().equals("FICHA_INTERNA_REPROGRAMADA")));
    }

    @Test
    void sinHueco_yTodasDeMayorPrioridad_quedaEnEspera() {
        llenarQuinceDias(Gravedad.ALTA);

        FichaInternaVistaDTO f = service.generar(solicitud(CITA_HOY, null, Gravedad.BAJA, false), RAMOS);

        assertEquals(EstadoFichaInterna.EN_ESPERA, f.estado());
        assertEquals("FICHA_INTERNA_EN_ESPERA", avisos.publicadas.get(0).tipo());
    }

    @Test
    void urgencia_hoyDesplazaALaNoUrgenteQueNoEmpezo() {
        horarios.horarios.clear();
        horarioLunesAViernes(VARGAS, 12, "09:00", "09:50");
        fichas.insertar(asignada(4000, 400, HOY, LocalTime.of(9, 0), false, Gravedad.ALTA));
        fichas.insertar(asignada(4001, 401, HOY, LocalTime.of(9, 25), false, Gravedad.BAJA));

        FichaInternaVistaDTO f = service.generar(urgencia(ABUELO, TipoEspecialidad.GASTROENTEROLOGIA), RAMOS);

        assertEquals(HOY, f.fecha());
        assertEquals("09:25", f.hora());
        FichaInternaDTO desplazada = fichas.porId(4001).orElseThrow();
        assertEquals(MANANA, desplazada.fecha(), "la desplazada busca el siguiente hueco libre");
        assertEquals(EstadoFichaInterna.ASIGNADA, fichas.porId(4000).orElseThrow().estado(), "la de 09:00 ya empezo");
    }

    @Test
    void terapiaIntensiva_asignaAhora_alMedicoQueSeLiberaAntes() {
        medico(LIMA_2, "Ana", TipoEspecialidad.TERAPIA_INTENSIVA, 45, 5);
        FichaInternaDTO reanimacion = new FichaInternaDTO(5000, TipoEspecialidad.TERAPIA_INTENSIVA, false, 500, "X",
                "", null, null, RAMOS, "", TipoEspecialidad.CONSULTORIO_FAMILIAR, LIMA, "Hugo", null, null, HOY,
                LocalTime.of(8, 30), 45, Gravedad.ALTA, true, false, 5000, "Paro", null, EstadoFichaInterna.ASIGNADA,
                reloj.ahora());
        fichas.insertar(reanimacion);

        FichaInternaVistaDTO f = service.generar(urgencia(ABUELO, TipoEspecialidad.TERAPIA_INTENSIVA), RAMOS);

        assertEquals("Ana Prueba", f.medico(), "Hugo esta ocupado hasta las 09:15");
        assertEquals("09:00", f.hora());
    }

    @Test
    void procesarEnEspera_enOrdenDePrioridad_sinDesplazar() {
        horarios.horarios.clear();
        medicos.store(new MedicoDTO(ARCE, "Patricia", "Prueba", TipoEspecialidad.GASTROENTEROLOGIA, 25, 5, false));
        FichaInternaDTO baja = asignada(6000, 600, null, null, false, Gravedad.BAJA).enEspera();
        FichaInternaDTO alta = asignada(6001, 601, null, null, false, Gravedad.ALTA).enEspera();
        fichas.insertar(baja);
        fichas.insertar(alta);
        horarioLunesAViernes(VARGAS, 12, "08:00", "08:25");

        assertEquals(2, service.procesarEnEspera());
        assertEquals(MANANA, fichas.porId(6001).orElseThrow().fecha(), "la ALTA elige primero");
        assertEquals(MANANA.plusDays(1), fichas.porId(6000).orElseThrow().fecha());
    }

    @Test
    void especialista_noVeLaGravedad_y_noSePresentoSoloDespuesDeLaHora() {
        fichas.insertar(asignada(7000, 700, HOY, LocalTime.of(10, 0), false, Gravedad.ALTA));

        List<FichaInternaVistaDTO> agenda = service.agendaEspecialista(VARGAS, HOY);

        assertNull(agenda.get(0).gravedad());
        assertCodigo("TODAVIA_NO_ES_LA_HORA", () -> service.noSePresento(7000, VARGAS));
        assertCodigo("NO_ES_DE_SU_AGENDA", () -> service.marcarAtendida(7000, ARCE, null));
        assertEquals(EstadoFichaInterna.ATENDIDA, service.marcarAtendida(7000, VARGAS, " Ulcera ").estado());
    }

    @Test
    void cola_ordenadaPorPrioridad() {
        fichas.insertar(asignada(8000, 800, MANANA, LocalTime.of(8, 0), false, Gravedad.BAJA));
        fichas.insertar(asignada(8001, 801, MANANA, LocalTime.of(8, 25), false, Gravedad.ALTA));
        fichas.insertar(asignada(8002, 802, MANANA, LocalTime.of(8, 50), false, Gravedad.MEDIA)); // urgente

        List<FichaInternaVistaDTO> cola = service.cola(TipoEspecialidad.GASTROENTEROLOGIA, false, RAMOS);

        assertEquals(List.of(8002L, 8001L, 8000L), cola.stream().map(FichaInternaVistaDTO::ticket).toList());
        assertCodigo("MEDICO_NO_DERIVA", () -> service.cola(TipoEspecialidad.GASTROENTEROLOGIA, false, VARGAS));
    }

    // ---------------------------------------------------------------------------------------------------------

    /** Solo Vargas, una franja de 25 min por dia y las 15 fechas habiles ocupadas por otros pacientes. */
    private void llenarQuinceDias(Gravedad gravedad) {
        medicos.store(new MedicoDTO(ARCE, "Patricia", "Prueba", TipoEspecialidad.GASTROENTEROLOGIA, 25, 5, false));
        horarios.horarios.clear();
        horarioLunesAViernes(VARGAS, 12, "08:00", "08:25");
        LocalDate dia = HOY;
        for (int i = 0; i < 15; i++) {
            do {
                dia = dia.plusDays(1);
            } while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY);
            fichas.insertar(asignada(3000 + i, 300 + i, dia, LocalTime.of(8, 0), false, gravedad));
        }
    }

    private FichaInternaDTO asignada(long id, long pacienteId, LocalDate fecha, LocalTime hora, boolean pediatrica,
                                     Gravedad gravedad) {
        boolean urgente = id == 8002;
        return new FichaInternaDTO(id, TipoEspecialidad.GASTROENTEROLOGIA, pediatrica, pacienteId, "P" + pacienteId, "",
                null, null, RAMOS, "Carlos Prueba", TipoEspecialidad.CONSULTORIO_FAMILIAR, fecha == null ? null : VARGAS,
                fecha == null ? null : "Raul Prueba", fecha == null ? null : 12, null, fecha, hora, 25, gravedad,
                urgente, false, id, "Motivo", null,
                fecha == null ? EstadoFichaInterna.EN_ESPERA : EstadoFichaInterna.ASIGNADA, reloj.ahora());
    }

    private void medico(long id, String nombre, TipoEspecialidad esp, int duracion, int cupo) {
        medicos.store(new MedicoDTO(id, nombre, "Prueba", esp, duracion, cupo, true));
    }

    private void horarioLunesAViernes(long medicoId, int consultorio, String desde, String hasta) {
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY)) {
            horarios.store(new HorarioDTO(horarios.nextIdentity(), medicoId, consultorio, d, LocalTime.parse(desde),
                    LocalTime.parse(hasta), true));
        }
    }

    private void paciente(long id, String nombre, LocalDate nacimiento) {
        pacientes.store(new PacienteDTO(id, "AF-" + id, "CI-" + id, nombre, "Prueba", nacimiento, true));
    }

    private static CitaDTO cita(long id, long paciente, long medico, LocalDate fecha, String hora, EstadoCita estado) {
        return new CitaDTO(id, paciente, "P", "AF", medico, "M", TipoEspecialidad.CONSULTORIO_FAMILIAR, 1, fecha,
                LocalTime.parse(hora), 1, estado);
    }

    private static SolicitudFichaInterna solicitud(Long cita, Long paciente, Gravedad gravedad, boolean urgencia) {
        return new SolicitudFichaInterna(cita, paciente, TipoEspecialidad.GASTROENTEROLOGIA, gravedad, urgencia,
                "Dolor abdominal persistente");
    }

    private static SolicitudFichaInterna urgencia(long paciente, TipoEspecialidad esp) {
        return new SolicitudFichaInterna(null, paciente, esp, Gravedad.ALTA, true, "Sangrado digestivo");
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        DominioException e = assertThrows(DominioException.class, accion::run);
        assertEquals(codigo, e.getCodigo());
    }
}
