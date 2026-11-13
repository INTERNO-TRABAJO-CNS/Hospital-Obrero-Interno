import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/reportes/reporte.dart';
import '../services/reportes_service.dart';
import 'aviso_seguro.dart';

/// "Reportes e indicadores": la pestaña elegida y su reporte.
class ReportesController extends ChangeNotifier with AvisoSeguro {
  ReportesController(this._servicio);

  final ReportesService _servicio;

  /// Como en el Figma, abre en "Por médico".
  TipoReporte tipo = TipoReporte.porMedico;
  Reporte? reporte;
  String? error;

  Future<void> cargar() async {
    try {
      reporte = await _servicio.reporte(tipo);
      error = null;
    } on ApiException catch (e) {
      error = e.mensaje;
    }
    notificar();
  }

  void elegir(TipoReporte nuevo) {
    tipo = nuevo;
    cargar();
  }
}
