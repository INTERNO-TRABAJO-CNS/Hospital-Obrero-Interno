import 'package:flutter/material.dart' show TimeOfDay;

import '../catalogo/especialidad.dart';
import '../catalogo/horario.dart';

// Supervision en tiempo real del administrador (Figma "ADMIN SUPERVISION ..."). Solo lectura.

/// Fila de "Agenda por médico". Las filas de detalle muestran la ficha que se esta atendiendo.
class FilaAgendaMedico {
  const FilaAgendaMedico({
    required this.medico,
    required this.especialidadOFicha,
    required this.fichasOPaciente,
    required this.horarioOConsultorio,
    required this.estado,
    this.detalle = false,
  });

  final String medico;

  /// "Consultorio Familiar" (resumen) o "Ficha 3 · 09:30" (detalle).
  final String especialidadOFicha;

  /// "4 / 7" (fichas del dia / cupo) o el nombre del paciente.
  final String fichasOPaciente;

  /// "08:00 – 11:45" o el numero de consultorio.
  final String horarioOConsultorio;
  final String estado;
  final bool detalle;
}

/// Pestaña "Citas comunes".
class ResumenCitas {
  const ResumenCitas({
    required this.fichasHoy,
    required this.atendidas,
    required this.confirmadas,
    required this.canceladas,
    required this.filas,
  });

  final int fichasHoy;
  final int atendidas;
  final int confirmadas;
  final int canceladas;
  final List<FilaAgendaMedico> filas;
}

/// Cada especialidad interna tiene una tabla General y otra de Pediatria (no se mezclan).
enum TablaInterna {
  general('Tabla General', 'General'),
  pediatria('Tabla Pediatría', 'Pediatría');

  const TablaInterna(this.titulo, this.nombre);

  final String titulo;
  final String nombre;
}

enum EstadoFichaInterna {
  asignada('ASIGNADA'),
  enEspera('EN_ESPERA'),
  atendida('ATENDIDA'),
  cancelada('CANCELADA'),
  desplazada('DESPLAZADA');

  const EstadoFichaInterna(this.codigo);

  final String codigo;
}

/// Ficha interna vista por el administrador (sin la gravedad: solo la ve el medico que derivo).
class FichaInternaSupervision {
  const FichaInternaSupervision({
    this.hora,
    required this.paciente,
    this.especialista,
    this.urgencia = false,
    required this.derivadoPor,
    required this.estado,
    this.detalle,
  });

  /// `null` mientras no tiene horario (EN_ESPERA, DESPLAZADA).
  final String? hora;
  final String paciente;
  final String? especialista;
  final bool urgencia;
  final String derivadoPor;
  final EstadoFichaInterna estado;

  /// "no se presentó", "pasó al 18 nov 11:00".
  final String? detalle;
}

/// Pestaña "Fichas internas" para una especialidad y una tabla.
class ResumenFichasInternas {
  const ResumenFichasInternas({required this.fecha, required this.fichas});

  final DateTime fecha;
  final List<FichaInternaSupervision> fichas;

  int contar(EstadoFichaInterna estado) => fichas.where((f) => f.estado == estado).length;
}

/// Una cirugia en la linea de tiempo del dia.
class CirugiaDelDia {
  const CirugiaDelDia({required this.paciente, required this.inicio, required this.fin, this.urgente = false});

  final String paciente;
  final TimeOfDay inicio;
  final TimeOfDay fin;
  final bool urgente;

  int get minutos => minutosDe(fin) - minutosDe(inicio);
}

/// Un quirofano con sus cirugias del dia.
class QuirofanoDelDia {
  const QuirofanoDelDia({
    required this.codigo,
    required this.especialidad,
    required this.apertura,
    required this.cierre,
    required this.limpiezaMin,
    required this.cirugias,
  });

  final String codigo;
  final Especialidad especialidad;
  final TimeOfDay apertura;
  final TimeOfDay cierre;
  final int limpiezaMin;
  final List<CirugiaDelDia> cirugias;
}

/// Pestaña "Quirófanos".
class ResumenQuirofanos {
  const ResumenQuirofanos({required this.fecha, required this.quirofanos, required this.desplazamientos});

  final DateTime fecha;
  final List<QuirofanoDelDia> quirofanos;
  final int desplazamientos;

  int get cirugiasHoy => quirofanos.fold(0, (n, q) => n + q.cirugias.length);
  int get urgentes => quirofanos.fold(0, (n, q) => n + q.cirugias.where((c) => c.urgente).length);

  /// Solo el tiempo de cirugia (sin limpieza).
  double get horasUsadas => quirofanos.fold(0, (n, q) => n + q.cirugias.fold(0, (m, c) => m + c.minutos)) / 60;

  double get horasAbiertas => quirofanos.fold(0, (n, q) => n + (minutosDe(q.cierre) - minutosDe(q.apertura))) / 60;
}
