package com.Hospital.CNS.cns_backend.model.historial;

import static com.Hospital.CNS.cns_backend.model.compartido.Citas.cita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.CambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCambioCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;

/** CU-18: filtros por cita, usuario y rango de fechas; [3a] sin cambios -> lista vacia. */
class HistorialCambiosServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 3);

    private FakeCambioCitaRepository cambios;
    private FakeCitaRepository citas;
    private HistorialCambiosService service;

    @BeforeEach
    void preparar() {
        cambios = new FakeCambioCitaRepository();
        citas = new FakeCitaRepository();
        citas.store(cita(1, 10, 7, DIA, LocalTime.of(9, 0), 3, EstadoCita.ATENDIDA));
        citas.store(cita(2, 11, 8, DIA.plusDays(1), LocalTime.of(9, 30), 5, EstadoCita.CANCELADA));
        cambios.registrar(cambio(1, 1, "PROGRAMADA", "Sistema", DIA.minusDays(1).atTime(9, 12)));
        cambios.registrar(cambio(2, 1, "ASISTENCIA_CONFIRMADA", "Enfermería · M. Flores", DIA.atTime(8, 25)));
        cambios.registrar(cambio(3, 1, "ATENDIDA", "Médico · Dr. Ramos", DIA.atTime(9, 40)));
        cambios.registrar(cambio(4, 2, "CANCELADA", "Sistema", DIA.plusDays(1).atTime(9, 0)));
        service = new HistorialCambiosService(cambios, citas);
    }

    private static CambioCitaDTO cambio(long id, long citaId, String accion, String usuario, LocalDateTime cuando) {
        return new CambioCitaDTO(id, citaId, accion, usuario, cuando, "motivo " + id);
    }

    @Test
    void consultar_porCita() {
        List<CambioCitaDTO> resultado = service.consultar(1L, null, null, null);

        assertEquals(List.of(3L, 2L, 1L), resultado.stream().map(CambioCitaDTO::cambioId).toList());
    }

    @Test
    void consultar_rangoFechas() {
        List<CambioCitaDTO> resultado = service.consultar(null, null, DIA, DIA);

        assertEquals(List.of(3L, 2L), resultado.stream().map(CambioCitaDTO::cambioId).toList());
    }

    @Test
    void consultar_porUsuario_contieneSinMayusculas() {
        List<CambioCitaDTO> resultado = service.consultar(null, "  flores ", null, null);

        assertEquals(1, resultado.size());
        assertEquals("ASISTENCIA_CONFIRMADA", resultado.get(0).accion());
    }

    @Test
    void consultar_sinFiltros_todoDelMasNuevoAlMasViejo() {
        List<CambioCitaDTO> resultado = service.consultar(null, "", null, null);

        assertEquals(List.of(4L, 3L, 2L, 1L), resultado.stream().map(CambioCitaDTO::cambioId).toList());
    }

    @Test
    void consultar_sinCoincidencias_vacio() {
        assertTrue(service.consultar(99L, null, null, null).isEmpty());
    }

    @Test
    void consultar_desdeDespuesDeHasta_rechaza() {
        DominioException e = assertThrows(DominioException.class,
                () -> service.consultar(null, null, DIA.plusDays(2), DIA));
        assertEquals("DATOS_INVALIDOS", e.getCodigo());
    }

    @Test
    void consultarConCita_agregaFichaMedicoYPaciente() {
        CambioCitaVistaDTO fila = service.consultarConCita(2L, null, null, null).get(0);

        assertEquals(5, fila.numeroFicha());
        assertEquals("Medico 8", fila.medico());
        assertEquals("Paciente 11", fila.paciente());
        assertEquals(DIA.plusDays(1), fila.fechaCita());
        assertEquals("CANCELADA", fila.accion());
    }

    @Test
    void consultarConCita_citaInexistente_filaSinDatosDeCita() {
        cambios.registrar(cambio(5, 77, "CANCELADA", "Sistema", DIA.atTime(12, 0)));

        CambioCitaVistaDTO fila = service.consultarConCita(77L, null, null, null).get(0);

        assertEquals(0, fila.numeroFicha());
        assertEquals("", fila.medico());
    }

    @Test
    void registrar_noPermiteBorrar() {
        // El historial es de solo agregar: la interfaz no expone borrar ni modificar.
        List<String> metodos = Arrays.stream(CambioCitaRepository.class.getDeclaredMethods())
                .map(Method::getName).toList();
        assertTrue(metodos.stream().noneMatch(n -> n.startsWith("delete") || n.startsWith("borrar")
                || n.startsWith("eliminar") || n.startsWith("update") || n.startsWith("modificar")),
                "CambioCitaRepository no debe exponer borrado ni modificacion: " + metodos);
    }
}
