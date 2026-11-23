package com.Hospital.CNS.cns_backend.infrastructure.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteDTO;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.FichaSupervisionDTO;
import com.Hospital.CNS.cns_backend.model.reportes.IndicadoresDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteEstadoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteGrupoDTO;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.ResumenEspecialidadDTO;

/**
 * Integracion contra PostgreSQL de las lecturas de CU-19/CU-20 (no corre con {@code ./mvnw test}; ejecutar con
 * {@code ./mvnw test -Dtest=PgReporteRepositoryIT}). Crea un medico, un consultorio, un paciente y 3 citas en un dia
 * sin otros datos (04/03/2031) y los borra al terminar. Si no hay base de datos, la prueba se omite.
 */
class PgReporteRepositoryIT {

    private static final LocalDate DIA = LocalDate.of(2031, 3, 4);

    private final PgReporteRepository repo = new PgReporteRepository();
    private final PgPacienteRepository pacientes = new PgPacienteRepository();
    private final PgMedicoRepository medicos = new PgMedicoRepository();
    private final PgConsultorioRepository consultorios = new PgConsultorioRepository();
    private final PgCitaRepository citas = new PgCitaRepository();
    private final PgCambioCitaRepository cambios = new PgCambioCitaRepository();

    private long pacienteId;
    private long medicoId;
    private int consultorioId;
    private long atendida;
    private long cancelada;
    private long programada;

    @BeforeEach
    void preparar() {
        try (Connection conn = DB.getConnection()) {
            Assumptions.assumeTrue(conn.isValid(2));
        } catch (IllegalStateException | SQLException sinBase) {
            Assumptions.abort("Sin PostgreSQL disponible: " + sinBase.getMessage());
        }
        pacienteId = pacientes.nextIdentity();
        assertTrue(pacientes.store(new PacienteDTO(pacienteId, "AF-10020", "IT-" + pacienteId, "Prueba", "Reportes",
                LocalDate.of(1990, 1, 1), false)));
        medicoId = medicos.nextIdentity();
        medicos.store(new MedicoDTO(medicoId, "Medico", "Reportes IT", TipoEspecialidad.CONSULTORIO_FAMILIAR, 40, 6,
                true));
        consultorioId = consultorios.nextIdentity();
        consultorios.store(new ConsultorioDTO(consultorioId, "Consultorio Reportes IT " + consultorioId, true));

        atendida = nuevaCita(LocalTime.of(8, 0), 1, EstadoCita.ASISTENCIA_CONFIRMADA);
        cambios.registrar(new CambioCitaDTO(0, atendida, "ASISTENCIA_CONFIRMADA", "enfermeria",
                DIA.atTime(7, 40), null));
        assertTrue(citas.registrarFin(atendida, DIA.atTime(8, 20)));

        cancelada = nuevaCita(LocalTime.of(8, 40), 2, EstadoCita.CANCELADA);
        cambios.registrar(new CambioCitaDTO(0, cancelada, "CANCELADA", "SISTEMA", DIA.atTime(8, 10),
                "No confirmo asistencia"));

        programada = nuevaCita(LocalTime.of(9, 20), 3, EstadoCita.PROGRAMADA);
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (pacienteId == 0) {
            return;
        }
        try (Connection conn = DB.getConnection()) {
            borrar(conn, "delete from citas.cambio_cita where cita_id in "
                    + "(select id from citas.cita where paciente_id = ?)", pacienteId);
            borrar(conn, "delete from citas.cita where paciente_id = ?", pacienteId);
            borrar(conn, "delete from identidad.paciente where id = ?", pacienteId);
            borrar(conn, "delete from recursos.consultorio where id = ?", consultorioId);
            borrar(conn, "delete from recursos.medico where id = ?", medicoId);
        }
    }

    @Test
    void IT_resumenDelDia_cuentaPorEstado() {
        ResumenEspecialidadDTO r = repo.resumenDelDia(DIA).stream().filter(x -> x.medicoId() == medicoId)
                .findFirst().orElseThrow();

        assertEquals(TipoEspecialidad.CONSULTORIO_FAMILIAR, r.especialidad());
        assertEquals("Medico Reportes IT", r.nombreMedico());
        assertEquals(2, r.fichas(), "las canceladas no ocupan cupo");
        assertEquals(1, r.atendidas());
        assertEquals(1, r.programadas());
        assertEquals(1, r.canceladas());
        assertEquals(6, r.cupoMaximo());
    }

    @Test
    void IT_fichasDeMedico() {
        List<FichaSupervisionDTO> f = repo.fichasDeMedico(medicoId, DIA);

        assertEquals(List.of(1, 2, 3), f.stream().map(FichaSupervisionDTO::numeroFicha).toList());
        assertEquals("Prueba Reportes", f.get(0).nombrePaciente());
        assertEquals(consultorioId, f.get(0).consultorio());
    }

    @Test
    void IT_citasPorEstado_cuentaLasAutomaticas() {
        ReporteEstadoDTO c = repo.citasPorEstado(DIA, DIA).stream()
                .filter(x -> x.estado() == EstadoCita.CANCELADA).findFirst().orElseThrow();

        assertEquals(1, c.cantidad());
        assertEquals(1, c.automaticas());
    }

    @Test
    void IT_atendidasPorMedicoYDia_yPorConsultorio() {
        ReporteRepository.AtendidasDia d = repo.atendidasPorMedicoYDia(DIA, DIA).stream()
                .filter(x -> x.medicoId() == medicoId).findFirst().orElseThrow();
        assertEquals(1, d.atendidas());

        ReporteGrupoDTO g = repo.atendidasPorConsultorio(DIA, DIA).stream()
                .filter(x -> x.grupo().equals("Consultorio Reportes IT " + consultorioId)).findFirst().orElseThrow();
        assertEquals(1, g.cantidad());
    }

    @Test
    void IT_indicadores() {
        IndicadoresDTO i = repo.indicadores(DIA, DIA);

        assertEquals(1, i.canceladas());
        assertEquals(1, i.canceladasAutomaticas());
        assertNotNull(i.fichasPorMedicoDia());
        assertEquals(40.0, i.minutosConfirmacionFin(), 0.01);
        assertEquals(1, i.atencionesMedidas());
    }

    private long nuevaCita(LocalTime hora, int ficha, EstadoCita estado) {
        long id = citas.nextIdentity();
        assertTrue(citas.store(new CitaDTO(id, pacienteId, "", "", medicoId, "", TipoEspecialidad.CONSULTORIO_FAMILIAR,
                consultorioId, DIA, hora, ficha, estado)));
        return id;
    }

    private static void borrar(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            st.executeUpdate();
        }
    }
}
