import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/notificacion/notificacion_dto.dart';
import '../core/api_client.dart';
import '../services/notificaciones_service.dart';

class NotificacionesController extends ChangeNotifier {
  NotificacionesController(this._service, this.pacienteId);

  final NotificacionesService _service;
  final int pacienteId;
  final List<NotificacionDto> notificaciones = [];
  StreamSubscription<NotificacionDto>? _subscription;
  NotificacionDto? ultima;
  bool isLoading = false;
  String? errorMessage;

  Future<void> cargar() async {
    isLoading = true;
    errorMessage = null;
    notifyListeners();
    try {
      notificaciones
        ..clear()
        ..addAll(await _service.delPaciente(pacienteId));
    } on ApiException catch (exception) {
      errorMessage = exception.mensaje;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  void conectar() {
    _subscription = _service.escuchar(pacienteId).listen(
      (notificacion) {
        notificaciones.insert(0, notificacion);
        ultima = notificacion;
        notifyListeners();
      },
      onError: (Object error) {
        if (error is ApiException) errorMessage = error.mensaje;
        notifyListeners();
      },
    );
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}