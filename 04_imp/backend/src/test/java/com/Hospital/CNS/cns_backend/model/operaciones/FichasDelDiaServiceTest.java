package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FichasDelDiaServiceTest {

    private CitaRepository citaRepository;
    private Reloj reloj;
    private FichasDelDiaService service;
    private final LocalDate HOY = LocalDate.of(2026, 11, 14);

    @BeforeEach
    void setUp() {
        citaRepository = Mockito.mock(CitaRepository.class);
        reloj = Mockito.mock(Reloj.class);
        when(reloj.ahora()).thenReturn(HOY.atStartOfDay());
        service = new FichasDelDiaService(citaRepository, reloj);
    }

    @Test
    void buscar_porMedico() {
        CitaDTO cita = new CitaDTO(
            1L, 100L, "Juan Perez", "12345", 
            200L, "Dr. Pérez", TipoEspecialidad.CARDIOLOGIA, 1, 
            HOY, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.delDia(HOY)).thenReturn(List.of(cita));

        List<CitaDTO> resultado = service.buscar(CriterioFicha.MEDICO, "Pérez");

        assertFalse(resultado.isEmpty());
        assertEquals("Dr. Pérez", resultado.get(0).nombreMedico());
    }

    @Test
    void buscar_fechaDistintaDeHoy_rechaza() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscar(null, "Valor");
        });
    }

    @Test
    void buscar_sinCoincidencias_vacio() {
        CitaDTO cita = new CitaDTO(
            1L, 100L, "Juan Perez", "12345", 
            200L, "Dr. Pérez", TipoEspecialidad.CARDIOLOGIA, 1, 
            HOY, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.delDia(HOY)).thenReturn(List.of(cita));

        List<CitaDTO> resultado = service.buscar(CriterioFicha.PACIENTE, "Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listadoTv_soloDeHoy() {
        CitaDTO cita = new CitaDTO(
            1L, 100L, "Juan Perez", "12345", 
            200L, "Dr. Pérez", TipoEspecialidad.CARDIOLOGIA, 1, 
            HOY, LocalTime.of(10, 0), 1, EstadoCita.PROGRAMADA
        );
        
        when(citaRepository.delDia(HOY)).thenReturn(List.of(cita));

        List<CitaDTO> resultado = service.listadoParaTv();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).citaId());
    }
}