package com.Hospital.CNS.cns_backend.model.cirugias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoFichaInterna;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeNotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Gravedad;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FakeFichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaDTO;
import com.Hospital.CNS.cns_backend.model.recursos.FakeDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeMedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.FakeQuirofanoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoDTO;

/** CU-24 con Fakes (el FakeCirugiaRepository rechaza los mismos choques que la base). Lunes 16/11/2026, 09:00. */
class ProgramacionCirugiaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final LocalDate MANANA = HOY.plusDays(1);
    private static final long RAMOS = 1;
    private static final long VARGAS = 10;

    private final RelojFijo reloj = new RelojFijo(LocalDateTime.of(HOY, LocalTime.of(9, 0)));
    private final FakeCirugiaRepository cirugias = new FakeCirugiaRepository();
    private final FakeFichaInternaRepository fichas = new FakeFichaInternaRepository();
    private final FakeQuirofanoRepository quirofanos = new FakeQuirofanoRepository();
    private final FakeMedicoRepository medicos = new FakeMedicoRepository();
    private final FakeNotificacionPublicador avisos = new FakeNotificacionPublicador();
    private ProgramacionCirugiaService service;

    @BeforeEach
    void preparar() {
        medicos.store(new MedicoDTO(RAMOS, "Carlos", "Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 7, true));
        medicos.store(new MedicoDTO(VARGAS, "Raul", "Vargas", TipoEspecialidad.GASTROENTEROLOGIA, 25, 6, true));
        quirofanos.store(quirofano(1, "G-1", 40));
        quirofanos.store(quirofano(2, "G-2", 30));
        fichas.insertar(ficha(1019, 110, EstadoFichaInterna.ATENDIDA, HOY, LocalTime.of(8, 0)));
        fichas.insertar(ficha(1024, 113, EstadoFichaInterna.ASIGNADA, HOY, LocalTime.of(10, 20)));
        service = new ProgramacionCirugiaService(cirugias, fichas, quirofanos, medicos, new FakeCitaRepository(),
                new FakeDiaNoHabilRepository(), (objeto, id, accion, usuario, motivo) -> { }, avisos,
                Transaccion.DIRECTA, reloj);
    }

    @Test
    void proponeElPrimerHuecoDesdeManana_yConfirma() {
        CirugiaDTOs.Solicitud s = new CirugiaDTOs.Solicitud(1019, 90, Gravedad.ALTA, false);

        CirugiaDTOs.Propuesta p = service.proponer(s, VARGAS);

        assertEquals("G-1", p.quirofano());
        assertEquals(MANANA, p.fecha());
        assertEquals("07:00", p.inicio());
        assertEquals("08:30", p.fin());
        assertEquals("09:10", p.limpiezaHasta(), "40 min de limpieza del G-1");
        assertNull(p.desplaza());
        assertTrue(cirugias.cirugias.isEmpty(), "proponer no guarda nada");

        CirugiaDTOs.Vista c = service.confirmar(confirmacion(s, p), VARGAS);

        assertEquals(58, c.numero());
        assertEquals(EstadoCirugia.PROGRAMADA, c.estado());
        assertEquals("CIRUGIA_PROGRAMADA", avisos.publicadas.get(0).tipo());
    }

    @Test
    void unaPropuestaViejaQueYaSeOcupo_CHOQUE_DE_HORARIO() {
        CirugiaDTOs.Solicitud s = new CirugiaDTOs.Solicitud(1019, 90, Gravedad.ALTA, false);
        CirugiaDTOs.Propuesta p = service.proponer(s, VARGAS);
        service.confirmar(confirmacion(s, p), VARGAS);

        assertCodigo("CHOQUE_DE_HORARIO", () -> service.confirmar(confirmacion(s, p), VARGAS));
    }

    @Test
    void rechazos_duracion_fichaNoAtendida_yEspecialidadQueNoOpera() {
        assertCodigo("DURACION_FUERA_DE_RANGO",
                () -> service.proponer(new CirugiaDTOs.Solicitud(1019, 200, Gravedad.ALTA, false), VARGAS));
        assertCodigo("FICHA_NO_ATENDIDA",
                () -> service.proponer(new CirugiaDTOs.Solicitud(1024, 60, Gravedad.ALTA, false), VARGAS));
        assertCodigo("ESPECIALIDAD_NO_OPERA",
                () -> service.proponer(new CirugiaDTOs.Solicitud(1019, 60, Gravedad.ALTA, false), RAMOS));
    }

    @Test
    void elCirujanoConUnaFichaInternaAsignada_noOperaAEsaHora() {
        fichas.insertar(ficha(2000, 200, EstadoFichaInterna.ASIGNADA, MANANA, LocalTime.of(7, 0)));

        CirugiaDTOs.Propuesta p = service.proponer(new CirugiaDTOs.Solicitud(1019, 60, Gravedad.MEDIA, false), VARGAS);

        assertEquals("07:30", p.inicio(), "de 07:00 a 07:25 atiende una ficha interna");
    }

    @Test
    void urgenciaSinHuecoHoy_desplazaALaNoUrgenteQueNoEmpezo_yLaReprogramaAlConfirmar() {
        cirugias.cirugias.add(cirugia(1, 1, 30, 901, LocalTime.of(9, 0), 840, true));
        cirugias.cirugias.add(cirugia(2, 2, 31, 902, LocalTime.of(9, 0), 180, false));
        cirugias.cirugias.add(cirugia(57, 2, 32, 903, LocalTime.of(12, 30), 630, false));
        CirugiaDTOs.Solicitud s = new CirugiaDTOs.Solicitud(1019, 60, Gravedad.ALTA, true);

        CirugiaDTOs.Propuesta p = service.proponer(s, VARGAS);

        assertEquals(57, p.desplaza().cirugia().numero());
        assertEquals("G-2", p.quirofano());
        assertEquals(HOY, p.fecha());
        assertEquals("12:30", p.inicio());
        assertEquals("13:30", p.fin());
        assertEquals(MANANA, p.desplaza().nuevaFecha());

        service.confirmar(confirmacion(s, p), VARGAS);

        CirugiaDTO movida = cirugias.porId(57).orElseThrow();
        assertEquals(LocalDateTime.of(MANANA, LocalTime.parse(p.desplaza().nuevoInicio())), movida.inicio());
        assertTrue(avisos.publicadas.stream().anyMatch(n -> n.tipo().equals("CIRUGIA_REPROGRAMADA")));
    }

    @Test
    void realizada_soloElCirujano() {
        CirugiaDTOs.Solicitud s = new CirugiaDTOs.Solicitud(1019, 90, Gravedad.ALTA, false);
        CirugiaDTOs.Vista c = service.confirmar(confirmacion(s, service.proponer(s, VARGAS)), VARGAS);

        assertCodigo("NO_ES_DE_SU_AGENDA", () -> service.marcar(c.numero(), RAMOS, EstadoCirugia.REALIZADA));
        assertEquals(EstadoCirugia.REALIZADA, service.marcar(c.numero(), VARGAS, EstadoCirugia.REALIZADA).estado());
        assertCodigo("ESTADO_NO_PERMITE", () -> service.marcar(c.numero(), VARGAS, EstadoCirugia.CANCELADA));
    }

    // ---------------------------------------------------------------------------------------------------------

    private static CirugiaDTOs.Confirmacion confirmacion(CirugiaDTOs.Solicitud s, CirugiaDTOs.Propuesta p) {
        CirugiaDTOs.Desplazamiento d = p.desplaza();
        return new CirugiaDTOs.Confirmacion(s.fichaTicket(), s.duracionMin(), s.gravedad(), s.urgente(),
                p.quirofanoId(), p.fecha(), LocalTime.parse(p.inicio()), d == null ? null : d.cirugia().numero(),
                d == null ? null : d.nuevaFecha(), d == null ? null : LocalTime.parse(d.nuevoInicio()));
    }

    private static QuirofanoDTO quirofano(int id, String nombre, int limpieza) {
        return new QuirofanoDTO(id, nombre, TipoEspecialidad.GASTROENTEROLOGIA, LocalTime.of(7, 0), LocalTime.of(19, 0),
                limpieza, true);
    }

    private FichaInternaDTO ficha(long id, long paciente, EstadoFichaInterna estado, LocalDate fecha, LocalTime hora) {
        return new FichaInternaDTO(id, TipoEspecialidad.GASTROENTEROLOGIA, false, paciente, "P" + paciente, "",
                LocalDate.of(1960, 1, 1), null, RAMOS, "Carlos Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, VARGAS,
                "Raul Vargas", 12, "Consultorio 12", fecha, hora, 25, Gravedad.MEDIA, false, true, id, "Motivo", null,
                estado, reloj.ahora());
    }

    private static CirugiaDTO cirugia(long id, int quirofano, long cirujano, long paciente, LocalTime inicio,
                                      int duracion, boolean urgente) {
        return new CirugiaDTO(id, 0, paciente, "P" + paciente, "", null, cirujano, "C" + cirujano, quirofano,
                quirofano == 1 ? "G-1" : "G-2", TipoEspecialidad.GASTROENTEROLOGIA, LocalDateTime.of(HOY, inicio),
                duracion, quirofano == 1 ? 40 : 30, urgente ? Gravedad.ALTA : Gravedad.BAJA, urgente, false, id,
                EstadoCirugia.PROGRAMADA, LocalDateTime.of(HOY, LocalTime.of(7, 0)));
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        DominioException e = assertThrows(DominioException.class, accion::run);
        assertEquals(codigo, e.getCodigo());
    }
}
