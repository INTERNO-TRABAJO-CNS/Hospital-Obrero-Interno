import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/quirofano/quirofano.dart';
import '../services/quirofano_service.dart';
import 'aviso_seguro.dart';

/// Catalogo de quirofanos. Las acciones devuelven `null` si salieron bien o el error (con su
/// codigo, que el Figma muestra en la caja roja).
class QuirofanosController extends ChangeNotifier with AvisoSeguro {
  QuirofanosController(this._servicio);

  final QuirofanoService _servicio;

  List<Quirofano> quirofanos = const [];
  bool cargando = false;
  ApiException? errorCarga;

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      quirofanos = await _servicio.listar();
      errorCarga = null;
    } on ApiException catch (e) {
      errorCarga = e;
    } finally {
      cargando = false;
      notificar();
    }
  }

  Future<ApiException?> guardar(Quirofano quirofano) => _ejecutar(() => _servicio.guardar(quirofano));

  Future<ApiException?> desactivar(int id) => _ejecutar(() => _servicio.desactivar(id));

  Future<ApiException?> _ejecutar(Future<void> Function() accion) async {
    try {
      await accion();
      quirofanos = await _servicio.listar();
      return null;
    } on ApiException catch (e) {
      return e;
    } finally {
      notificar();
    }
  }
}
