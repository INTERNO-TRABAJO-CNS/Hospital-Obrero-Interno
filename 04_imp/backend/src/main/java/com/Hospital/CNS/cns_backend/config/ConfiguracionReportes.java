package com.Hospital.CNS.cns_backend.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.Hospital.CNS.cns_backend.controller.AutorizacionWebSocket;
import com.Hospital.CNS.cns_backend.controller.ResolutorUsuario;
import com.Hospital.CNS.cns_backend.infrastructure.pg.PgReporteRepository;
import com.Hospital.CNS.cns_backend.infrastructure.ws.AgendaEventosWs;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.RolUsuario;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteRepository;
import com.Hospital.CNS.cns_backend.model.reportes.ReporteService;
import com.Hospital.CNS.cns_backend.model.reportes.SupervisionService;

import tools.jackson.databind.json.JsonMapper;

/**
 * T100-04 (CU-19/CU-20): reportes, indicadores y supervision del administrador, y los WebSocket de tiempo real
 * (RNF-08). AgendaEventosWs reemplaza en produccion al AgendaEventos que solo escribia en el log.
 *
 * <pre>
 * /ws/supervision  solo ADMINISTRADOR            (agenda de supervision, CU-19)
 * /ws/fichas       ENFERMERIA o ADMINISTRADOR    (fichas del dia)
 * </pre>
 *
 * El token de sesion va en {@code ?token=}. Los origenes permitidos son los de CORS ({@code cns.web.origen}).
 */
@Configuration
@EnableWebSocket
public class ConfiguracionReportes implements WebSocketConfigurer {

    private final AgendaEventosWs eventos;
    private final ResolutorUsuario resolutor;
    private final String[] origenes;

    public ConfiguracionReportes(AgendaEventosWs eventos, ResolutorUsuario resolutor,
                                 @Value("${cns.web.origen:http://localhost:5050}") String origen) {
        this.eventos = eventos;
        this.resolutor = resolutor;
        this.origenes = origen.split("\\s*,\\s*");
    }

    /** Un solo AgendaEventos en la aplicacion: lo usan CitaService, ActualizacionEstadoService, etc. */
    @Bean
    static AgendaEventosWs agendaEventos(ObjectProvider<JsonMapper> json) {
        return new AgendaEventosWs(json.getIfAvailable(() -> JsonMapper.builder().build()));
    }

    @Bean
    ReporteRepository reporteRepository() {
        return new PgReporteRepository();
    }

    @Bean
    SupervisionService supervisionService(ReporteRepository repositorio, Reloj reloj) {
        return new SupervisionService(repositorio, reloj);
    }

    @Bean
    ReporteService reporteService(ReporteRepository repositorio, Reloj reloj) {
        return new ReporteService(repositorio, reloj);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(eventos.supervision(), "/ws/supervision")
                .addInterceptors(new AutorizacionWebSocket(resolutor, RolUsuario.ADMINISTRADOR))
                .setAllowedOriginPatterns(origenes);
        registro.addHandler(eventos.fichas(), "/ws/fichas")
                .addInterceptors(new AutorizacionWebSocket(resolutor, RolUsuario.ENFERMERIA, RolUsuario.ADMINISTRADOR))
                .setAllowedOriginPatterns(origenes);
    }
}
