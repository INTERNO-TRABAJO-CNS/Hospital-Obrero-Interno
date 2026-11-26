package com.Hospital.CNS.cns_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.operaciones.FichasDelDiaService;

@Configuration
public class ConfiguracionOperaciones {

    @Bean
    FichasDelDiaService fichasDelDiaService(
            CitaRepository citasRepository,
            Reloj reloj) {
        return new FichasDelDiaService(citasRepository, reloj);
    }
}
