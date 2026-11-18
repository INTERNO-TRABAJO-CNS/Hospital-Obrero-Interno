package com.Hospital.CNS.cns_backend.model.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FakeSolicitudRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    private FakeSolicitudRepository repo;

    @BeforeEach
    void preparar() {
        repo = new FakeSolicitudRepository();
    }

    private static SolicitudDTO solicitud(long id, long paciente, int prioridad, long orden, EstadoSolicitud estado) {
        return new SolicitudDTO(id, paciente, TipoEspecialidad.ODONTOLOGIA, DIA, prioridad, orden, estado, null);
    }

    @Test
    void siguientesParaProcesar_ordenaPorPrioridadYLuegoPorLlegada() {
        repo.store(solicitud(1, 100, 2, 1, EstadoSolicitud.PENDIENTE)); // reprogramada, llego primero
        repo.store(solicitud(2, 101, 1, 3, EstadoSolicitud.PENDIENTE));
        repo.store(solicitud(3, 102, 1, 2, EstadoSolicitud.EN_ESPERA));

        List<SolicitudDTO> orden = repo.siguientesParaProcesar(DIA, 10);

        assertEquals(List.of(3L, 2L, 1L), orden.stream().map(SolicitudDTO::solicitudId).toList());
    }

    @Test
    void siguientesParaProcesar_respetaMaximoYDescartaAsignadasYSinCupo() {
        repo.store(solicitud(1, 100, 1, 1, EstadoSolicitud.ASIGNADA));
        repo.store(solicitud(2, 101, 1, 2, EstadoSolicitud.SIN_CUPO));
        repo.store(solicitud(3, 102, 1, 3, EstadoSolicitud.PENDIENTE));
        repo.store(solicitud(4, 103, 1, 4, EstadoSolicitud.PENDIENTE));

        List<SolicitudDTO> orden = repo.siguientesParaProcesar(DIA, 1);

        assertEquals(1, orden.size());
        assertEquals(3L, orden.get(0).solicitudId());
    }

    @Test
    void siguientesParaProcesar_noIncluyeDiasPosterioresAlLimite() {
        repo.store(solicitud(1, 100, 1, 1, EstadoSolicitud.PENDIENTE));

        assertTrue(repo.siguientesParaProcesar(DIA.minusDays(1), 10).isEmpty());
    }

    @Test
    void pacienteTieneSolicitudActivaEnDia_soloPendienteOEnEspera() {
        repo.store(solicitud(1, 100, 1, 1, EstadoSolicitud.EN_ESPERA));
        repo.store(solicitud(2, 101, 1, 2, EstadoSolicitud.ASIGNADA));

        assertTrue(repo.pacienteTieneSolicitudActivaEnDia(100, DIA));
        assertFalse(repo.pacienteTieneSolicitudActivaEnDia(101, DIA));
        assertFalse(repo.pacienteTieneSolicitudActivaEnDia(100, DIA.plusDays(1)));
    }

    @Test
    void store_conElMismoIdReemplazaLaSolicitud() {
        repo.store(solicitud(1, 100, 1, 1, EstadoSolicitud.PENDIENTE));
        repo.store(solicitud(1, 100, 1, 1, EstadoSolicitud.ASIGNADA));

        assertEquals(1, repo.solicitudes.size());
        assertEquals(EstadoSolicitud.ASIGNADA, repo.porId(1).orElseThrow().estado());
    }
}
