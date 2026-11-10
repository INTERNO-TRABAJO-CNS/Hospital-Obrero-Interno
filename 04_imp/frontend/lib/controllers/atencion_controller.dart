import 'package:flutter/foundation.dart';

import '../models/atencion/datos_atencion_dto.dart';
import '../models/atencion/resultado_atencion_dto.dart';
import '../services/api_client.dart';
import '../services/atencion_service.dart';

class AtencionController extends ChangeNotifier {
  AtencionController(this._service, this.citaId, this.medicoId);

  final AtencionService _service;
  final int citaId;
  final int medicoId;
  DatosAtencionDto? datos;
  ResultadoAtencionDto? resultado;
  bool isLoading = false;
  String? errorMessage;

  Future<void> cargar() async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();
    try {
      datos = await _service.datosParaAtencion(citaId, medicoId);
    } on ApiException catch (exception) {
      errorMessage = exception.error.mensaje;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<void> registrar(String medicacion) async {
    errorMessage = null;
    resultado = null;
    if (medicacion.trim().isEmpty || medicacion.length > 2000) {
      errorMessage = 'La medicacion es obligatoria y no puede superar 2000 caracteres.';
      notifyListeners();
      return;
    }

    isLoading = true;
    notifyListeners();
    try {
      resultado = await _service.registrar(citaId, medicoId, medicacion.trim());
    } on ApiException catch (exception) {
      errorMessage = exception.error.mensaje;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}