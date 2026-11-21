package com.Hospital.CNS.cns_backend.model.operaciones;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.FakeCitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.FichaTvDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RelojFijo;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FichasDelDiaServiceTest {

    private FakeCitaRepository citaRepository;
    private RelojFijo reloj;
    private FichasDelDiaService service;

    private final LocalDate HOY = LocalDate.of(2026, 11, 14);

    @BeforeEach
    void setUp() {
        citaRepository = new FakeCitaRepository();
        reloj = new RelojFijo(HOY.atStartOfDay());
        service = new FichasDelDiaService(citaRepository, reloj);
    }

    private CitaDTO crearCita(long id, LocalDate fecha, LocalTime hora, int ficha, String paciente, String medico) {
        return new CitaDTO(
                id, 100L + id, paciente, "AF-" + id,
                200L + id, medico, TipoEspecialidad.CARDIOLOGIA, 1,
                fecha, hora, ficha, EstadoCita.PROGRAMADA
        );
    }

    @Test
    void buscar_porMedico() {
        citaRepository.citas.add(
                crearCita(1L, HOY, LocalTime.of(10, 0), 1, "Juan Perez", "Dr. Pérez")
        );

        List<CitaDTO> resultado = service.buscar(CriterioFicha.MEDICO, "Pérez");

        assertFalse(resultado.isEmpty());
        assertEquals("Dr. Pérez", resultado.get(0).nombreMedico());
    }

    @Test
    void buscar_sinCriterio_rechaza() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscar(null, "Valor");
        });
    }

    @Test
    void buscar_sinCoincidencias_vacio() {
        citaRepository.citas.add(
                crearCita(1L, HOY, LocalTime.of(10, 0), 1, "Juan Perez", "Dr. Pérez")
        );

        List<CitaDTO> resultado = service.buscar(CriterioFicha.PACIENTE, "Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listadoDelDia_excluyeCitasDeOtraFecha() {
        CitaDTO citaHoy = crearCita(1L, HOY, LocalTime.of(9, 0), 1, "Ana López", "Dra. Gómez");
        CitaDTO citaManana = crearCita(2L, HOY.plusDays(1), LocalTime.of(11, 0), 2, "Carlos Ruiz", "Dr. Pérez");
        citaRepository.citas.addAll(List.of(citaHoy, citaManana));

        List<CitaDTO> resultado = service.listadoDelDia();

        assertEquals(1, resultado.size());
        assertEquals("Ana López", resultado.get(0).nombrePaciente());
        assertEquals("AF-1", resultado.get(0).numeroAfiliado());
    }

    @Test
    void listadoTv_soloDeHoy() {
        citaRepository.citas.addAll(List.of(
                crearCita(1L, HOY, LocalTime.of(10, 0), 5, "Juan Perez", "Dr. Pérez"),
                crearCita(2L, HOY.minusDays(1), LocalTime.of(8, 0), 9, "Otro Paciente", "Dr. Pérez")
        ));

        List<FichaTvDTO> resultado = service.listadoParaTv();

        assertEquals(1, resultado.size());
        assertEquals(5, resultado.get(0).numeroFicha());
        assertEquals(LocalTime.of(10, 0), resultado.get(0).hora());
        assertEquals(TipoEspecialidad.CARDIOLOGIA, resultado.get(0).especialidad());
        assertEquals(1, resultado.get(0).consultorio());
        assertEquals(EstadoCita.PROGRAMADA, resultado.get(0).estado());
    }
}