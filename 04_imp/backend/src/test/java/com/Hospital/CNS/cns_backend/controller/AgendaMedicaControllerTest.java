package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.operaciones.AgendaMedicaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgendaMedicaControllerTest {

    @Test
    void controller_rolPaciente_403() {
        AgendaMedicaService serviceMock = Mockito.mock(AgendaMedicaService.class);
        AgendaMedicaController controller = new AgendaMedicaController(serviceMock);

        // Simulamos la petición con un rol no autorizado (PACIENTE)
        ResponseEntity<?> response = controller.obtenerAgenda("PACIENTE", 1L, null);

        assertEquals(403, response.getStatusCode().value());
    }
}