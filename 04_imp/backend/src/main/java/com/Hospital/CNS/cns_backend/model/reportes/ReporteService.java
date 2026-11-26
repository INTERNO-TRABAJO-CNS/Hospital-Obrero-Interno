package com.Hospital.CNS.cns_backend.model.reportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.Hospital.CNS.cns_backend.model.compartido.DominioException;
import com.Hospital.CNS.cns_backend.model.compartido.EstadoCita;
import com.Hospital.CNS.cns_backend.model.compartido.Reloj;
import com.Hospital.CNS.cns_backend.model.compartido.TipoEspecialidad;

/**
 * CU-19 (reportes de citas por estado, medico, especialidad y consultorio) y CU-20 (indicadores de gestion). Solo
 * lectura: ningun metodo modifica citas.
 *
 * <p>Flujos alternativos: [1a] sin rol ADMINISTRADOR lo corta el controller (ACCESO_DENEGADO); [3a] sin datos el
 * reporte sale vacio (o el indicador en null) y la pantalla lo informa; periodo invalido: DATOS_INVALIDOS.</p>
 */
public class ReporteService {

    /** Meta minima de fichas por medico y dia (RF-20). */
    public static final int META_MINIMA = 5;

    /** Maximo de fichas por medico y dia (RF-06). */
    public static final int MAXIMO_DIARIO = 7;

    private final ReporteRepository repositorio;
    private final Reloj reloj;

    public ReporteService(ReporteRepository repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /** Citas del periodo por estado: siempre los 4 estados, en el orden del ciclo de vida (0 si no hay). */
    public List<ReporteEstadoDTO> porEstado(LocalDate d, LocalDate h) {
        validarPeriodo(d, h);
        Map<EstadoCita, ReporteEstadoDTO> porEstado = new EnumMap<>(EstadoCita.class);
        for (ReporteEstadoDTO fila : repositorio.citasPorEstado(d, h)) {
            porEstado.put(fila.estado(), fila);
        }
        List<ReporteEstadoDTO> resultado = new ArrayList<>();
        for (EstadoCita estado : EstadoCita.values()) {
            resultado.add(porEstado.getOrDefault(estado, new ReporteEstadoDTO(estado, 0, 0)));
        }
        return resultado;
    }

    /**
     * Cumplimiento de la meta diaria por medico: fichas atendidas en los dias que tuvo fichas, hasta hoy (los dias
     * futuros del periodo todavia no se pueden cumplir). Marca {@code bajoMeta} si el promedio es menor a 5.
     */
    public List<CumplimientoMedicoDTO> cumplimiento(LocalDate d, LocalDate h) {
        validarPeriodo(d, h);
        LocalDate hoy = reloj.ahora().toLocalDate();
        LocalDate hasta = h.isAfter(hoy) ? hoy : h;
        if (d.isAfter(hasta)) {
            return List.of();
        }
        Map<Long, List<ReporteRepository.AtendidasDia>> porMedico = new LinkedHashMap<>();
        for (ReporteRepository.AtendidasDia dia : repositorio.atendidasPorMedicoYDia(d, hasta)) {
            porMedico.computeIfAbsent(dia.medicoId(), id -> new ArrayList<>()).add(dia);
        }
        List<CumplimientoMedicoDTO> resultado = new ArrayList<>();
        for (List<ReporteRepository.AtendidasDia> dias : porMedico.values()) {
            ReporteRepository.AtendidasDia primero = dias.get(0);
            int atendidas = dias.stream().mapToInt(ReporteRepository.AtendidasDia::atendidas).sum();
            int diasBajoMeta = (int) dias.stream().filter(x -> x.atendidas() < META_MINIMA).count();
            double promedio = (double) atendidas / dias.size();
            resultado.add(new CumplimientoMedicoDTO(primero.medicoId(), primero.nombreMedico(),
                    primero.especialidad(), dias.size(), atendidas, promedio, diasBajoMeta, promedio < META_MINIMA));
        }
        resultado.sort(Comparator.comparing(CumplimientoMedicoDTO::especialidad)
                .thenComparing(CumplimientoMedicoDTO::nombreMedico));
        return resultado;
    }

    /** Demanda del periodo: solicitudes por especialidad basica (0 si no hubo). */
    public List<ReporteGrupoDTO> porEspecialidad(LocalDate d, LocalDate h) {
        validarPeriodo(d, h);
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (TipoEspecialidad e : TipoEspecialidad.values()) {
            if (e.esBasica()) {
                cantidades.put(e.name(), 0);
            }
        }
        for (ReporteGrupoDTO fila : repositorio.solicitudesPorEspecialidad(d, h)) {
            cantidades.merge(fila.grupo(), fila.cantidad(), Integer::sum);
        }
        return cantidades.entrySet().stream().map(x -> new ReporteGrupoDTO(x.getKey(), x.getValue())).toList();
    }

    /** Fichas atendidas en el periodo por consultorio (solo los consultorios que atendieron). */
    public List<ReporteGrupoDTO> porConsultorio(LocalDate d, LocalDate h) {
        validarPeriodo(d, h);
        return repositorio.atendidasPorConsultorio(d, h).stream()
                .sorted(Comparator.comparing(ReporteGrupoDTO::grupo))
                .toList();
    }

    /** CU-20: indicadores de demanda, cancelaciones, carga de trabajo y tiempo confirmacion -> fin. */
    public IndicadoresDTO indicadores(LocalDate d, LocalDate h) {
        validarPeriodo(d, h);
        return repositorio.indicadores(d, h);
    }

    private static void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new DominioException("DATOS_INVALIDOS", "Indique la fecha desde y la fecha hasta del reporte");
        }
        if (desde.isAfter(hasta)) {
            throw new DominioException("DATOS_INVALIDOS", "La fecha desde no puede ser posterior a la fecha hasta");
        }
    }
}
