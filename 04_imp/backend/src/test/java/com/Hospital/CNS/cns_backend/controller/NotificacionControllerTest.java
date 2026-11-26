package com.Hospital.CNS.cns_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.comunicacion.NotificacionRepository;

class NotificacionControllerTest {

    @Test
    void delPaciente_devuelveSoloLaPersonaAutenticada() {
        FakeRepository repository = new FakeRepository();
        NotificacionController controller = new NotificacionController(
                repository,
                resolver(RolUsuario.PACIENTE, 7));

        assertEquals(1, controller.delPaciente(new HttpHeaders(), 7).size());
    }

    @Test
    void delPaciente_rechazaConsultarOtraPersona() {
        NotificacionController controller = new NotificacionController(
                new FakeRepository(),
                resolver(RolUsuario.PACIENTE, 7));

        DominioException error = assertThrows(
                DominioException.class,
                () -> controller.delPaciente(new HttpHeaders(), 8));

        assertEquals("NO_ES_DEL_PACIENTE", error.getCodigo());
    }

    private static ResolutorUsuario resolver(RolUsuario rol, long personaId) {
        return new ResolutorUsuario(null) {
            @Override
            public UsuarioAutenticado resolver(Function<String, String> cabecera) {
                return new UsuarioAutenticado(1, rol, personaId);
            }
        };
    }

    private static class FakeRepository implements NotificacionRepository {
        @Override
        public List<NotificacionDTO> delPaciente(long pacienteId) {
            return List.of(new NotificacionDTO(1, pacienteId, 4L, null, null, "AVISO_T45", "Aviso",
                    LocalDateTime.of(2026, 11, 10, 12, 0)));
        }

        @Override
        public boolean avisoYaEnviado(long citaId) {
            return false;
        }
    }
}
