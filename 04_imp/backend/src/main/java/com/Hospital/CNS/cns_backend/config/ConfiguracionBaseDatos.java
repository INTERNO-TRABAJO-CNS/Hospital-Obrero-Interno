package com.Hospital.CNS.cns_backend.config;

import java.time.ZoneId;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgTransaccion;
import com.Hospital.CNS.cns_backend.lib.DB;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RelojSistema;
import com.Hospital.CNS.cns_backend.model.compartido.Transaccion;

/**
 * Une la base de datos con el model: los Pg*Repository usan el pool de Spring (Hikari) a traves de
 * {@link DB}, y los Service reciben la {@link Transaccion} real y el reloj con la hora del hospital.
 */
@Configuration
public class ConfiguracionBaseDatos {

    public ConfiguracionBaseDatos(DataSource dataSource) {
        DB.usar(dataSource);
    }

    @Bean
    Transaccion transaccion() {
        return new PgTransaccion();
    }

    @Bean
    @ConditionalOnMissingBean(Reloj.class)
    Reloj reloj(@Value("${cns.zona-horaria:America/La_Paz}") String zona) {
        return new RelojSistema(ZoneId.of(zona));
    }
}
