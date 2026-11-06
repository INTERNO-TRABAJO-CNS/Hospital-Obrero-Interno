package com.Hospital.CNS.cns_backend.config;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Hospital.CNS.cns_backend.infrastructure.pg.PgConsultorioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgDiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgHorarioRepository;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgMedicoRepository;
import com.Hospital.CNS.cns_backend.model.compartido.CitaRepository;
import com.Hospital.CNS.cns_backend.model.recursos.CatalogoService;
import com.Hospital.CNS.cns_backend.model.recursos.ConsultorioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.DiaNoHabilRepository;
import com.Hospital.CNS.cns_backend.model.recursos.HorarioRepository;
import com.Hospital.CNS.cns_backend.model.recursos.MedicoRepository;

/**
 * Unico lugar que une el model de recursos con su base de datos. Para cambiar de motor de base de
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

    @Bean
    CatalogoService catalogoService(MedicoRepository medicos, ConsultorioRepository consultorios,
                                    HorarioRepository horarios, DiaNoHabilRepository dias, CitaRepository citas) {
        return new CatalogoService(medicos, consultorios, horarios, dias, citas);
    }

    /**
     * TEMPORAL: lo reemplaza el PgCitaRepository de su tarjeta (T100-03). Mientras no exista,
     * se comporta como "no hay citas": ningun cambio de catalogo afecta citas programadas.
     */
    @Bean
    @ConditionalOnMissingBean(CitaRepository.class)
    CitaRepository citaRepositoryTemporal() {
        return (CitaRepository) Proxy.newProxyInstance(CitaRepository.class.getClassLoader(),
                new Class<?>[] {CitaRepository.class}, (proxy, metodo, args) -> {
                    if (metodo.getDeclaringClass() == Object.class) {
                        return switch (metodo.getName()) {
                            case "toString" -> "CitaRepositoryTemporal";
                            case "hashCode" -> 0;
                            default -> proxy == args[0];
                        };
                    }
                    return valorVacio(metodo.getReturnType());
                });
    }

    private static Object valorVacio(Class<?> tipo) {
        if (tipo == boolean.class) {
            return false;
        }
        if (tipo == int.class) {
            return 0;
        }
        if (tipo == long.class) {
            return 0L;
        }
        if (tipo == List.class) {
            return List.of();
        }
        if (tipo == Optional.class) {
            return Optional.empty();
        }
        return null;
    }
}
