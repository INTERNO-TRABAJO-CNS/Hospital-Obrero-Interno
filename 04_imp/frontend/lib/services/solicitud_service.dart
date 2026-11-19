import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/solicitud/solicitud.dart';

abstract class SolicitudService {
  Future<ResultadoSolicitud> solicitar(
    Especialidad especialidad,
    DateTime dia,
  );

  Future<ResultadoSolicitud> consultar(int solicitudId);

  Future<List<ResultadoSolicitud>> listar();
}

class ApiSolicitudService implements SolicitudService {
  ApiSolicitudService(this._api);

  final ApiClient _api;

  @override
  Future<ResultadoSolicitud> solicitar(
    Especialidad especialidad,
    DateTime dia,
  ) async {
    final fecha =
        '${dia.year.toString().padLeft(4, '0')}-'
        '${dia.month.toString().padLeft(2, '0')}-'
        '${dia.day.toString().padLeft(2, '0')}';

    final respuesta = await _api.post(
      '/api/v1/solicitudes',
      {
        'especialidad': especialidad.codigo,
        'diaSolicitado': fecha,
      },
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