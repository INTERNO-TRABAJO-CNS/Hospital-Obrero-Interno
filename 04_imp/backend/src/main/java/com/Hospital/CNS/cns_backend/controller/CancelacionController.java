package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.CancelacionCita;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoCancelacion;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.identidad.PacienteService;

@RestController
public class CancelacionController {

    public record CancelacionResponse(ResultadoCancelacion resultado, String mensaje) {
    }

    private final CancelacionCita cancelacion;
    private final PacienteService pacientes;
    private final ResolutorUsuario resolutor;

    public CancelacionController(CancelacionCita cancelacion, PacienteService pacientes, ResolutorUsuario resolutor) {
        this.cancelacion = cancelacion;
        this.pacientes = pacientes;
        this.resolutor = resolutor;
    }

    @PostMapping("/api/v1/citas/{id}/cancelar")
    public CancelacionResponse cancelar(@RequestHeader HttpHeaders cabeceras, @PathVariable long id,
                                        @RequestParam(name = "pacienteId", required = false) Long pacienteId) {
        UsuarioAutenticado paciente = Permisos.exigir(resolutor, cabeceras,
                "Solo un paciente puede cancelar una cita", RolUsuario.PACIENTE);
        ResultadoCancelacion resultado = cancelacion.cancelarPorPaciente(id,
                pacientes.pacienteDelGrupo(paciente.personaId(), pacienteId));
        return switch (resultado) {
            case CANCELADA -> new CancelacionResponse(resultado, "Cita cancelada");
            case NO_ENCONTRADA -> throw new DominioException("NO_ENCONTRADA", "No existe esa cita");
            case NO_ES_DEL_PACIENTE ->
                throw new DominioException("NO_ES_DEL_PACIENTE", "La cita no pertenece al paciente autenticado");
            case ESTADO_NO_PERMITE ->
                throw new DominioException("ESTADO_NO_PERMITE", "Solo se puede cancelar una cita PROGRAMADA");
        };
    }
}