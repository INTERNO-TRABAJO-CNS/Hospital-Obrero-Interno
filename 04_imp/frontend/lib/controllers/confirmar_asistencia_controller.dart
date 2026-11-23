import 'package:flutter/foundation.dart';
import '../services/estado_cita_service.dart';

class ConfirmarAsistenciaController extends ChangeNotifier {
  final EstadoCitaService _service;
  bool isLoading = false;
  String mensajeResultado = '';

  ConfirmarAsistenciaController(this._service);

  Future<void> confirmar(int citaId) async {
    isLoading = true;
    notifyListeners();

    try {
      // Estructura de usuario autenticado requerida por el backend para el rol de ENFERMERIA
      final usuarioAutenticado = {
        'id': 1,
        'roles': ['ENFERMERIA']
      };

      final resultado = await _service.confirmarAsistencia(citaId, usuarioAutenticado);
      mensajeResultado = 'Resultado: $resultado';
    } catch (e) {
      mensajeResultado = 'Error al confirmar: $e';
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}