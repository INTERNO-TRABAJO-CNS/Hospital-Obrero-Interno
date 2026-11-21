package com.Hospital.CNS.cns_backend.controller;

import com.Hospital.CNS.cns_backend.model.compartido.CitaDTO;
import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.FichaTvDTO;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.operaciones.CriterioFicha;
import com.Hospital.CNS.cns_backend.model.operaciones.FichasDelDiaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FichasController {

    private final FichasDelDiaService fichasDelDiaService;
    private final ResolutorUsuario resolutor;

    public FichasController(
            FichasDelDiaService fichasDelDiaService,
            ResolutorUsuario resolutor) {
        this.fichasDelDiaService = fichasDelDiaService;
        this.resolutor = resolutor;
    }

    @GetMapping("/fichas/hoy")
    public ResponseEntity<List<CitaDTO>> consultarFichasDelDia(
            @RequestHeader HttpHeaders cabeceras,
            @RequestParam(required = false) CriterioFicha criterio,
            @RequestParam(required = false) String valor) {

        try {
            Permisos.exigir(
                    resolutor,
                    cabeceras,
                    "Solo enfermería puede consultar las fichas del día",
                    RolUsuario.ENFERMERIA
            );

            if (criterio != null) {
                List<CitaDTO> resultado =
                        fichasDelDiaService.buscar(criterio, valor);
                return ResponseEntity.ok(resultado);
            }

            List<CitaDTO> todas =
                    fichasDelDiaService.listadoDelDia();

            return ResponseEntity.ok(todas);

        } catch (IllegalArgumentException e) {
            throw new DominioException("DATO_INVALIDO", e.getMessage(), e);
        }
    }

    @GetMapping("/tv/fichas")
    public ResponseEntity<List<FichaTvDTO>> listadoParaTv() {
        List<FichaTvDTO> resultado =
                fichasDelDiaService.listadoParaTv();

        return ResponseEntity.ok(resultado);
    }
} 