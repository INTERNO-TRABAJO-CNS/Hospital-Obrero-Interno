package com.Hospital.CNS.cns_backend.model.reportes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** CU-19/CU-20: SupervisionService y ReporteService con FakeReporteRepository (sin BD). */
class ReporteServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 23); // lunes
    private static final LocalDate VIERNES = HOY.plusDays(4);

    private final FakeReporteRepository repo = new FakeReporteRepository();
    private final RelojFijo reloj = new RelojFijo(HOY.atTime(10, 0));
    private final SupervisionService supervision = new SupervisionService(repo, reloj);
    private final ReporteService reportes = new ReporteService(repo, reloj);

    private static ResumenEspecialidadDTO medico(long id, String nombre, TipoEspecialidad e, int fichas) {
        return new ResumenEspecialidadDTO(e, id, nombre, LocalTime.of(8, 0), LocalTime.of(12, 0), 7, fichas,
                fichas, 0, 0, 0);
    }

    private static ReporteRepository.AtendidasDia dia(long medicoId, String nombre, LocalDate fecha, int atendidas) {
        return new ReporteRepository.AtendidasDia(medicoId, nombre, TipoEspecialidad.CONSULTORIO_FAMILIAR, fecha,
                atendidas);
    }

    // ---------- CU-19: agenda de supervision ----------

    @Test
    void resumenDelDia_agrupaPorEspecialidad() {
        repo.resumenes.put(HOY, List.of(
                medico(3, "Dra. Quispe", TipoEspecialidad.PEDIATRIA, 2),
                medico(1, "Dr. Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 4),
                medico(2, "Dr. Mamani", TipoEspecialidad.ODONTOLOGIA, 2),
                medico(4, "Dr. Arce", TipoEspecialidad.CONSULTORIO_FAMILIAR, 6)));

        List<ResumenEspecialidadDTO> r = supervision.resumenDelDia(HOY);

        assertEquals(List.of(TipoEspecialidad.CONSULTORIO_FAMILIAR, TipoEspecialidad.CONSULTORIO_FAMILIAR,
                        TipoEspecialidad.ODONTOLOGIA, TipoEspecialidad.PEDIATRIA),
                r.stream().map(ResumenEspecialidadDTO::especialidad).toList());
        assertEquals(List.of("Dr. Arce", "Dr. Ramos"), List.of(r.get(0).nombreMedico(), r.get(1).nombreMedico()));
    }

    @Test
    void resumenDelDia_soloEspecialidadesBasicas() {
        repo.resumenes.put(HOY, List.of(
                medico(1, "Dr. Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 4),
                medico(9, "Dr. Vargas", TipoEspecialidad.GASTROENTEROLOGIA, 0)));

        assertEquals(List.of(1L), supervision.resumenDelDia(HOY).stream().map(ResumenEspecialidadDTO::medicoId).toList());
    }

    @Test
    void resumenDelDia_sinFechaUsaHoy() {
        repo.resumenes.put(HOY, List.of(medico(1, "Dr. Ramos", TipoEspecialidad.CONSULTORIO_FAMILIAR, 4)));

        assertEquals(1, supervision.resumenDelDia(null).size());
    }

    /** [3a] Sin datos para el criterio: lista vacia (la pantalla informa que no hay informacion). */
    @Test
    void resumenDelDia_sinDatos_listaVacia() {
        assertTrue(supervision.resumenDelDia(HOY.plusDays(1)).isEmpty());
    }

    @Test
    void fichasDeMedico_ordenadasPorHorario() {
        repo.fichasDe(1, HOY, List.of(
                new FichaSupervisionDTO(12, 3, LocalTime.of(9, 30), EstadoCita.PROGRAMADA, 4, "Ana"),
                new FichaSupervisionDTO(10, 1, LocalTime.of(8, 0), EstadoCita.ATENDIDA, 4, "Luis"),
                new FichaSupervisionDTO(11, 2, LocalTime.of(8, 45), EstadoCita.ASISTENCIA_CONFIRMADA, 4, "Rosa")));

        assertEquals(List.of(1, 2, 3),
                supervision.fichasDeMedico(1, HOY).stream().map(FichaSupervisionDTO::numeroFicha).toList());
    }

    // ---------- CU-19: reportes ----------

    @Test
    void porEstado_devuelveLosCuatroEstados() {
        repo.estados.add(new ReporteEstadoDTO(EstadoCita.CANCELADA, 9, 4));
        repo.estados.add(new ReporteEstadoDTO(EstadoCita.ATENDIDA, 61, 0));

        List<ReporteEstadoDTO> r = reportes.porEstado(HOY, VIERNES);

        assertEquals(List.of(EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA, EstadoCita.ATENDIDA,
                EstadoCita.CANCELADA), r.stream().map(ReporteEstadoDTO::estado).toList());
        assertEquals(0, r.get(0).cantidad());
        assertEquals(61, r.get(2).cantidad());
        assertEquals(4, r.get(3).automaticas());
    }

    @Test
    void porEspecialidad_incluyeLasBasicasSinSolicitudes() {
        repo.solicitudes.add(new ReporteGrupoDTO("PEDIATRIA", 26));

        List<ReporteGrupoDTO> r = reportes.porEspecialidad(HOY, VIERNES);

        assertEquals(List.of("CONSULTORIO_FAMILIAR", "ODONTOLOGIA", "PEDIATRIA"),
                r.stream().map(ReporteGrupoDTO::grupo).toList());
        assertEquals(List.of(0, 0, 26), r.stream().map(ReporteGrupoDTO::cantidad).toList());
    }

    @Test
    void periodoInvalido_datosInvalidos() {
        DominioException e = assertThrows(DominioException.class, () -> reportes.porEstado(VIERNES, HOY));
        assertEquals("DATOS_INVALIDOS", e.getCodigo());
        assertEquals("DATOS_INVALIDOS",
                assertThrows(DominioException.class, () -> reportes.cumplimiento(null, HOY)).getCodigo());
    }

    // ---------- CU-20: cumplimiento e indicadores ----------

    @Test
    void cumplimiento_marcaMenosDe5() {
        LocalDate ayer = HOY.minusDays(3); // viernes anterior
        repo.atendidas.add(dia(1, "Dr. Ramos", ayer, 7));
        repo.atendidas.add(dia(1, "Dr. Ramos", HOY, 6));
        repo.atendidas.add(dia(2, "Dr. Mamani", ayer, 4));
        repo.atendidas.add(dia(2, "Dr. Mamani", HOY, 4));
        repo.atendidas.add(dia(3, "Dra. Salas", ayer, 5));

        List<CumplimientoMedicoDTO> r = reportes.cumplimiento(ayer, HOY);

        CumplimientoMedicoDTO mamani = r.stream().filter(c -> c.medicoId() == 2).findFirst().orElseThrow();
        assertTrue(mamani.bajoMeta());
        assertEquals(4.0, mamani.promedioDiario());
        assertEquals(2, mamani.diasBajoMeta());

        CumplimientoMedicoDTO ramos = r.stream().filter(c -> c.medicoId() == 1).findFirst().orElseThrow();
        assertFalse(ramos.bajoMeta());
        assertEquals(13, ramos.fichasAtendidas());
        assertEquals(2, ramos.dias());

        CumplimientoMedicoDTO salas = r.stream().filter(c -> c.medicoId() == 3).findFirst().orElseThrow();
        assertFalse(salas.bajoMeta(), "5 fichas cumple la meta minima");
    }

    @Test
    void cumplimiento_noCuentaDiasFuturos() {
        repo.atendidas.add(dia(1, "Dr. Ramos", HOY, 6));

        reportes.cumplimiento(HOY, VIERNES);

        assertEquals(HOY, repo.ultimoHasta);
    }

    @Test
    void cumplimiento_periodoFuturo_vacio() {
        assertTrue(reportes.cumplimiento(HOY.plusDays(1), VIERNES).isEmpty());
    }

    /** CU-20 [3a]: sin datos que lo respalden, el indicador va en null (no se inventa un valor). */
    @Test
    void indicadores_sinDatos_noInventaValores() {
        IndicadoresDTO r = reportes.indicadores(HOY, VIERNES);

        assertNull(r.fichasPorMedicoDia());
        assertNull(r.minutosConfirmacionFin());
        assertEquals(0, r.solicitudes());
    }
}
