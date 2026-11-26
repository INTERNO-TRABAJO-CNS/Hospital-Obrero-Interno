import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/comun/fechas_iso.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/medico/agenda_medico.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/medico_actual.dart';
import '../services/fichas_internas_service.dart';
import '../services/medico_service.dart';
import 'aviso_seguro.dart';

/// Resultado de cargar `/medico/yo`: si el backend no responde NO se cierra la sesión (se puede
/// reintentar); solo un 401/403 la invalida.
enum CargaMedico { ok, sinConexion, rechazada }

/// El médico de la sesión (menú, píldora y tipo de médico). Vive mientras dure la sesión.
class MedicoController extends ChangeNotifier with AvisoSeguro {
  MedicoController(this._servicio);

  final MedicoService _servicio;

  MedicoActual? medico;
  String? error;

  Future<CargaMedico> cargar() async {
    try {
      medico = await _servicio.yo();
      error = null;
      return CargaMedico.ok;
    } on ApiException catch (e) {
      error = e.mensaje;
      final rechazada = e.estado == 401 || e.estado == 403 || e.codigo == 'RESPUESTA_INVALIDA';
      return rechazada ? CargaMedico.rechazada : CargaMedico.sinConexion;
    } finally {
      notificar();
    }
  }

  void olvidar() {
    medico = null;
    error = null;
    notificar();
  }
}

/// CU-13: agenda del día (hoy por defecto) y selector de otras fechas. La fecha elegida se
/// recuerda mientras dure la sesión: al volver de una atención o del historial se ve el mismo día.
class AgendaMedicoController extends ChangeNotifier with AvisoSeguro {
  AgendaMedicoController(this._servicio, {FichasInternasService? fichas, DateTime Function()? reloj})
      : _fichas = fichas,
        _reloj = reloj ?? DateTime.now;

  final AgendaMedicoService _servicio;
  final FichasInternasService? _fichas;
  final DateTime Function() _reloj;

  AgendaDia? agenda;

  /// null = hoy.
  String? fechaElegida;

  /// Ficha interna generada desde cada cita (una sola por cita).
  Map<int, FichaInterna> derivaciones = const {};
  bool cargando = false;
  String? error;
  int _pedido = 0;

  String get hoy => hoyIso(_reloj());
  bool get esHoy => agenda?.fecha == hoy;

  /// "6 citas hoy" o "1 cita este día".
  String get titulo {
    final n = agenda?.cantidad ?? 0;
    final citas = n == 1 ? '1 cita' : '$n citas';
    return esHoy ? '$citas hoy' : '$citas este día';
  }

  /// "Jueves 12 de noviembre · 6 citas hoy" (Figma).
  String get resumen => agenda == null ? 'Cargando…' : '${fechaTituloIso(agenda!.fecha)} · $titulo';

  /// Carga la fecha pedida, o la última elegida, o hoy. Ignora respuestas viejas si se pidió otra fecha.
  Future<void> cargar([String? fecha]) async {
    if (fecha != null) {
      fechaElegida = fecha == hoy ? null : fecha;
    }
    final pedida = fecha ?? fechaElegida;
    final numero = ++_pedido;
    cargando = true;
    error = null;
    notificar();
    try {
      final respuesta = await _servicio.agenda(pedida);
      if (numero != _pedido) {
        return;
      }
      agenda = respuesta;
      derivaciones = await _buscarDerivaciones(respuesta.fichas);
    } on ApiException catch (e) {
      if (numero == _pedido) {
        error = e.mensaje;
      }
    } finally {
      if (numero == _pedido) {
        cargando = false;
        notificar();
      }
    }
  }

  Future<void> volverAHoy() => cargar(hoy);

  Future<Map<int, FichaInterna>> _buscarDerivaciones(List<FichaAgenda> lista) async {
    final fichas = _fichas;
    if (fichas == null) {
      return const {};
    }
    final resultado = <int, FichaInterna>{};
    for (final f in lista) {
      if (f.estado == EstadoCita.atendida || f.estado == EstadoCita.asistenciaConfirmada) {
        final ficha = await fichas.deCita(f.citaId);
        if (ficha != null) {
          resultado[f.citaId] = ficha;
        }
      }
    }
    return resultado;
  }

  /// "Atender": solo cuando enfermería ya confirmó la asistencia.
  bool puedeAtender(FichaAgenda f) => f.estado == EstadoCita.asistenciaConfirmada;

  /// "Ticket interno": médico que deriva, cita de hoy confirmada o atendida y todavía sin derivación.
  bool puedeTicketInterno(FichaAgenda f, MedicoActual? medico) =>
      (medico?.puedeDerivar ?? false) &&
      esHoy &&
      !derivaciones.containsKey(f.citaId) &&
      (f.estado == EstadoCita.asistenciaConfirmada || f.estado == EstadoCita.atendida);

  /// Al cerrar sesión.
  void olvidar() {
    agenda = null;
    fechaElegida = null;
    derivaciones = const {};
    error = null;
    _pedido++;
    cargando = false;
    notificar();
  }
}
