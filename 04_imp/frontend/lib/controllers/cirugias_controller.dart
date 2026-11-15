import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import '../models/comun/fechas_iso.dart';
import '../models/comun/texto.dart';
import '../models/medico/cirugia.dart';
import '../models/medico/ficha_interna.dart';
import '../models/medico/medico_actual.dart';
import '../services/cirugias_service.dart';
import '../services/fichas_internas_service.dart';
import 'aviso_seguro.dart';

/// CU-24: cirugías del día en los quirófanos de la especialidad.
class CirugiasController extends ChangeNotifier with AvisoSeguro {
  CirugiasController(this.medico, this._servicio, {DateTime Function()? reloj})
      : _reloj = reloj ?? DateTime.now,
        fecha = hoyIso((reloj ?? DateTime.now)());

  final MedicoActual medico;
  final CirugiasService _servicio;
  final DateTime Function() _reloj;

  String fecha;
  List<Cirugia> cirugias = const [];
  String? error;

  String get hoy => hoyIso(_reloj());
  bool get esHoy => fecha == hoy;
  String get ahora => horaActual(_reloj());

  Future<void> cargar() async {
    error = null;
    try {
      cirugias = await _servicio.delDia(medico, fecha);
    } on ApiException catch (e) {
      error = e.mensaje;
    }
    notificar();
  }

  Future<void> cambiarFecha(String nueva) {
    fecha = nueva;
    return cargar();
  }

  List<String> get quirofanos =>
      {..._servicio.quirofanosDe(medico.especialidad), for (final c in cirugias) c.quirofano}.toList()..sort();

  List<Cirugia> get mias => [
        for (final c in cirugias)
          if (mismoNombre(c.cirujano, medico.nombre)) c
      ];

  Future<void> marcar(Cirugia c, EstadoCirugia estado) async {
    try {
      await _servicio.marcar(c.numero, estado);
      await cargar();
    } on ApiException catch (e) {
      error = e.mensaje;
      notificar();
    }
  }
}

/// CU-24: "Programar cirugía" desde una ficha interna ATENDIDA.
class ProgramarCirugiaController extends ChangeNotifier with AvisoSeguro {
  ProgramarCirugiaController(this.medico, this._cirugias, this._fichas) {
    final esp = medico.especialidad;
    if (esp.operaEnQuirofano) {
      duracion = (((esp.cirugiaMin + esp.cirugiaMax) / 2) / 10).round() * 10;
    }
  }

  final MedicoActual medico;
  final CirugiasService _cirugias;
  final FichasInternasService _fichas;

  FichaInterna? ficha;
  bool cargada = false;
  int? duracion;
  Gravedad gravedad = Gravedad.alta;
  bool urgente = false;
  PropuestaCirugia? propuesta;
  (String, String)? rechazo;
  Cirugia? confirmada;
  bool trabajando = false;

  bool get choque => rechazo?.$1 == 'CHOQUE_DE_HORARIO';

  Future<void> cargar(int? ticket) async {
    ficha = ticket == null ? null : await _fichas.porTicket(ticket);
    cargada = true;
    notificar();
  }

  void cambiarDuracion(String texto) {
    duracion = int.tryParse(texto.trim());
    notificar();
  }

  void elegirGravedad(Gravedad g) {
    gravedad = g;
    notificar();
  }

  void cambiarUrgente(bool valor) {
    urgente = valor;
    notificar();
  }

  DatosCirugia _datos() =>
      DatosCirugia(fichaTicket: ficha!.ticket, duracionMin: duracion ?? 0, gravedad: gravedad, urgente: urgente);

  Future<void> proponer() async {
    if (ficha == null) {
      return;
    }
    trabajando = true;
    rechazo = null;
    propuesta = null;
    notificar();
    try {
      propuesta = await _cirugias.proponer(_datos(), medico);
    } on ApiException catch (e) {
      rechazo = (e.codigo, e.mensaje);
    } finally {
      trabajando = false;
      notificar();
    }
  }

  Future<void> confirmar() async {
    final p = propuesta;
    if (p == null) {
      return;
    }
    trabajando = true;
    notificar();
    try {
      confirmada = await _cirugias.confirmar(_datos(), p, medico);
    } on ApiException catch (e) {
      rechazo = (e.codigo, e.mensaje);
    } finally {
      trabajando = false;
      notificar();
    }
  }
}
