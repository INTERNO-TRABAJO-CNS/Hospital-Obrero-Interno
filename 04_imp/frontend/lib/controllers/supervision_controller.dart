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

/// Supervision del administrador (solo lectura). Se vuelve a pedir cada [intervalo].
class SupervisionController extends ChangeNotifier with AvisoSeguro {
  SupervisionController(this._servicio, {this.intervalo = const Duration(seconds: 15)});

  final SupervisionService _servicio;
  final Duration intervalo;
  Timer? _temporizador;

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
    super.dispose();
  }
}
