import '../models/fichas/tv_ficha_model.dart';
import '../services/fichas_service.dart';

class FichasController {
  final FichasService _fichasService;

  FichasController({required FichasService fichasService})
      : _fichasService = fichasService;

  Future<List<TvFichaModel>> obtenerListadoParaTv() {
    return _fichasService.obtenerListadoParaTv();
  }
}