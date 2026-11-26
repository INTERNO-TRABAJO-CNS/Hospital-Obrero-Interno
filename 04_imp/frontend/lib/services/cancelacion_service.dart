import '../core/api_client.dart';

/// `?pacienteId=N` cuando el titular actua sobre un beneficiario (RN-05); vacio si es su propia cita.
String conPaciente(int? pacienteId) => pacienteId == null ? '' : '?pacienteId=$pacienteId';

/// CU-11: cancelacion de una cita por el paciente autenticado.
abstract class CancelacionService {
  /// `POST /api/v1/citas/{citaId}/cancelar`. [pacienteId]: el beneficiario dueno de la cita (RN-05).
  Future<void> cancelar(int citaId, {int? pacienteId});
}

class ApiCancelacionService implements CancelacionService {
  ApiCancelacionService(this._api);

  final ApiClient _api;

  @override
  Future<void> cancelar(int citaId, {int? pacienteId}) async {
    await _api.post('/api/v1/citas/$citaId/cancelar${conPaciente(pacienteId)}');
  }
}
