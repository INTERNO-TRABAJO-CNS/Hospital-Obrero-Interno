import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/medico.dart';
import '../models/comun/fechas.dart';
import '../models/comun/fechas_iso.dart';
import '../models/enfermeria/ficha_dia.dart' show EstadoCita;
import '../models/reportes/reporte.dart';
import '../models/reportes/reporte_api.dart';
import 'catalogo_service.dart' show listaDe, unoDe;

/// Reportes e indicadores del administrador.
abstract class ReportesService {
  /// Reporte de la semana actual (lunes a viernes).
  Future<Reporte> reporte(TipoReporte tipo);

  /// `true` si ese reporte muestra datos de prueba (sin backend).
  bool esDePrueba(TipoReporte tipo);
}

/// CU-19/CU-20 con el backend (GET /api/v1/admin/reportes/...), siempre de la semana actual (lunes a
/// viernes). Un indicador sin datos que lo respalden se muestra como «Sin datos» (CU-20 3a). Fichas
/// internas y quirofanos todavia no se calculan en el backend: se piden a [respaldo] (datos de prueba).
class ApiReportesService implements ReportesService {
  ApiReportesService(this._api, {required ReportesService respaldo, DateTime Function()? reloj})
      : _respaldo = respaldo,
        _reloj = reloj ?? DateTime.now;

  final ApiClient _api;
  final ReportesService _respaldo;
  final DateTime Function() _reloj;

  static const String _base = '/api/v1/admin/reportes';
  static const String sinDatos = 'Sin datos';
  static const String _notaTiempos = 'Tiempos de espera y de atención por separado no se miden: solo confirmación → fin';

  @override
  bool esDePrueba(TipoReporte tipo) => tipo == TipoReporte.internasYQuirofanos;

  @override
  Future<Reporte> reporte(TipoReporte tipo) {
    switch (tipo) {
      case TipoReporte.porMedico:
        return _porMedico();
      case TipoReporte.porEstado:
        return _porEstado();
      case TipoReporte.porEspecialidad:
        return _porEspecialidad();
      case TipoReporte.porConsultorio:
        return _porConsultorio();
      case TipoReporte.internasYQuirofanos:
        return _respaldo.reporte(tipo);
    }
  }

  /// Lunes y viernes de la semana actual.
  Map<String, String> _semana() {
    final hoy = soloFecha(_reloj());
    final lunes = hoy.subtract(Duration(days: hoy.weekday - 1));
    return {'desde': isoDe(lunes), 'hasta': isoDe(lunes.add(const Duration(days: 4)))};
  }

  Future<List<T>> _lista<T>(String ruta, T Function(Map<String, dynamic>) convertir) async =>
      listaDe(await _api.get('$_base/$ruta', consulta: _semana()), convertir);

  Future<IndicadoresGestion> _indicadores() async =>
      unoDe(await _api.get('$_base/indicadores', consulta: _semana()), IndicadoresGestion.desdeJson);

  Future<Reporte> _porMedico() async {
    final (medicos, ind) = await (_lista('cumplimiento', CumplimientoMedico.desdeJson), _indicadores()).wait;
    final carga = ind.fichasPorMedicoDia;
    final minutos = ind.minutosConfirmacionFin;
    return Reporte(
      titulo: 'Fichas atendidas por día (meta mínima $cupoMinimo, máximo $cupoMaximo)',
      filas: [
        for (final m in medicos)
          FilaGrafico(
            etiqueta: m.nombreMedico,
            valor: m.promedioDiario,
            texto: m.bajoMeta
                ? '${_numero(m.promedioDiario)} fichas · bajo la meta'
                : '${_numero(m.promedioDiario)} fichas',
            alerta: m.bajoMeta,
          ),
      ],
      maximo: cupoMaximo,
      indicadores: [
        Indicador('${ind.solicitudes} solicitudes', 'Demanda (semana)'),
        Indicador('${ind.canceladas} (${ind.canceladasAutomaticas} automáticas)', 'Cancelaciones'),
        Indicador(carga == null ? sinDatos : '${carga.toStringAsFixed(1)} fichas/médico/día', 'Carga de trabajo'),
        Indicador('${ind.sinCupo} solicitudes', 'Sin cupo'),
      ],
      nota: minutos == null ? '$_notaTiempos.' : '$_notaTiempos (promedio de la semana: ${minutos.round()} min).',
    );
  }

  Future<Reporte> _porEstado() async {
    final conteos = await _lista('estado', ConteoEstado.desdeJson);
    ConteoEstado de(EstadoCita e) => conteos.firstWhere((c) => c.estado == e.codigo,
        orElse: () => ConteoEstado(estado: e.codigo, cantidad: 0, automaticas: 0));
    final atendidas = de(EstadoCita.atendida);
    final confirmadas = de(EstadoCita.asistenciaConfirmada);
    final programadas = de(EstadoCita.programada);
    final canceladas = de(EstadoCita.cancelada);
    final total = conteos.fold(0, (n, c) => n + c.cantidad);
    String porcentaje(int n) => total == 0 ? sinDatos : '${(n * 100 / total).round()} %';
    return Reporte(
      titulo: 'Fichas por estado (semana ${semanaLaboral(_reloj())})',
      filas: [
        FilaGrafico(etiqueta: 'Atendidas', valor: atendidas.cantidad, texto: '${atendidas.cantidad} fichas'),
        FilaGrafico(
            etiqueta: 'Asist. confirmada', valor: confirmadas.cantidad, texto: '${confirmadas.cantidad} fichas'),
        FilaGrafico(etiqueta: 'Programadas', valor: programadas.cantidad, texto: '${programadas.cantidad} fichas'),
        FilaGrafico(
          etiqueta: 'Canceladas',
          valor: canceladas.cantidad,
          texto: '${canceladas.cantidad} fichas (${canceladas.automaticas} automáticas)',
          alerta: true,
        ),
      ],
      maximo: total,
      indicadores: [
        Indicador('$total fichas', 'Total (semana)'),
        Indicador(porcentaje(atendidas.cantidad), 'Atendidas'),
        Indicador(porcentaje(canceladas.cantidad), 'Canceladas'),
        Indicador('${canceladas.automaticas}', 'Canceladas automáticamente'),
      ],
      nota: 'Cancelada (automática): el paciente no confirmó su llegada 30 minutos antes de la cita.',
    );
  }

  Future<Reporte> _porEspecialidad() async {
    final (grupos, ind) = await (_lista('especialidad', ConteoGrupo.desdeJson), _indicadores()).wait;
    final total = grupos.fold(0, (n, g) => n + g.cantidad);
    final orden = [...grupos]..sort((a, b) => b.cantidad.compareTo(a.cantidad));
    String nombre(String codigo) => Especialidad.desdeCodigo(codigo).nombre;
    return Reporte(
      titulo: 'Solicitudes por especialidad básica (semana ${semanaLaboral(_reloj())})',
      filas: [
        for (final g in orden) FilaGrafico(etiqueta: nombre(g.grupo), valor: g.cantidad, texto: '${g.cantidad} solicitudes'),
      ],
      maximo: orden.isEmpty ? 0 : orden.first.cantidad,
      indicadores: [
        Indicador('$total solicitudes', 'Demanda (semana)'),
        Indicador('${ind.sinCupo} solicitudes', 'Sin cupo'),
        Indicador(total == 0 ? sinDatos : nombre(orden.first.grupo), 'La más pedida'),
        Indicador(total == 0 ? sinDatos : nombre(orden.last.grupo), 'La menos pedida'),
      ],
      nota: 'Las especialidades internas no las pide el paciente: llegan por ficha interna '
          '(ver «Fichas internas y quirófanos»).',
    );
  }

  Future<Reporte> _porConsultorio() async {
    final grupos = await _lista('consultorio', ConteoGrupo.desdeJson);
    final total = grupos.fold(0, (n, g) => n + g.cantidad);
    final masUsado = grupos.isEmpty ? null : grupos.reduce((a, b) => b.cantidad > a.cantidad ? b : a);
    return Reporte(
      titulo: 'Fichas atendidas por consultorio (semana ${semanaLaboral(_reloj())})',
      filas: [
        for (final g in grupos) FilaGrafico(etiqueta: g.grupo, valor: g.cantidad, texto: '${g.cantidad} fichas'),
      ],
      maximo: masUsado?.cantidad ?? 0,
      indicadores: [
        Indicador('${grupos.length}', 'Consultorios en uso'),
        Indicador(masUsado?.grupo ?? sinDatos, 'El más usado'),
        Indicador('$total fichas', 'Atendidas (semana)'),
        Indicador(grupos.isEmpty ? sinDatos : (total / grupos.length).toStringAsFixed(1), 'Fichas por consultorio'),
      ],
      nota: 'Cuenta las fichas atendidas en cada consultorio, sin importar el médico.',
    );
  }

  /// 7 -> "7"; 5.5 -> "5.5".
  static String _numero(double n) => n == n.roundToDouble() ? '${n.round()}' : n.toStringAsFixed(1);
}

/// De prueba: cifras fijas parecidas a las del Figma.
class FakeReportesService implements ReportesService {
  FakeReportesService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now;

  final DateTime Function() _reloj;

  @override
  bool esDePrueba(TipoReporte tipo) => true;

  @override
  Future<Reporte> reporte(TipoReporte tipo) async {
    final semana = semanaLaboral(_reloj());
    switch (tipo) {
      case TipoReporte.porMedico:
        FilaGrafico medico(String nombre, int fichas) => FilaGrafico(
              etiqueta: nombre,
              valor: fichas,
              texto: fichas < cupoMinimo ? '$fichas fichas · bajo la meta' : '$fichas fichas',
              alerta: fichas < cupoMinimo,
            );
        return Reporte(
          titulo: 'Fichas atendidas por día (meta mínima $cupoMinimo, máximo $cupoMaximo)',
          filas: [medico('Dr. Ramos', 7), medico('Dra. Quispe', 6), medico('Dr. Mamani', 4), medico('Dra. Salas', 5)],
          maximo: cupoMaximo,
          indicadores: const [
            Indicador('86 solicitudes', 'Demanda (semana)'),
            Indicador('9 (4 automáticas)', 'Cancelaciones'),
            Indicador('5.5 fichas/médico/día', 'Carga de trabajo'),
            Indicador('3 solicitudes', 'Sin cupo'),
          ],
          nota: 'Tiempos de espera y de atención por separado no se miden: solo confirmación → fin.',
        );
      case TipoReporte.porEstado:
        return Reporte(
          titulo: 'Fichas por estado (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Atendidas', valor: 61, texto: '61 fichas'),
            FilaGrafico(etiqueta: 'Asist. confirmada', valor: 8, texto: '8 fichas'),
            FilaGrafico(etiqueta: 'Programadas', valor: 8, texto: '8 fichas'),
            FilaGrafico(etiqueta: 'Canceladas', valor: 9, texto: '9 fichas (4 automáticas)', alerta: true),
          ],
          maximo: 86,
          indicadores: const [
            Indicador('86 fichas', 'Total (semana)'),
            Indicador('71 %', 'Atendidas'),
            Indicador('10 %', 'Canceladas'),
            Indicador('4', 'Canceladas automáticamente'),
          ],
          nota: 'Cancelada (automática): el paciente no confirmó su llegada 30 minutos antes de la cita.',
        );
      case TipoReporte.porEspecialidad:
        return Reporte(
          titulo: 'Solicitudes por especialidad básica (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Consultorio Familiar', valor: 41, texto: '41 solicitudes'),
            FilaGrafico(etiqueta: 'Pediatría', valor: 26, texto: '26 solicitudes'),
            FilaGrafico(etiqueta: 'Odontología', valor: 19, texto: '19 solicitudes'),
          ],
          maximo: 41,
          indicadores: const [
            Indicador('86 solicitudes', 'Demanda (semana)'),
            Indicador('3 solicitudes', 'Sin cupo'),
            Indicador('Consultorio Familiar', 'La más pedida'),
            Indicador('Odontología', 'La menos pedida'),
          ],
          nota: 'Las especialidades internas no las pide el paciente: llegan por ficha interna '
              '(ver «Fichas internas y quirófanos»).',
        );
      case TipoReporte.porConsultorio:
        return Reporte(
          titulo: 'Fichas atendidas por consultorio (semana $semana)',
          filas: const [
            FilaGrafico(etiqueta: 'Consultorio 1', valor: 9, texto: '9 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 2', valor: 14, texto: '14 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 4', valor: 22, texto: '22 fichas'),
            FilaGrafico(etiqueta: 'Consultorio 6', valor: 16, texto: '16 fichas'),
          ],
          maximo: 22,
          indicadores: const [
            Indicador('4', 'Consultorios en uso'),
            Indicador('Consultorio 4', 'El más usado'),
            Indicador('61 fichas', 'Atendidas (semana)'),
            Indicador('15.3', 'Fichas por consultorio'),
          ],
          nota: 'Cuenta las fichas atendidas en cada consultorio, sin importar el médico.',
        );
      case TipoReporte.internasYQuirofanos:
        FilaGrafico interna(String nombre, int fichas) =>
            FilaGrafico(etiqueta: nombre, valor: fichas, texto: '$fichas fichas');
        return Reporte(
          titulo: 'Fichas internas por especialidad (semana $semana)',
          filas: [
            interna('Gastroenterología', 38),
            interna('Cardiología', 31),
            interna('Dermatología', 24),
            interna('Neumología', 19),
            interna('Endocrinología', 17),
            interna('Hematología', 12),
            interna('Nefrología', 11),
            interna('Infectología', 9),
            interna('Medicina del Trabajo', 6),
            interna('Terapia Intensiva', 3),
          ],
          maximo: 40,
          indicadores: const [
            Indicador('6 %', 'No se presentó (fichas internas)'),
            Indicador('31 h / 144 h', 'Horas de quirófano usadas / abiertas'),
            Indicador('4', 'Desplazamientos (fichas y cirugías)'),
            Indicador('3', 'Fichas en espera ahora'),
          ],
          nota: 'La gravedad no aparece en reportes. Los desplazamientos cuentan las fichas y cirugías movidas '
              'por un caso de mayor prioridad.',
        );
    }
  }
}
