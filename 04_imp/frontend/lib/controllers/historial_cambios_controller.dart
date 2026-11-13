import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/historial/cambio_cita.dart';
import '../services/historial_cambios_service.dart';
import 'aviso_seguro.dart';

/// "Historial de cambios": filtros (cita, usuario, desde-hasta) y resultados.
class HistorialCambiosController extends ChangeNotifier with AvisoSeguro {
  HistorialCambiosController(this._servicio);

  final HistorialCambiosService _servicio;

  String cita = '';
  String usuario = '';
  DateTime? desde;
  DateTime? hasta;

  List<CambioCita> cambios = const [];
  bool cargando = false;
  String? error;

  void elegirRango(DateTime? nuevoDesde, DateTime? nuevoHasta) {
    desde = nuevoDesde;
    hasta = nuevoHasta;
    notificar();
  }

  Future<void> buscar() async {
    cargando = true;
    notificar();
    try {
      cambios = await _servicio.buscar(FiltroCambios(cita: cita, usuario: usuario, desde: desde, hasta: hasta));
      error = null;
    } on ApiException catch (e) {
      error = e.mensaje;
    } finally {
      cargando = false;
      notificar();
    }
  }
}
