import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/solicitud/solicitud.dart';

abstract class SolicitudService {
  /// El paciente solo envía la especialidad: el día, médico, hora y consultorio los asigna el sistema.
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad);

  Future<ResultadoSolicitud> consultar(int solicitudId);

  Future<List<ResultadoSolicitud>> listar();
}

class ApiSolicitudService implements SolicitudService {
  ApiSolicitudService(this._api);

  final ApiClient _api;

  @override
  Future<ResultadoSolicitud> solicitar(Especialidad especialidad) async {
    final respuesta = await _api.post(
      '/api/v1/solicitudes',
      {'especialidad': especialidad.codigo},
    );

    return leerRespuesta(
      () => ResultadoSolicitud.fromJson(respuesta),
    );
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