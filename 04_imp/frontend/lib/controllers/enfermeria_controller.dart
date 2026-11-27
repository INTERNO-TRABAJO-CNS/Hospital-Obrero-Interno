import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/comun/texto.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../services/enfermeria_service.dart';
import 'aviso_seguro.dart';

/// Columna por la que busca "Fichas del día" (chips del Figma).
enum CampoBusqueda {
  paciente('Paciente', 'Buscar por paciente…'),
  medico('Médico', 'Buscar por médico…'),
  estado('Estado', 'Buscar por estado…'),
  consultorio('Consultorio', 'Buscar por consultorio…');

  const CampoBusqueda(this.titulo, this.pista);

  final String titulo;
  final String pista;
}

/// Pantallas de enfermeria: "Fichas del día" (solo lectura, con busqueda) y
/// "Confirmar asistencia" (hasta 30 minutos antes de cada cita).
class EnfermeriaController extends ChangeNotifier with AvisoSeguro {
  EnfermeriaController(this._servicio, {required this.usuario, DateTime Function()? reloj})
      : _reloj = reloj ?? DateTime.now;

  final EnfermeriaService _servicio;

  /// Quien confirma, para el historial ("Enfermería · M. Flores").
  final String usuario;
  final DateTime Function() _reloj;

  List<FichaDelDia> fichas = const [];
  bool cargando = false;
  String? error;
  CampoBusqueda campo = CampoBusqueda.medico;
  String texto = '';

  DateTime get ahora => _reloj();

  Future<void> cargar() async {
    cargando = true;
    notificar();
    try {
      fichas = await _servicio.fichasDeHoy();
      error = null;
    } on ApiException catch (e) {
      error = e.mensaje;
    } finally {
      cargando = false;
      notificar();
    }
  }

  void elegirCampo(CampoBusqueda nuevo) {
    campo = nuevo;
    notificar();
  }

  void buscar(String nuevo) {
    texto = nuevo;
    notificar();
  }

  /// Fichas que coinciden con la busqueda.
  List<FichaDelDia> get filtradas {
    final buscado = textoPlano(texto);
    if (buscado.isEmpty) {
      return fichas;
    }
    String valor(FichaDelDia f) => switch (campo) {
          CampoBusqueda.paciente => f.paciente,
          CampoBusqueda.medico => f.medico,
          CampoBusqueda.estado => f.estado.codigo,
          CampoBusqueda.consultorio => f.consultorio,
        };
    return [
      for (final f in fichas)
        if (textoPlano(valor(f)).contains(buscado)) f
    ];
  }

  /// Citas PROGRAMADA cuyo plazo para confirmar sigue abierto, la mas urgente primero.
  List<FichaDelDia> get porConfirmar {
    final momento = ahora;
    return [
      for (final f in fichas)
        if (f.estado == EstadoCita.programada && !momento.isAfter(f.limiteConfirmacion)) f
    ]..sort((a, b) => a.limiteConfirmacion.compareTo(b.limiteConfirmacion));
  }

  /// Minutos que faltan para que venza el plazo (redondeado hacia arriba).
  int minutosRestantes(FichaDelDia f) => (f.limiteConfirmacion.difference(ahora).inSeconds / 60).ceil();

  /// `null` si se confirmo; si no, el mensaje (p. ej. el plazo ya vencio).
  Future<String?> confirmar(FichaDelDia f) async {
    try {
      await _servicio.confirmarLlegada(f.citaId, usuario: usuario);
      return null;
    } on ApiException catch (e) {
      return e.mensaje;
    } finally {
      await cargar();
    }
  }
}

/// "Enfermería · M. Flores" a partir del nombre de la sesion.
String usuarioParaHistorial(String rol, String nombre) => '$rol · ${nombreCorto(nombre)}';
