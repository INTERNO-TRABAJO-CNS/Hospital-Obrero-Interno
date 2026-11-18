import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/medico/atencion.dart';
import '../services/medico_service.dart';
import 'aviso_seguro.dart';

/// CU-15: datos autocompletados, receta y "Finalizar atención".
class AtencionController extends ChangeNotifier with AvisoSeguro {
  AtencionController(this._servicio);

  final AtencionService _servicio;

  DatosAtencion? datos;
  String medicacion = '';
  bool cargando = false;
  bool enviando = false;
  bool finalizada = false;
  String? error;

  int get caracteres => medicacion.trim().length;

  /// Solo evita enviar algo que el backend rechazaría; la regla la aplica el backend (RECETA_INVALIDA).
  bool get recetaLista => caracteres > 0 && caracteres <= maxReceta;
  bool get puedeFinalizar => datos != null && recetaLista && !enviando && !finalizada;

  Future<void> cargar(int citaId) async {
    cargando = true;
    error = null;
    datos = null;
    medicacion = '';
    finalizada = false;
    notificar();
    try {
      datos = await _servicio.datos(citaId);
    } on ApiException catch (e) {
      error = e.mensaje;
    } finally {
      cargando = false;
      notificar();
    }
  }

  void escribir(String texto) {
    medicacion = texto;
    notificar();
  }

  /// Guarda la receta en el historial y pasa la cita a ATENDIDA. No envía si la receta está vacía.
  Future<bool> finalizar() async {
    final d = datos;
    if (d == null || !recetaLista || enviando) {
      return false;
    }
    enviando = true;
    error = null;
    notificar();
    try {
      await _servicio.registrar(d.citaId, medicacion.trim());
      finalizada = true;
      datos = d.conEstado(EstadoCita.atendida);
      return true;
    } on ApiException catch (e) {
      error = e.mensaje;
      return false;
    } finally {
      enviando = false;
      notificar();
    }
  }
}
