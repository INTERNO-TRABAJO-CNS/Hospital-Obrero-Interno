package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.operaciones.ActualizacionEstadoService;
import com.Hospital.CNS.cns_backend.model.operaciones.ResultadoConfirmacion;

/**
 * CU-12 (T100-10). El usuario sale SIEMPRE del token (nunca del body ni de parametros): enfermeria confirma la
 * llegada y el medico dueno de la cita registra el fin. Los rechazos se devuelven como {@code {codigo, mensaje}}.
 */
@RestController
@RequestMapping("/api/v1/citas")
public class EstadoCitaController {

    public record ConfirmacionResponse(ResultadoConfirmacion resultado, String mensaje) {
    }

    public record FinalizacionResponse(ResultadoFinalizacion resultado, String mensaje) {
    }

    private final ActualizacionEstadoService service;
    private final ResolutorUsuario resolutor;

    public EstadoCitaController(ActualizacionEstadoService service, ResolutorUsuario resolutor) {
        this.service = service;
        this.resolutor = resolutor;
    }

    @PostMapping("/{id}/confirmar-asistencia")
    public ConfirmacionResponse confirmarAsistencia(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        UsuarioAutenticado enfermeria = Permisos.exigir(resolutor, cabeceras,
                "Solo enfermería puede confirmar la asistencia", RolUsuario.ENFERMERIA);
        ResultadoConfirmacion resultado = service.confirmarAsistencia(id, enfermeria);
        return switch (resultado) {
            case CONFIRMADA -> new ConfirmacionResponse(resultado, "Asistencia confirmada");
            case NO_ENCONTRADA -> throw new DominioException("NO_ENCONTRADA", "No existe esa cita");
            case ROL_NO_AUTORIZADO ->
                throw new DominioException("ACCESO_DENEGADO", "Solo enfermería puede confirmar la asistencia");
            case NO_ES_DE_HOY -> throw new DominioException("NO_ES_DE_HOY", "Solo se confirman citas de hoy");
            case FUERA_DE_PLAZO -> throw new DominioException("FUERA_DE_PLAZO",
                    "Pasó el plazo de 30 minutos antes de la cita; la cita se cancela automáticamente");
            case ESTADO_INVALIDO -> throw new DominioException("ESTADO_NO_PERMITE",
                    "Solo se confirma la llegada de una cita PROGRAMADA");
        };
    }

    @PostMapping("/{id}/finalizar")
    public FinalizacionResponse finalizar(@RequestHeader HttpHeaders cabeceras, @PathVariable("id") long id) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        ResultadoFinalizacion resultado = service.finalizar(id, medico.personaId());
        return switch (resultado) {
            case FINALIZADA -> new FinalizacionResponse(resultado, "Atención finalizada");
            case NO_ENCONTRADA -> throw new DominioException("NO_ENCONTRADA", "No existe esa cita");
            case NO_ES_DE_SU_AGENDA ->
                throw new DominioException("NO_ES_DE_SU_AGENDA", "La cita no es de su agenda");
            case ESTADO_NO_PERMITE -> throw new DominioException("ESTADO_NO_PERMITE",
                    "Solo se finaliza una cita con asistencia confirmada");
        };
    }
}
