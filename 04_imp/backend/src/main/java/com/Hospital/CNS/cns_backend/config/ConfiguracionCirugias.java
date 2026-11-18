package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgCirugiaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgQuirofanoRepository;
import com.Hospital.CNS.cns_backend.model.cirugias.CirugiaRepository;
import com.Hospital.CNS.cns_backend.model.cirugias.ProgramacionCirugiaService;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.NotificacionPublicador;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.fichasinternas.CambioFichaRepository;
import com.Hospital.CNS.cns_backend.model.fichasinternas.FichaInternaRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoCatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.QuirofanoRepository;

/** Quirofanos (T100-19) y cirugias (T100-23): une su model con PostgreSQL. */
@Configuration
public class ConfiguracionCirugias {

    @Bean
    QuirofanoRepository quirofanoRepository() {
        return new PgQuirofanoRepository();
    }

    @Bean
    CirugiaRepository cirugiaRepository() {
        return new PgCirugiaRepository();
    }

    @Bean
    QuirofanoCatalogoService quirofanoCatalogoService(QuirofanoRepository quirofanos, CirugiaRepository cirugias,
                                                      Reloj reloj) {
        return new QuirofanoCatalogoService(quirofanos, id -> cirugias.contarProgramadasDesde(id, reloj.ahora()));
    }

    @Bean
    ProgramacionCirugiaService programacionCirugiaService(CirugiaRepository cirugias, FichaInternaRepository fichas,
                                                          QuirofanoRepository quirofanos, MedicoRepository medicos,
                                                          CitaRepository citas, DiaNoHabilRepository dias,
                                                          CambioFichaRepository cambios, NotificacionPublicador avisos,
                                                          Transaccion transaccion, Reloj reloj) {
        return new ProgramacionCirugiaService(cirugias, fichas, quirofanos, medicos, citas, dias, cambios, avisos,
                transaccion, reloj);
    }
}
