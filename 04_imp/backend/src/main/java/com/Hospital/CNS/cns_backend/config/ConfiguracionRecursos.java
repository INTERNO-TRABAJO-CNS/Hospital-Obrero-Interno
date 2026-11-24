package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgCitaRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgConsultorioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgHorarioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgMedicoRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;
import com.Hospital.CNS.cns_backend.model.recursos.CatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Unico lugar que une el model de recursos (y las citas) con su base de datos. Para cambiar de motor de base de
 * datos solo se reemplazan las clases Pg* de aqui: model y controller no se tocan.
 */
@Configuration
public class ConfiguracionRecursos {

    @Bean
    MedicoRepository medicoRepository() {
        return new PgMedicoRepository();
    }

    @Bean
    ConsultorioRepository consultorioRepository() {
        return new PgConsultorioRepository();
    }

    @Bean
    HorarioRepository horarioRepository() {
        return new PgHorarioRepository();
    }

    @Bean
    DiaNoHabilRepository diaNoHabilRepository() {
        return new PgDiaNoHabilRepository();
    }

    /** Tabla citas.cita (migracion V5). La asignacion automatica que crea las citas es de T100-03. */
    @Bean
    CitaRepository citaRepository() {
        return new PgCitaRepository();
    }

    @Bean
    CatalogoService catalogoService(MedicoRepository medicos, ConsultorioRepository consultorios,
                                    HorarioRepository horarios, DiaNoHabilRepository dias, CitaRepository citas,
                                    Reloj reloj, Transaccion transaccion) {
        return new CatalogoService(medicos, consultorios, horarios, dias, citas, reloj, transaccion);
    }
}
