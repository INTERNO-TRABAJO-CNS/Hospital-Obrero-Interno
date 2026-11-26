package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.operaciones.ActualizacionEstadoService;
import com.Hospital.CNS.cns_backend.model.operaciones.ResultadoConfirmacion;
import com.Hospital.CNS.cns_backend.model.compartido.ResultadoFinalizacion;
import com.Hospital.CNS.cns_backend.controller.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citas")
public class EstadoCitaController {

    private final ActualizacionEstadoService service;

    public EstadoCitaController(ActualizacionEstadoService service) {
        this.service = service;
    }

    @PostMapping("/{id}/confirmar-asistencia")
    public ResponseEntity<ResultadoConfirmacion> confirmarAsistencia(
            @PathVariable("id") long id,
            @RequestBody UsuarioAutenticado usuario) {
        ResultadoConfirmacion resultado = service.confirmarAsistencia(id, usuario);
        if (resultado == ResultadoConfirmacion.CONFIRMADA) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.unprocessableEntity().body(resultado);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ResultadoFinalizacion> finalizar(
            @PathVariable("id") long id,
            @RequestParam("medicoId") long medicoId) {
        ResultadoFinalizacion resultado = service.finalizar(id, medicoId);
        if (resultado == ResultadoFinalizacion.FINALIZADA) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.unprocessableEntity().body(resultado);
    }
}