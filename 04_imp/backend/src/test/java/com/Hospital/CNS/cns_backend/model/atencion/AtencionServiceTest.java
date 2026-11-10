package com.Hospital.CNS.cns_backend.model.atencion;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeFinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeHistorialRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FinalizacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.RecetaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/** CU-15 con FakeHistorialRepository, FakeFinalizacionCita y FakeCitaRepository (sin BD). */
class AtencionServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    private static final LocalDateTime AHORA = HOY.atTime(8, 35);
    private static final long MEDICO = 7;

    private FakeCitaRepository citas;
    private FakeHistorialRepository historiales;
    private FakeFinalizacionCita finalizacion;
    private AtencionService service;

    @BeforeEach
    void preparar() {
        citas = new FakeCitaRepository();
        historiales = new FakeHistorialRepository();
        finalizacion = new FakeFinalizacionCita();
        service = new AtencionService(citas, historiales, finalizacion, new RelojFijo(AHORA));
        citas.store(cita(1, 100, MEDICO, HOY, LocalTime.of(8, 0), 2, EstadoCita.ASISTENCIA_CONFIRMADA));
    }

    @Test
    void registrar_ok_agregaRecetaYFinaliza() {
        ResultadoAtencion r = service.registrar(1, MEDICO, "  Paracetamol 500 mg c/8 h por 3 dias  ");

        assertEquals(ResultadoAtencion.REGISTRADA, r);
        assertEquals(1, historiales.recetas.size());
        RecetaDTO receta = historiales.recetas.get(0);
        assertEquals("Paracetamol 500 mg c/8 h por 3 dias", receta.medicacion());
        assertEquals(100, receta.pacienteId());
        assertEquals(2, receta.numeroFicha());
        assertEquals(AHORA, receta.fechaHora());
        assertEquals(1, finalizacion.llamadas.size());
        assertEquals(new FakeFinalizacionCita.Llamada(1, MEDICO), finalizacion.llamadas.get(0));
    }

    @Test
    void registrar_recetaVacia() { // [2a]
        assertEquals(ResultadoAtencion.RECETA_INVALIDA, service.registrar(1, MEDICO, "   "));
        assertEquals(ResultadoAtencion.RECETA_INVALIDA, service.registrar(1, MEDICO, null));
        assertTrue(historiales.recetas.isEmpty());
        assertTrue(finalizacion.llamadas.isEmpty());
    }

    @Test
    void registrar_recetaDemasiadoLarga() { // [2a]
        assertEquals(ResultadoAtencion.RECETA_INVALIDA, service.registrar(1, MEDICO, "x".repeat(2001)));
        assertEquals(ResultadoAtencion.REGISTRADA, service.registrar(1, MEDICO, "x".repeat(2000)));
    }

    @Test
    void registrar_citaNoConfirmada() { // [1a]
        citas.store(cita(2, 101, MEDICO, HOY, LocalTime.of(9, 0), 3, EstadoCita.PROGRAMADA));

        assertEquals(ResultadoAtencion.ESTADO_NO_PERMITE, service.registrar(2, MEDICO, "Reposo"));
        assertTrue(historiales.recetas.isEmpty());
        assertTrue(finalizacion.llamadas.isEmpty());
    }

    @Test
    void registrar_citaAjena() { // [1a]
        assertEquals(ResultadoAtencion.NO_ES_DE_SU_AGENDA, service.registrar(1, 99, "Reposo"));
        assertTrue(historiales.recetas.isEmpty());
    }

    @Test
    void registrar_citaInexistente() {
        assertEquals(ResultadoAtencion.NO_ENCONTRADA, service.registrar(55, MEDICO, "Reposo"));
    }

    @Test
    void registrar_siFinalizarFalla_noDejaReceta() {
        finalizacion.resultado = ResultadoFinalizacion.ESTADO_NO_PERMITE;

        assertEquals(ResultadoAtencion.ESTADO_NO_PERMITE, service.registrar(1, MEDICO, "Reposo"));
        assertTrue(historiales.recetas.isEmpty(), "la receta se quita si no se pudo finalizar");
        assertEquals(1, finalizacion.llamadas.size());
    }

    @Test
    void registrar_siFinalizarLanzaError_noDejaReceta() {
        FinalizacionCita rota = (citaId, medicoId) -> {
            throw new IllegalStateException("sin conexion");
        };
        AtencionService conFalla = new AtencionService(citas, historiales, rota, new RelojFijo(AHORA));

        assertThrows(IllegalStateException.class, () -> conFalla.registrar(1, MEDICO, "Reposo"));
        assertTrue(historiales.recetas.isEmpty());
    }

    @Test
    void datosParaAtencion_autocompleta() {
        DatosAtencionDTO d = service.datosParaAtencion(1, MEDICO);

        assertEquals("AF-100", d.numeroAfiliado());
        assertEquals(2, d.numeroFicha());
        assertEquals(HOY, d.fecha());
        assertEquals(LocalTime.of(8, 0), d.hora());
        assertEquals(TipoEspecialidad.CONSULTORIO_FAMILIAR, d.consulta());
        assertEquals(TipoEspecialidad.Categoria.BASICA, d.categoriaConsulta());
        assertTrue(d.puedeDerivar());
    }

    @Test
    void datosParaAtencion_consultaInterna_noDeriva() {
        citas.store(cita(3, 102, MEDICO, TipoEspecialidad.CARDIOLOGIA, HOY, LocalTime.of(10, 0), 1,
                EstadoCita.ASISTENCIA_CONFIRMADA));

        DatosAtencionDTO d = service.datosParaAtencion(3, MEDICO);

        assertEquals(TipoEspecialidad.CARDIOLOGIA, d.consulta());
        assertEquals(TipoEspecialidad.Categoria.INTERNA, d.categoriaConsulta());
        assertFalse(d.puedeDerivar());
    }

    @Test
    void datosParaAtencion_citaAjena_rechaza() { // [1a]
        DominioException e = assertThrows(DominioException.class, () -> service.datosParaAtencion(1, 99));
        assertEquals("NO_ES_DE_SU_AGENDA", e.getCodigo());
    }
}
