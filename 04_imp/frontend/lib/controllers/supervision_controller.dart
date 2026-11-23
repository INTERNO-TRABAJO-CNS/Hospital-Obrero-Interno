import 'dart:async';

import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/supervision/supervision.dart';
import '../services/supervision_service.dart';
import 'aviso_seguro.dart';

/// Pestañas de "Supervisión en tiempo real".
enum PestanaSupervision {
  citas('Citas comunes'),
  internas('Fichas internas'),
  quirofanos('Quirófanos');

  const PestanaSupervision(this.titulo);

  final String titulo;
}

/// Supervision del administrador (solo lectura). Las citas comunes se vuelven a pedir apenas el
/// WebSocket avisa un cambio (RNF-08); ademas se refresca cada [intervalo] por si se corta la conexion.
class SupervisionController extends ChangeNotifier with AvisoSeguro {
  SupervisionController(this._servicio, {this.intervalo = const Duration(seconds: 15)});

  final SupervisionService _servicio;
  final Duration intervalo;
  Timer? _temporizador;
  StreamSubscription<void>? _cambios;

  /// La pestaña de citas comunes muestra datos de prueba (sin backend).
  bool get citasDePrueba => _servicio.citasDePrueba;

  /// Datos de prueba en la pestaña actual (fichas internas y quirofanos todavia no tienen backend).
  bool get datosDePrueba => pestana != PestanaSupervision.citas || citasDePrueba;

  PestanaSupervision pestana = PestanaSupervision.citas;

  /// `null` = todas las especialidades basicas.
  Especialidad? basica;
  Especialidad interna = Especialidad.gastroenterologia;
  TablaInterna tabla = TablaInterna.general;

  ResumenCitas? citas;
  ResumenFichasInternas? internas;
  ResumenQuirofanos? quirofanos;
  String? error;

  /// Carga la pestaña actual y la refresca cada [intervalo].
  void iniciar() {
    cargar();
    _temporizador?.cancel();
    _temporizador = Timer.periodic(intervalo, (_) => cargar());
    _escucharCambios();
  }

  /// Cada cambio de cita recarga las citas comunes. Si la conexion se corta, queda el refresco periodico.
  void _escucharCambios() {
    _cambios?.cancel();
    _cambios = _servicio.cambios().listen((_) {
      if (pestana == PestanaSupervision.citas) {
        cargar();
      }
    }, onError: (Object _) {});
  }

  Future<void> cargar() async {
    try {
      switch (pestana) {
        case PestanaSupervision.citas:
          citas = await _servicio.citasComunes(especialidad: basica);
        case PestanaSupervision.internas:
          internas = await _servicio.fichasInternas(interna, tabla);
        case PestanaSupervision.quirofanos:
          quirofanos = await _servicio.quirofanos();
      }
      error = null;
    } on ApiException catch (e) {
      error = e.mensaje;
    }
    notificar();
  }

  void elegirPestana(PestanaSupervision nueva) {
    pestana = nueva;
    cargar();
  }

  void elegirBasica(Especialidad? nueva) {
    basica = nueva;
    cargar();
  }

  void elegirInterna(Especialidad nueva) {
    interna = nueva;
    cargar();
  }

  void elegirTabla(TablaInterna nueva) {
    tabla = nueva;
    cargar();
  }

  @override
  void dispose() {
    _temporizador?.cancel();
    _cambios?.cancel();
    super.dispose();
  }
}
