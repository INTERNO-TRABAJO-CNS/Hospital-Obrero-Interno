package com.Hospital.CNS.cns_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.recursos.MedicoActualDTO;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoActualService;

/** T100-18: GET /api/v1/medico/yo -> datos del medico autenticado (403 si no es MEDICO). */
@RestController
@RequestMapping("/api/v1/medico")
public class MedicoActualController {

    private final MedicoActualService service;
    private final ResolutorUsuario resolutor;

    public MedicoActualController(MedicoActualService service, ResolutorUsuario resolutor) {
        this.service = service;
        this.resolutor = resolutor;
    }

    @GetMapping("/yo")
    public MedicoActualDTO yo(@RequestHeader HttpHeaders cabeceras) {
        UsuarioAutenticado medico = Permisos.exigirMedico(resolutor, cabeceras);
        return service.yo(medico.personaId());
    }
}
