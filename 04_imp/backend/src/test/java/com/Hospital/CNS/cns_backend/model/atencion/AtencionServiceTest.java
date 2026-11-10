package com.Hospital.CNS.cns_backend.model.atencion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtencionServiceTest {
    private static final long CITA_ID = 10L;
    private static final long MEDICO_ID = 20L;
    private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");

    private FakeCitaRepository citas;
    private FakeHistorialRepository historial;
    private FakeFinalizacionCita finalizacion;
    private AtencionService service;

    @BeforeEach
    void setUp() {
        citas = new FakeCitaRepository();
        historial = new FakeHistorialRepository();
        finalizacion = new FakeFinalizacionCita();
        service = new AtencionService(citas, historial, finalizacion, () -> AHORA);
        citas.datos = datos(EstadoCita.ASISTENCIA_CONFIRMADA, MEDICO_ID);
    }

    @Test
    void registrar_ok_agregaRecetaYFinaliza() {
        ResultadoAtencion resultado = service.registrar(CITA_ID, MEDICO_ID, "Paracetamol 500 mg");

        assertEquals(ResultadoAtencion.REGISTRADA, resultado);
        assertEquals(1, historial.agregadas.size());
        assertEquals("Paracetamol 500 mg", historial.agregadas.get(0).medicacion());
        assertTrue(finalizacion.finalizada);
    }

    @Test
    void registrar_recetaVacia() {
        ResultadoAtencion resultado = service.registrar(CITA_ID, MEDICO_ID, "  ");

        assertEquals(ResultadoAtencion.RECETA_INVALIDA, resultado);
        assertTrue(historial.agregadas.isEmpty());
    }

    @Test
    void registrar_citaNoConfirmada() {
        citas.datos = datos(EstadoCita.PROGRAMADA, MEDICO_ID);

        ResultadoAtencion resultado = service.registrar(CITA_ID, MEDICO_ID, "Receta");

        assertEquals(ResultadoAtencion.ESTADO_NO_PERMITE, resultado);
        assertTrue(historial.agregadas.isEmpty());
    }

    @Test
    void registrar_citaAjena() {
        citas.datos = datos(EstadoCita.ASISTENCIA_CONFIRMADA, 99L);

        ResultadoAtencion resultado = service.registrar(CITA_ID, MEDICO_ID, "Receta");

        assertEquals(ResultadoAtencion.NO_ES_DE_SU_AGENDA, resultado);
        assertTrue(historial.agregadas.isEmpty());
    }

    @Test
    void registrar_siFinalizarFalla_noDejaReceta() {
        finalizacion.exito = false;

        assertThrows(
                AtencionException.class,
                () -> service.registrar(CITA_ID, MEDICO_ID, "Receta"));

        assertEquals(1, historial.agregadas.size());
        assertEquals(1, historial.eliminadas.size());
        assertFalse(finalizacion.finalizada);
    }

    private DatosAtencionDTO datos(EstadoCita estado, long medicoId) {
        return new DatosAtencionDTO(
                CITA_ID,
                medicoId,
                "1234567",
                "FIC-01",
                LocalDate.of(2026, 11, 10),
                LocalTime.of(9, 30),
                "Consulta general",
                estado);
    }

    private static class FakeCitaRepository implements CitaRepository {
        private DatosAtencionDTO datos;

        @Override
        public Optional<DatosAtencionDTO> buscarParaAtencion(long citaId) {
            return Optional.ofNullable(datos);
        }
    }

    private static class FakeHistorialRepository implements HistorialRepository {
        private final List<Receta> agregadas = new ArrayList<>();
        private final List<Receta> eliminadas = new ArrayList<>();

        @Override
        public void agregarReceta(long citaId, String medicacion, Instant registradaEn) {
            agregadas.add(new Receta(citaId, medicacion));
        }

        @Override
        public void quitarReceta(long citaId, String medicacion) {
            eliminadas.add(new Receta(citaId, medicacion));
        }
    }

    private static class FakeFinalizacionCita implements FinalizacionCita {
        private boolean exito = true;
        private boolean finalizada;

        @Override
        public boolean finalizar(long citaId, long medicoId) {
            finalizada = exito;
            return exito;
        }
    }

    private record Receta(long citaId, String medicacion) {
    }
}