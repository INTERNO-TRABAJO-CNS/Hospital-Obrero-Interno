import 'package:flutter/material.dart' show TimeOfDay;

import '../catalogo/especialidad.dart';
import '../catalogo/horario.dart';

/// Limpieza del quirofano despues de cada cirugia (Figma "ADMIN CATALOGO QUIROFANOS").
const int limpiezaMinima = 30;
const int limpiezaMaxima = 45;

/// Quirofano del catalogo. Cada uno es de una sola especialidad (no se comparten).
class Quirofano {
  const Quirofano({
    this.id = 0,
    required this.nombre,
    required this.especialidad,
    this.apertura = const TimeOfDay(hour: 7, minute: 0),
    this.cierre = const TimeOfDay(hour: 19, minute: 0),
    required this.limpiezaMin,
    this.cirugiasProgramadas = 0,
    this.activo = true,
  });

  final int id;

  /// "G-1".
  final String nombre;
  final Especialidad especialidad;
  final TimeOfDay apertura;
  final TimeOfDay cierre;
  final int limpiezaMin;

  /// Cirugias programadas a futuro (impiden cambiar la especialidad o desactivarlo).
  final int cirugiasProgramadas;
  final bool activo;

  /// "07:00 – 19:00".
  String get horario => '${horaATexto(apertura)} – ${horaATexto(cierre)}';

  /// Horas abiertas por dia.
  double get horasAbiertas => (minutosDe(cierre) - minutosDe(apertura)) / 60;

  Quirofano copiar({
    int? id,
    String? nombre,
    Especialidad? especialidad,
    TimeOfDay? apertura,
    TimeOfDay? cierre,
    int? limpiezaMin,
    bool? activo,
  }) =>
      Quirofano(
        id: id ?? this.id,
        nombre: nombre ?? this.nombre,
        especialidad: especialidad ?? this.especialidad,
        apertura: apertura ?? this.apertura,
        cierre: cierre ?? this.cierre,
        limpiezaMin: limpiezaMin ?? this.limpiezaMin,
        cirugiasProgramadas: cirugiasProgramadas,
        activo: activo ?? this.activo,
      );
}
