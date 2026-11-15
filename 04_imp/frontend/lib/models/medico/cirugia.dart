import '../catalogo/especialidad.dart';
import 'ficha_interna.dart';

/// CU-24: cirugías en quirófano. Los quirófanos no se comparten entre especialidades.
enum EstadoCirugia {
  programada('PROGRAMADA'),
  realizada('REALIZADA'),
  cancelada('CANCELADA');

  const EstadoCirugia(this.codigo);

  final String codigo;
}

/// Horario de los quirófanos (PV-19): 07:00 a 19:00; las urgencias pueden operarse fuera de horario.
const String quirofanoAbre = '07:00';
const String quirofanoCierra = '19:00';

/// Quirófano de una especialidad con su limpieza (30 a 45 min).
class QuirofanoEspecialidad {
  const QuirofanoEspecialidad(this.codigo, this.especialidad, this.limpiezaMin);

  final String codigo;
  final Especialidad especialidad;
  final int limpiezaMin;
}

class Cirugia {
  const Cirugia({
    required this.numero,
    required this.fichaTicket,
    required this.pacienteId,
    required this.nombrePaciente,
    required this.numeroAfiliado,
    required this.edad,
    required this.especialidad,
    required this.quirofano,
    required this.cirujano,
    required this.fecha,
    required this.inicio,
    required this.fin,
    required this.limpiezaHasta,
    required this.duracionMin,
    required this.urgente,
    required this.gravedad,
    required this.estado,
  });

  final int numero;
  final int fichaTicket;
  final int pacienteId;
  final String nombrePaciente;
  final String numeroAfiliado;
  final int edad;
  final Especialidad especialidad;
  final String quirofano;
  final String cirujano;
  final String fecha;
  final String inicio;
  final String fin;
  final String limpiezaHasta;
  final int duracionMin;
  final bool urgente;
  final Gravedad gravedad;
  final EstadoCirugia estado;

  Cirugia copia({EstadoCirugia? estado, String? fecha, String? inicio, String? fin, String? limpiezaHasta}) => Cirugia(
        numero: numero,
        fichaTicket: fichaTicket,
        pacienteId: pacienteId,
        nombrePaciente: nombrePaciente,
        numeroAfiliado: numeroAfiliado,
        edad: edad,
        especialidad: especialidad,
        quirofano: quirofano,
        cirujano: cirujano,
        fecha: fecha ?? this.fecha,
        inicio: inicio ?? this.inicio,
        fin: fin ?? this.fin,
        limpiezaHasta: limpiezaHasta ?? this.limpiezaHasta,
        duracionMin: duracionMin,
        urgente: urgente,
        gravedad: gravedad,
        estado: estado ?? this.estado,
      );
}

class DatosCirugia {
  const DatosCirugia(
      {required this.fichaTicket, required this.duracionMin, required this.gravedad, required this.urgente});

  final int fichaTicket;
  final int duracionMin;
  final Gravedad gravedad;
  final bool urgente;
}

/// Cirugía no urgente que una urgencia desplazaría, con su nuevo horario.
class Desplazamiento {
  const Desplazamiento({required this.cirugia, required this.nuevaFecha, required this.nuevoInicio});

  final Cirugia cirugia;
  final String nuevaFecha;
  final String nuevoInicio;
}

/// Lo que calcula "Proponer horario" (no guarda nada).
class PropuestaCirugia {
  const PropuestaCirugia({
    required this.quirofano,
    required this.especialidad,
    required this.fecha,
    required this.inicio,
    required this.fin,
    required this.limpiezaHasta,
    required this.limpiezaMin,
    this.desplaza,
  });

  final String quirofano;
  final Especialidad especialidad;
  final String fecha;
  final String inicio;
  final String fin;
  final String limpiezaHasta;
  final int limpiezaMin;
  final Desplazamiento? desplaza;
}

/// Rechazos posibles (Figma "CIRUGIA CHOQUE DE HORARIO").
const List<(String, String)> rechazosCirugia = [
  (
    'DURACION_FUERA_DE_RANGO',
    'La duración no está en el rango de cirugía de tu especialidad '
        '(Gastroenterología 30–120 min, Hematología 30–120, Dermatología 15–120).'
  ),
  ('ESPECIALIDAD_NO_OPERA', 'Infectología, Medicina del Trabajo y Terapia Intensiva no operan en quirófano.'),
  ('FICHA_NO_ATENDIDA', 'Solo el especialista que atendió la ficha puede programar la cirugía.'),
];
