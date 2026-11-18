package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.atencion.AtencionService;
import com.Hospital.CNS.cns_backend.model.atencion.DatosAtencionDTO;
import com.Hospital.CNS.cns_backend.model.atencion.ResultadoAtencion;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;

/**
 * CU-15: datos de la cita en atencion y registro de receta + fin. Un resultado distinto de REGISTRADA
 * sale como error {codigo, mensaje}: RECETA_INVALIDA 400, NO_ES_DE_SU_AGENDA 403, NO_ENCONTRADA 404,
 * ESTADO_NO_PERMITE 409.
 */
@RestController
@RequestMapping("/api/v1/citas")
public class AtencionController {

    /** Cuerpo del POST. */
    public record RegistroAtencionRequest(String medicacion) {
    }

    /** Respuesta del POST cuando se registro. */
    public record RegistroAtencionResponse(ResultadoAtencion resultado, String mensaje) {
    }

    private final AtencionService atencion;
    private final ResolutorUsuario resolutor;

    public AtencionController(AtencionService atencion, ResolutorUsuario resolutor) {
        this.atencion = atencion;
        this.resolutor = resolutor;
    }

    @GetMapping("/{citaId}/atencion")
    public DatosAtencionDTO datos(@RequestHeader HttpHeaders cabeceras, @PathVariable("citaId") long citaId) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return atencion.datosParaAtencion(citaId, medico.personaId());
    }

    @PostMapping("/{citaId}/atencion")
    public RegistroAtencionResponse registrar(@RequestHeader HttpHeaders cabeceras, @PathVariable("citaId") long citaId,
                                              @RequestBody RegistroAtencionRequest cuerpo) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        ResultadoAtencion r = atencion.registrar(citaId, medico.personaId(), cuerpo == null ? null : cuerpo.medicacion());
        if (r != ResultadoAtencion.REGISTRADA) {
            throw new DominioException(r.name(), AtencionService.mensaje(r));
        }
        return new RegistroAtencionResponse(r, AtencionService.mensaje(r));
    }
}
