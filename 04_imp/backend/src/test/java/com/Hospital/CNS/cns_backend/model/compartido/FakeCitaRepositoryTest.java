package com.Hospital.CNS.cns_backend.model.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FakeCitaRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 9);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);

    private FakeCitaRepository repo;

    @BeforeEach
    void preparar() {
        repo = new FakeCitaRepository();
    }

    private static CitaDTO cita(long id, long medico, long paciente, LocalTime hora, EstadoCita estado) {
        return new CitaDTO(id, paciente, "Paciente " + paciente, "AF-" + paciente, medico, "Medico " + medico,
                TipoEspecialidad.PEDIATRIA, 3, DIA, hora, 1, estado);
    }

    @Test
    void store_rechazaMismoMedicoFechaYHora() {
        assertTrue(repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA)));

        assertFalse(repo.store(cita(2, 10, 101, NUEVE, EstadoCita.PROGRAMADA)));
        assertEquals(1, repo.citas.size());
    }

    @Test
    void store_permiteMismaHoraConOtroMedico() {
        assertTrue(repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA)));

        assertTrue(repo.store(cita(2, 11, 101, NUEVE, EstadoCita.PROGRAMADA)));
    }

    @Test
    void store_permiteReutilizarElHorarioSiLaCitaAnteriorEstaCancelada() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));
        repo.cambiarEstado(1, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA);

        assertTrue(repo.store(cita(2, 10, 101, NUEVE, EstadoCita.PROGRAMADA)));
        assertEquals(2, repo.citas.size());
    }

    @Test
    void store_conElMismoIdActualizaSinDuplicar() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));

        assertTrue(repo.store(cita(1, 10, 100, NUEVE, EstadoCita.ATENDIDA)));
        assertEquals(1, repo.citas.size());
        assertEquals(EstadoCita.ATENDIDA, repo.porId(1).orElseThrow().estado());
    }

    @Test
    void cambiarEstado_soloSiElEstadoActualEsDesde() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));

        assertFalse(repo.cambiarEstado(1, EstadoCita.ASISTENCIA_CONFIRMADA, EstadoCita.ATENDIDA));
        assertEquals(EstadoCita.PROGRAMADA, repo.porId(1).orElseThrow().estado());

        assertTrue(repo.cambiarEstado(1, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA));
        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, repo.porId(1).orElseThrow().estado());
    }

    @Test
    void cambiarEstado_dosIntentosSobreElMismoEstadoSoloUnoGana() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));

        boolean primero = repo.cambiarEstado(1, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA);
        boolean segundo = repo.cambiarEstado(1, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA);

        assertTrue(primero);
        assertFalse(segundo);
        assertEquals(EstadoCita.CANCELADA, repo.porId(1).orElseThrow().estado());
    }

    @Test
    void cambiarEstado_citaInexistenteDevuelveFalse() {
        assertFalse(repo.cambiarEstado(99, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA));
    }

    @Test
    void registrarFin_soloDesdeAsistenciaConfirmada() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));
        LocalDateTime fin = LocalDateTime.of(2026, 11, 9, 9, 40);

        assertFalse(repo.registrarFin(1, fin));
        assertTrue(repo.horasFin.isEmpty());

        repo.cambiarEstado(1, EstadoCita.PROGRAMADA, EstadoCita.ASISTENCIA_CONFIRMADA);
        assertTrue(repo.registrarFin(1, fin));
        assertEquals(EstadoCita.ATENDIDA, repo.porId(1).orElseThrow().estado());
        assertEquals(fin, repo.horasFin.get(1L));
    }

    @Test
    void losConteosYConsultasIgnoranLasCanceladas() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));
        repo.store(cita(2, 10, 101, LocalTime.of(10, 0), EstadoCita.CANCELADA));

        assertEquals(1, repo.contarDelMedicoEnFecha(10, DIA));
        assertTrue(repo.pacienteTieneCitaEnFecha(100, DIA));
        assertFalse(repo.pacienteTieneCitaEnFecha(101, DIA));
        assertTrue(repo.medicoTienePacienteEnAgenda(10, 100));
        assertFalse(repo.medicoTienePacienteEnAgenda(10, 101));
        assertEquals(2, repo.delDia(DIA).size());
        assertEquals(2, repo.delMedicoEnFecha(10, DIA).size());
        assertEquals(1, repo.delPaciente(101).size());
    }

    @Test
    void contarProgramadas_porMedicoYPorConsultorio() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));
        repo.store(cita(2, 10, 101, LocalTime.of(10, 0), EstadoCita.ASISTENCIA_CONFIRMADA));

        assertEquals(1, repo.contarProgramadasDelMedico(10));
        assertEquals(1, repo.contarProgramadasEnConsultorio(3));
        assertEquals(0, repo.contarProgramadasEnConsultorio(4));
    }

    @Test
    void programadasHasta_soloProgramadasHastaElLimite() {
        repo.store(cita(1, 10, 100, NUEVE, EstadoCita.PROGRAMADA));
        repo.store(cita(2, 10, 101, LocalTime.of(10, 0), EstadoCita.PROGRAMADA));
        repo.store(cita(3, 11, 102, NUEVE, EstadoCita.CANCELADA));

        var hasta = repo.programadasHasta(LocalDateTime.of(DIA, NUEVE));

        assertEquals(1, hasta.size());
        assertEquals(1L, hasta.get(0).citaId());
    }

    @Test
    void nextIdentity_noChocaConIdsGuardados() {
        repo.store(cita(5, 10, 100, NUEVE, EstadoCita.PROGRAMADA));

        assertEquals(6L, repo.nextIdentity());
    }
}
