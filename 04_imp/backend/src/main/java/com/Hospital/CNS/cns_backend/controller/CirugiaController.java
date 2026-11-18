package com.Hospital.CNS.cns_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaDTOs;
import com.Hospital.CNS.cns_backend.model.cirugias.ProgramacionCirugiaService;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCirugia;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoCatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoDTO;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoVistaDTO;

/**
 * Quirofanos (T100-19, solo ADMINISTRADOR) y cirugias (T100-23, el especialista que opera).
 *
 * <pre>
 *   GET/POST /api/v1/admin/quirofanos · PUT/DELETE /api/v1/admin/quirofanos/{id}
 *   POST /api/v1/especialista/cirugias/propuesta       -> propuesta (no guarda nada)
 *   POST /api/v1/especialista/cirugias                 -> 201 cirugia (o 409 CHOQUE_DE_HORARIO)
 *   GET  /api/v1/especialista/cirugias?fecha=          -> cirugias del dia en sus quirofanos
 *   GET  /api/v1/especialista/quirofanos               -> nombres de sus quirofanos
 *   POST /api/v1/especialista/cirugias/{id}/realizada | /no-se-presento
 *   GET  /api/v1/medico/pacientes/{pacienteId}/cirugias (historial)
 *   GET  /api/v1/cirugias/{id}                         -> para el ticket
 * </pre>
 */
@RestController
public class CirugiaController {

    private final ProgramacionCirugiaService cirugias;
    private final QuirofanoCatalogoService quirofanos;
    private final ResolutorUsuario resolutor;

    public CirugiaController(ProgramacionCirugiaService cirugias, QuirofanoCatalogoService quirofanos,
                             ResolutorUsuario resolutor) {
        this.cirugias = cirugias;
        this.quirofanos = quirofanos;
        this.resolutor = resolutor;
    }

    // ---- Catalogo de quirofanos (administrador) ----

    @GetMapping("/api/v1/admin/quirofanos")
    public List<QuirofanoVistaDTO> listarQuirofanos(@RequestHeader HttpHeaders cabeceras) {
        admin(cabeceras);
        return quirofanos.listar();
    }

    @PostMapping("/api/v1/admin/quirofanos")
    public ResponseEntity<QuirofanoVistaDTO> crearQuirofano(@RequestHeader HttpHeaders cabeceras,
                                                            @RequestBody QuirofanoDTO datos) {
        admin(cabeceras);
        return ResponseEntity.status(HttpStatus.CREATED).body(quirofanos.crear(datos));
    }

    @PutMapping("/api/v1/admin/quirofanos/{id}")
    public QuirofanoVistaDTO editarQuirofano(@RequestHeader HttpHeaders cabeceras, @PathVariable int id,
                                             @RequestBody QuirofanoDTO datos) {
        admin(cabeceras);
        return quirofanos.editar(id, datos);
    }

    @DeleteMapping("/api/v1/admin/quirofanos/{id}")
    public ResponseEntity<Void> desactivarQuirofano(@RequestHeader HttpHeaders cabeceras, @PathVariable int id) {
        admin(cabeceras);
        quirofanos.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Cirugias (especialista) ----

    @PostMapping("/api/v1/especialista/cirugias/propuesta")
    public CirugiaDTOs.Propuesta proponer(@RequestHeader HttpHeaders cabeceras,
                                          @RequestBody CirugiaDTOs.Solicitud solicitud) {
        return cirugias.proponer(solicitud, medico(cabeceras));
    }

    @PostMapping("/api/v1/especialista/cirugias")
    public ResponseEntity<CirugiaDTOs.Vista> confirmar(@RequestHeader HttpHeaders cabeceras,
                                                       @RequestBody CirugiaDTOs.Confirmacion confirmacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cirugias.confirmar(confirmacion, medico(cabeceras)));
    }

    @GetMapping("/api/v1/especialista/cirugias")
    public List<CirugiaDTOs.Vista> delDia(@RequestHeader HttpHeaders cabeceras,
                                          @RequestParam(name = "fecha", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return cirugias.delDia(medico(cabeceras), fecha);
    }

    @GetMapping("/api/v1/especialista/quirofanos")
    public List<String> susQuirofanos(@RequestHeader HttpHeaders cabeceras) {
        return cirugias.quirofanosDe(medico(cabeceras));
    }

    @PostMapping("/api/v1/especialista/cirugias/{id}/realizada")
    public CirugiaDTOs.Vista realizada(@RequestHeader HttpHeaders cabeceras, @PathVariable long id) {
        return cirugias.marcar(id, medico(cabeceras), EstadoCirugia.REALIZADA);
    }

    @PostMapping("/api/v1/especialista/cirugias/{id}/no-se-presento")
    public CirugiaDTOs.Vista noSePresento(@RequestHeader HttpHeaders cabeceras, @PathVariable long id) {
        return cirugias.marcar(id, medico(cabeceras), EstadoCirugia.CANCELADA);
    }

    @GetMapping("/api/v1/medico/pacientes/{pacienteId}/cirugias")
    public List<CirugiaDTOs.Vista> delPaciente(@RequestHeader HttpHeaders cabeceras, @PathVariable long pacienteId) {
        return cirugias.delPaciente(pacienteId, medico(cabeceras));
    }

    @GetMapping("/api/v1/cirugias/{id}")
    public CirugiaDTOs.Vista porId(@RequestHeader HttpHeaders cabeceras, @PathVariable long id) {
        Permisos.exigir(resolutor, cabeceras, "Solo el personal medico ve las cirugias", RolUsuario.MEDICO,
                RolUsuario.ADMINISTRADOR);
        return cirugias.porId(id);
    }

    private long medico(HttpHeaders cabeceras) {
        return Permisos.exigirMedico(resolutor, cabeceras).personaId();
    }

    private void admin(HttpHeaders cabeceras) {
        Permisos.exigir(resolutor, cabeceras, "Solo el administrador gestiona los quirofanos", RolUsuario.ADMINISTRADOR);
    }
}
