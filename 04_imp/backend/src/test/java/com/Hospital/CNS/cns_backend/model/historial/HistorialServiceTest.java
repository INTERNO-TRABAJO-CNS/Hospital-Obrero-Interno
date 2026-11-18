package com.Hospital.CNS.cns_backend.model.historial;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.AtencionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.FakePacienteRepository;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;

/** CU-14 con FakeHistorialRepository y FakeCitaRepository (sin BD). */
class HistorialServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    private static final long MEDICO = 7;
    private static final long PACIENTE = 100;

    private FakeCitaRepository citas;
    private FakeHistorialRepository historiales;
    private HistorialService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        historiales = new FakeHistorialRepository();
        historiales.nombres.put(PACIENTE, "Ana Quispe");
        service = new HistorialService(historiales, citas, new RelojFijo(HOY.atTime(10, 0)));
    }

    @Test
    void verHistorial_pacienteEnAgenda_ok() {
        citas.store(cita(1, PACIENTE, MEDICO, HOY, LocalTime.of(11, 0), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, PACIENTE, 9, TipoEspecialidad.ODONTOLOGIA, HOY.minusDays(5), LocalTime.of(8, 0), 1,
                EstadoCita.ATENDIDA));
        historiales.agregarReceta(new RecetaDTO(0, 2, PACIENTE, "Ibuprofeno 400 mg", HOY.minusDays(5).atTime(8, 40), 1));

        HistorialPacienteDTO h = service.verHistorial(MEDICO, PACIENTE);

        assertEquals("Ana Quispe", h.nombrePaciente());
        assertEquals(1, h.citasPasadas().size(), "la cita de hoy a las 11:00 todavia no paso");
        assertEquals(2L, h.citasPasadas().get(0).citaId());
        assertEquals(1, h.atenciones().size());
        AtencionDTO atencion = h.atenciones().get(0);
        assertEquals("ODONTOLOGIA", atencion.especialidad(), "se completa con la cita de la receta");
        assertEquals("Medico 9", atencion.medico());
        assertEquals(LocalTime.of(8, 0), atencion.hora());
        assertEquals("Ibuprofeno 400 mg", atencion.recetas().get(0).medicacion());
    }

    @Test
    void verHistorial_pacienteAjeno_rechaza() { // [1a]
        citas.store(cita(1, PACIENTE, 9, HOY, LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));

        DominioException e = assertThrows(DominioException.class, () -> service.verHistorial(MEDICO, PACIENTE));

        assertEquals("PACIENTE_FUERA_DE_AGENDA", e.getCodigo());
    }

    @Test
    void verHistorial_citaCanceladaNoCuentaComoAgenda() { // [1a]
        citas.store(cita(1, PACIENTE, MEDICO, HOY, LocalTime.of(8, 0), 1, EstadoCita.CANCELADA));

        assertThrows(DominioException.class, () -> service.verHistorial(MEDICO, PACIENTE));
    }

    @Test
    void verHistorial_ordenaPorFecha() {
        citas.store(cita(1, PACIENTE, MEDICO, HOY.minusDays(30), LocalTime.of(8, 0), 1, EstadoCita.ATENDIDA));
        citas.store(cita(2, PACIENTE, MEDICO, HOY.minusDays(2), LocalTime.of(9, 0), 2, EstadoCita.ATENDIDA));
        citas.store(cita(3, PACIENTE, MEDICO, HOY.minusDays(10), LocalTime.of(9, 0), 2, EstadoCita.CANCELADA));
        historiales.agregarReceta(new RecetaDTO(0, 1, PACIENTE, "A", HOY.minusDays(30).atTime(8, 30), 1));
        historiales.agregarReceta(new RecetaDTO(0, 2, PACIENTE, "B", HOY.minusDays(2).atTime(9, 30), 2));

        HistorialPacienteDTO h = service.verHistorial(MEDICO, PACIENTE);

        assertEquals(List.of(2L, 3L, 1L), h.citasPasadas().stream().map(CitaPasadaDTO::citaId).toList());
        assertEquals(List.of(HOY.minusDays(2), HOY.minusDays(30)),
                h.atenciones().stream().map(AtencionDTO::fecha).toList());
    }

    @Test
    void verHistorial_sinHistorialTodavia_listasVacias() {
        citas.store(cita(1, PACIENTE, MEDICO, HOY.plusDays(1), LocalTime.of(8, 0), 1, EstadoCita.PROGRAMADA));

        HistorialPacienteDTO h = service.verHistorial(MEDICO, PACIENTE);

        assertTrue(h.atenciones().isEmpty());
        assertTrue(h.citasPasadas().isEmpty());
        assertEquals("Paciente 100", h.nombrePaciente(), "sin historial, el nombre sale de la cita");
    }

    @Test
    void verHistorial_noModificaNada() {
        citas.store(cita(1, PACIENTE, MEDICO, HOY, LocalTime.of(8, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA));

        service.verHistorial(MEDICO, PACIENTE);

        assertEquals(EstadoCita.ASISTENCIA_CONFIRMADA, citas.porId(1).orElseThrow().estado());
        assertTrue(historiales.recetas.isEmpty());
    }

    // ------------------------------------- datos del paciente y buscador (Figma del historial, 13/11/2026)

    @Test
    void verHistorial_conPacientes_traeAfiliacionTitularYEdad() {
        FakePacienteRepository pacientes = new FakePacienteRepository();
        pacientes.store(new PacienteDTO(PACIENTE, "AF-10001", "4512301", "Ana", "Quispe", LocalDate.of(2002, 3, 1), true));
        HistorialService conPacientes = new HistorialService(historiales, citas, new RelojFijo(HOY.atTime(10, 0)),
                pacientes);
        citas.store(cita(1, PACIENTE, MEDICO, HOY, LocalTime.of(11, 0), 3, EstadoCita.PROGRAMADA));

        HistorialPacienteDTO h = conPacientes.verHistorial(MEDICO, PACIENTE);

        assertEquals("AF-10001", h.numeroAfiliado());
        assertEquals(true, h.titular());
        assertEquals(24, h.edad());
    }

    @Test
    void buscarEnAgenda_soloPacientesDeSuAgenda() {
        FakePacienteRepository pacientes = new FakePacienteRepository();
        pacientes.store(new PacienteDTO(PACIENTE, "AF-10001", "4512301", "Ana", "Quispe", LocalDate.of(2002, 3, 1), true));
        pacientes.store(new PacienteDTO(101, "AF-10001", "9000001", "Sofia", "Quispe", LocalDate.of(2018, 1, 1), false));
        HistorialService conPacientes = new HistorialService(historiales, citas, new RelojFijo(HOY.atTime(10, 0)),
                pacientes);
        citas.store(cita(1, PACIENTE, MEDICO, HOY, LocalTime.of(11, 0), 3, EstadoCita.PROGRAMADA));
        citas.store(cita(2, 101, 9, HOY, LocalTime.of(11, 0), 1, EstadoCita.PROGRAMADA));

        List<PacienteDeAgendaDTO> encontrados = conPacientes.buscarEnAgenda(MEDICO, "AF-10001");

        assertEquals(1, encontrados.size());
        assertEquals(PACIENTE, encontrados.get(0).pacienteId());
        assertTrue(conPacientes.buscarEnAgenda(MEDICO, " ").isEmpty());
    }
}
