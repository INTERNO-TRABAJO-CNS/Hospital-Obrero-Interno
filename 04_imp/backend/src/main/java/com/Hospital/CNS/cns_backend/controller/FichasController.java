package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.operaciones.CriterioFicha;
import com.Hospital.CNS.cns_backend.model.operaciones.FichasDelDiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FichasController {

    private final FichasDelDiaService fichasDelDiaService;

    public FichasController(FichasDelDiaService fichasDelDiaService) {
        this.fichasDelDiaService = fichasDelDiaService;
    }

    @GetMapping("/fichas/hoy")
    public ResponseEntity<List<CitaDTO>> consultarFichasDelDia(
            @RequestParam(required = false) CriterioFicha criterio,
            @RequestParam(required = false) String valor) {
        try {
            if (criterio != null && valor != null) {
                List<CitaDTO> resultado = fichasDelDiaService.buscar(criterio, valor);
                return ResponseEntity.ok(resultado);
            }
            List<CitaDTO> todas = fichasDelDiaService.listadoParaTv();
            return ResponseEntity.ok(todas);
        } catch (IllegalArgumentException e) {
            // Regla de negocio: fecha distinta o criterio inválido -> 422 Unprocessable Entity
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/tv/fichas")
    public ResponseEntity<List<CitaDTO>> listadoParaTv() {
        List<CitaDTO> resultado = fichasDelDiaService.listadoParaTv();
        return ResponseEntity.ok(resultado);
    }
}
