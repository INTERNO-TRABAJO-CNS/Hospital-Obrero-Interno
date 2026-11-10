package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.atencion.AtencionException;
import com.Hospital.CNS.cns_backend.model.atencion.AtencionService;
import com.Hospital.CNS.cns_backend.model.atencion.DatosAtencionDTO;
import com.Hospital.CNS.cns_backend.model.atencion.ResultadoAtencion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
public class AtencionController {
    private final AtencionService service;

    public AtencionController(AtencionService service) {
        this.service = service;
    }

    @GetMapping("/{citaId}/atencion")
    public DatosAtencionDTO datos(
            @PathVariable long citaId,
            @RequestParam long medicoId) {
        return service.datosParaAtencion(citaId, medicoId);
    }

    @PostMapping("/{citaId}/atencion")
    public ResponseEntity<ResultadoAtencion> registrar(
            @PathVariable long citaId,
            @RequestParam long medicoId,
            @RequestBody MedicacionRequest request) {
        ResultadoAtencion resultado = service.registrar(citaId, medicoId, request.medicacion());
        return ResponseEntity.status(statusFor(resultado)).body(resultado);
    }

    private HttpStatus statusFor(ResultadoAtencion resultado) {
        return switch (resultado) {
            case REGISTRADA -> HttpStatus.OK;
            case RECETA_INVALIDA -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NO_ES_DE_SU_AGENDA -> HttpStatus.FORBIDDEN;
            case ESTADO_NO_PERMITE -> HttpStatus.CONFLICT;
        };
    }

    public record MedicacionRequest(String medicacion) {
    }
}