import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/solicitud/solicitud.dart';
import '../services/solicitud_service.dart';

class SolicitudController extends ChangeNotifier {
  SolicitudController({required SolicitudService service})
      : _service = service;

  final SolicitudService _service;

  bool cargando = false;
  String? error;
  ResultadoSolicitud? resultado;

  List<ResultadoSolicitud> solicitudes = [];

  Future<ResultadoSolicitud?> solicitar(Especialidad especialidad) async {
    cargando = true;
    error = null;
    resultado = null;
    notifyListeners();

    try {
      resultado = await _service.solicitar(especialidad);
      return resultado;
    } on ApiException catch (e) {
      error = e.mensaje;
      return null;
    } catch (_) {
      error = 'No se pudo enviar la solicitud. Intente nuevamente.';
      return null;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }

  Future<ResultadoSolicitud?> consultar(int solicitudId) async {
    cargando = true;
    error = null;
    notifyListeners();

    try {
      resultado = await _service.consultar(solicitudId);
      return resultado;
    } on ApiException catch (e) {
      error = e.mensaje;
      return null;
    } catch (_) {
      error = 'No se pudo consultar la solicitud.';
      return null;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }

  Future<bool> listar() async {
    cargando = true;
    error = null;
    notifyListeners();

    try {
      solicitudes = await _service.listar();
      return true;
    } on ApiException catch (e) {
      error = e.mensaje;
      return false;
    } catch (_) {
      error = 'No se pudieron cargar tus citas. Intente nuevamente.';
      return false;
    } finally {
      cargando = false;
      notifyListeners();
    }
  }
}