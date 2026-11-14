import '../models/fichas/cita_model.dart';
import '../services/fichas_service.dart';

class FichasController {
  final FichasService _fichasService;

  FichasController({FichasService? fichasService})
      : _fichasService = fichasService ?? FichasService();

  Future<List<CitaModel>> obtenerFichasDelDia({String? criterio, String? valor}) async {
    try {
      return await _fichasService.obtenerFichasDelDia(criterio: criterio, valor: valor);
    } catch (e) {
      // Manejo de errores o propagación para la vista de enfermería
      rethrow;
    }
  }

  Future<List<CitaModel>> obtenerListadoParaTv() async {
    try {
      return await _fichasService.obtenerListadoParaTv();
    } catch (e) {
      rethrow;
    }
  }
}