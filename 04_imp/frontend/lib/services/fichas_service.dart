import '../core/api_client.dart';
import '../models/fichas/tv_ficha_model.dart';

class FichasService {
  final ApiClient _api;

  FichasService(this._api);

  Future<List<TvFichaModel>> obtenerListadoParaTv() async {
    final respuesta = await _api.get('/api/v1/tv/fichas');

    final datos = respuesta['datos'] as List? ?? const [];

    return leerRespuesta(() => [
          for (final elemento in datos)
            TvFichaModel.fromJson(
              Map<String, dynamic>.from(elemento as Map),
            ),
        ]);
  }
}