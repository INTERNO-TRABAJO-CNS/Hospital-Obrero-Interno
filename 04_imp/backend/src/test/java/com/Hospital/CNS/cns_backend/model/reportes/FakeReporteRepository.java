package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReporteRepository en memoria para probar los services sin BD. Las pruebas cargan las filas ya agregadas, como las
 * devolveria PgReporteRepository (en el orden que quieran: el service es quien ordena).
 */
public class FakeReporteRepository implements ReporteRepository {

    public final Map<LocalDate, List<ResumenEspecialidadDTO>> resumenes = new HashMap<>();
    public final Map<String, List<FichaSupervisionDTO>> fichas = new HashMap<>();
    public final List<ReporteEstadoDTO> estados = new ArrayList<>();
    public final List<AtendidasDia> atendidas = new ArrayList<>();
    public final List<ReporteGrupoDTO> solicitudes = new ArrayList<>();
    public final List<ReporteGrupoDTO> consultorios = new ArrayList<>();
    public IndicadoresDTO indicadores = new IndicadoresDTO(0, 0, 0, 0, null, null, 0);

    /** Periodo de la ultima consulta de atendidas (para comprobar que el service recorta los dias futuros). */
    public LocalDate ultimoDesde;
    public LocalDate ultimoHasta;

    public void fichasDe(long medicoId, LocalDate fecha, List<FichaSupervisionDTO> lista) {
        fichas.put(medicoId + "@" + fecha, lista);
    }

    @Override
    public List<ResumenEspecialidadDTO> resumenDelDia(LocalDate fecha) {
        return resumenes.getOrDefault(fecha, List.of());
    }

    @Override
    public List<FichaSupervisionDTO> fichasDeMedico(long medicoId, LocalDate fecha) {
        return fichas.getOrDefault(medicoId + "@" + fecha, List.of());
    }

    @Override
    public List<ReporteEstadoDTO> citasPorEstado(LocalDate desde, LocalDate hasta) {
        return estados;
    }

    @Override
    public List<AtendidasDia> atendidasPorMedicoYDia(LocalDate desde, LocalDate hasta) {
        ultimoDesde = desde;
        ultimoHasta = hasta;
        return atendidas.stream().filter(a -> !a.fecha().isBefore(desde) && !a.fecha().isAfter(hasta)).toList();
    }

    @Override
    public List<ReporteGrupoDTO> solicitudesPorEspecialidad(LocalDate desde, LocalDate hasta) {
        return solicitudes;
    }

    @Override
    public List<ReporteGrupoDTO> atendidasPorConsultorio(LocalDate desde, LocalDate hasta) {
        return consultorios;
    }

    @Override
    public IndicadoresDTO indicadores(LocalDate desde, LocalDate hasta) {
        return indicadores;
    }
}
