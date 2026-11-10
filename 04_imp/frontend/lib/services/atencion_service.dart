import '../models/error_dto.dart';
import '../models/atencion/datos_atencion_dto.dart';
import '../models/atencion/resultado_atencion_dto.dart';
import 'api_client.dart';

abstract interface class AtencionService {
  Future<DatosAtencionDto> datosParaAtencion(int citaId, int medicoId);

  Future<ResultadoAtencionDto> registrar(
    int citaId,
    int medicoId,
    String medicacion,
  );
}

class RestAtencionService implements AtencionService {
  RestAtencionService(this._client);

  final ApiClient _client;

  @override
  Future<DatosAtencionDto> datosParaAtencion(int citaId, int medicoId) async {
    final json = await _client.get('/citas/$citaId/atencion?medicoId=$medicoId');
    return DatosAtencionDto.fromJson(_asMap(json));
  }

  @override
  Future<ResultadoAtencionDto> registrar(
    int citaId,
    int medicoId,
    String medicacion,
  ) async {
    final json = await _client.post(
      '/citas/$citaId/atencion?medicoId=$medicoId',
      {'medicacion': medicacion},
    );
    return ResultadoAtencionDto.fromJson(json);
  }

  Map<String, dynamic> _asMap(dynamic json) {
    if (json is Map<String, dynamic>) return json;
    throw const ApiException(
      ErrorDto(codigo: 'RESPUESTA_INVALIDA', mensaje: 'Respuesta de atencion invalida.'),
    );
  }
}

class FakeAtencionService implements AtencionService {
  FakeAtencionService({DatosAtencionDto? datos})
      : datos = datos ??
            const DatosAtencionDto(
              citaId: 10,
              medicoId: 20,
              afiliado: '1234567',
              ficha: 'FIC-01',
              fecha: '2026-11-10',
              hora: '09:30',
              consulta: 'Consulta general',
              estado: 'ASISTENCIA_CONFIRMADA',
            );

  final DatosAtencionDto datos;
  String? medicacionRegistrada;

  @override
  Future<DatosAtencionDto> datosParaAtencion(int citaId, int medicoId) async {
    return datos;
  }

  @override
  Future<ResultadoAtencionDto> registrar(
    int citaId,
    int medicoId,
    String medicacion,
  ) async {
    medicacionRegistrada = medicacion;
    return const ResultadoAtencionDto(resultado: 'REGISTRADA');
  }
}