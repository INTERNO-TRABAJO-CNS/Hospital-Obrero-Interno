import 'package:flutter/material.dart' show TimeOfDay;

/// Dias de la semana con el nombre que usa el backend (java.time.DayOfWeek).
enum DiaSemana {
  lunes('MONDAY', 'Lun'),
  martes('TUESDAY', 'Mar'),
  miercoles('WEDNESDAY', 'Mié'),
  jueves('THURSDAY', 'Jue'),
  viernes('FRIDAY', 'Vie'),
  sabado('SATURDAY', 'Sáb'),
  domingo('SUNDAY', 'Dom');

  const DiaSemana(this.codigo, this.corto);

  final String codigo;
  final String corto;

  static DiaSemana desdeCodigo(String codigo) =>
      values.firstWhere((d) => d.codigo == codigo, orElse: () => lunes);
}

/// Franja semanal de un medico en un consultorio (espejo de HorarioDTO del backend).
/// El backend guarda un horario por dia.
class Horario {
  const Horario({
    this.id = 0,
    required this.medicoId,
    required this.consultorioId,
    required this.dia,
    required this.inicio,
    required this.fin,
    this.activo = true,
  });

  factory Horario.desdeJson(Map<String, dynamic> json) => Horario(
        id: (json['horarioId'] as num?)?.toInt() ?? 0,
        medicoId: (json['medicoId'] as num?)?.toInt() ?? 0,
        consultorioId: (json['consultorioId'] as num?)?.toInt() ?? 0,
        dia: DiaSemana.desdeCodigo(json['diaSemana'] as String? ?? ''),
        inicio: horaDesdeTexto(json['horaInicio'] as String? ?? '00:00'),
        fin: horaDesdeTexto(json['horaFin'] as String? ?? '00:00'),
        activo: json['activo'] as bool? ?? false,
      );

  final int id;
  final int medicoId;
  final int consultorioId;
  final DiaSemana dia;
  final TimeOfDay inicio;
  final TimeOfDay fin;
  final bool activo;

  Map<String, dynamic> aJson() => {
        'horarioId': id,
        'medicoId': medicoId,
        'consultorioId': consultorioId,
        'diaSemana': dia.codigo,
        'horaInicio': horaATexto(inicio),
        'horaFin': horaATexto(fin),
        'activo': activo,
      };

  Horario copiar({int? id, DiaSemana? dia, bool? activo}) => Horario(
        id: id ?? this.id,
        medicoId: medicoId,
        consultorioId: consultorioId,
        dia: dia ?? this.dia,
        inicio: inicio,
        fin: fin,
        activo: activo ?? this.activo,
      );
}

/// "08:00" o "08:00:00" -> TimeOfDay.
TimeOfDay horaDesdeTexto(String texto) {
  final partes = texto.split(':');
  return TimeOfDay(hour: int.parse(partes[0]), minute: int.parse(partes[1]));
}

/// TimeOfDay -> "08:00".
String horaATexto(TimeOfDay hora) =>
    '${hora.hour.toString().padLeft(2, '0')}:${hora.minute.toString().padLeft(2, '0')}';

/// Minutos desde la medianoche (para comparar horas).
int minutosDe(TimeOfDay hora) => hora.hour * 60 + hora.minute;
