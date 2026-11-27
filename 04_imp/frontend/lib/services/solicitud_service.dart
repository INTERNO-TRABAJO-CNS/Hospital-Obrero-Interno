import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas.dart';
import '../models/comun/fechas_iso.dart';
import '../models/solicitud/solicitud.dart';

/// CU-04: el paciente elige especialidad y dia (RF-04, RU-01, RN-01); medico, hora y consultorio los asigna el
/// sistema. El titular tambien pide para sus beneficiarios (RN-05).
abstract class SolicitudService {
  /// `POST /api/v1/solicitudes`. [pacienteId]: el beneficiario; null = el mismo titular.
  /// Errores: DIA_NO_HABIL, FUERA_DE_RANGO, YA_TIENE_CITA_ESE_DIA, YA_TIENE_SOLICITUD_ACTIVA, EDAD_NO_PEDIATRICA.
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad, DateTime dia, {int? pacienteId});

  /// `GET /api/v1/solicitudes/dias-disponibles`: dias habiles desde hoy hasta 15 dias habiles despues.
  Future<List<DateTime>> diasDisponibles();

  Future<ResultadoSolicitud> consultar(int solicitudId);

  Future<List<ResultadoSolicitud>> listar();
}

class ApiSolicitudService implements SolicitudService {
  ApiSolicitudService(this._api);

  final ApiClient _api;

  @override
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad, DateTime dia, {int? pacienteId}) async {
    final respuesta = await _api.post('/api/v1/solicitudes', {
      'especialidad': especialidad.codigo,
      'dia': isoDe(dia),
      if (pacienteId != null) 'pacienteId': pacienteId,
    });

    return leerRespuesta(
      () => ResultadoSolicitud.fromJson(respuesta),
    );
  }

  @override
  Future<List<DateTime>> diasDisponibles() async {
    final respuesta = await _api.get('/api/v1/solicitudes/dias-disponibles');
    return leerRespuesta(() {
      final datos = respuesta['datos'];
      if (datos is! List) {
        throw const FormatException('se esperaba una lista de dias');
      }
      return [for (final d in datos) DateTime.parse(d as String)];
    });
  }

  @override
  Future<ResultadoSolicitud> consultar(int solicitudId) async {
    final respuesta = await _api.get(
      '/api/v1/solicitudes/$solicitudId',
    );

    return leerRespuesta(
      () => ResultadoSolicitud.fromJson(respuesta),
    );
  }

  @override
  Future<List<ResultadoSolicitud>> listar() async {
    final respuesta = await _api.get('/api/v1/solicitudes');

    final datos = respuesta['datos'];

    if (datos is! List) {
      throw const FormatException(
        'La respuesta de Mis citas no contiene una lista válida',
      );
    }

    return leerRespuesta(() {
      return datos.map((item) {
        if (item is! Map) {
          throw const FormatException(
            'Una solicitud recibida tiene un formato inválido',
          );
        }

        return ResultadoSolicitud.fromJson(
          Map<String, dynamic>.from(item),
        );
      }).toList();
    });
  }
}

/// En memoria (AUTH_FAKE y pruebas): dias habiles de lunes a viernes y las mismas reglas basicas del backend.
class FakeSolicitudService implements SolicitudService {
  FakeSolicitudService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now;

  final DateTime Function() _reloj;
  final List<ResultadoSolicitud> solicitudes = [];
  int _siguiente = 500;

  @override
  Future<List<DateTime>> diasDisponibles() async {
    final dias = <DateTime>[];
    var dia = soloFecha(_reloj());
    while (dias.length < 16) {
      if (dia.weekday != DateTime.saturday && dia.weekday != DateTime.sunday) {
        dias.add(dia);
      }
      dia = DateTime(dia.year, dia.month, dia.day + 1);
    }
    return dias;
  }

  @override
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad, DateTime dia, {int? pacienteId}) async {
    final mismoDia = solicitudes.any((s) =>
        s.pacienteId == pacienteId &&
        soloFecha(s.diaSolicitado) == soloFecha(dia) &&
        (s.estado == EstadoSolicitudApp.pendiente || s.estado == EstadoSolicitudApp.enEspera));
    if (mismoDia) {
      throw const ApiException('YA_TIENE_CITA_ESE_DIA', 'Ya tiene una cita o una solicitud para ese dia',
          estado: 409);
    }
    final nueva = ResultadoSolicitud(
      solicitudId: ++_siguiente,
      estado: EstadoSolicitudApp.pendiente,
      mensaje: 'Su solicitud fue registrada. El sistema le asignará médico, hora y consultorio y le avisará.',
      especialidad: especialidad,
      diaSolicitado: soloFecha(dia),
      prioridad: 1,
      ordenLlegada: _siguiente,
      pacienteId: pacienteId,
    );
    solicitudes.add(nueva);
    return nueva;
  }

  @override
  Future<ResultadoSolicitud> consultar(int solicitudId) async => solicitudes.firstWhere(
        (s) => s.solicitudId == solicitudId,
        orElse: () => throw const ApiException('NO_ENCONTRADA', 'No se encontro la solicitud', estado: 404),
      );

  @override
  Future<List<ResultadoSolicitud>> listar() async => solicitudes.reversed.toList();
}
