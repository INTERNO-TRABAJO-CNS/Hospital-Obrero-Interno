import '../core/api_client.dart';

/// CU-11: cancelacion de una cita por el paciente autenticado.
abstract class CancelacionService {
  /// `POST /api/v1/citas/{citaId}/cancelar`.
  Future<void> cancelar(int citaId);
}

class ApiCancelacionService implements CancelacionService {
  ApiCancelacionService(this._api);

  final ApiClient _api;

  @override
  Future<void> cancelar(int citaId) async {
    await _api.post('/api/v1/citas/$citaId/cancelar');
  }
}
