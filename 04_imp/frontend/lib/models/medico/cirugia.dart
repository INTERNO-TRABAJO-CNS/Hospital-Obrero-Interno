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

  factory Cirugia.fromJson(Map<String, dynamic> json) => Cirugia(
        numero: (json['numero'] as num).toInt(),
        fichaTicket: (json['fichaTicket'] as num?)?.toInt() ?? 0,
        pacienteId: (json['pacienteId'] as num).toInt(),
        nombrePaciente: json['nombrePaciente'] as String? ?? '',
        numeroAfiliado: json['numeroAfiliado'] as String? ?? '',
        edad: (json['edad'] as num?)?.toInt() ?? 0,
        especialidad: Especialidad.desdeCodigo(json['especialidad'] as String? ?? ''),
        quirofano: json['quirofano'] as String? ?? '',
        cirujano: json['cirujano'] as String? ?? '',
        fecha: json['fecha'] as String? ?? '',
        inicio: json['inicio'] as String? ?? '',
        fin: json['fin'] as String? ?? '',
        limpiezaHasta: json['limpiezaHasta'] as String? ?? '',
        duracionMin: (json['duracionMin'] as num?)?.toInt() ?? 0,
        urgente: json['urgente'] as bool? ?? false,
        gravedad: Gravedad.desdeCodigo(json['gravedad'] as String?) ?? Gravedad.media,
        estado: EstadoCirugia.values.firstWhere((e) => e.codigo == json['estado'],
            orElse: () => throw FormatException('estado de cirugía desconocido "${json['estado']}"')),
      );

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

  factory Desplazamiento.fromJson(Map<String, dynamic> json) => Desplazamiento(
        cirugia: Cirugia.fromJson(Map<String, dynamic>.from(json['cirugia'] as Map)),
        nuevaFecha: json['nuevaFecha'] as String? ?? '',
        nuevoInicio: json['nuevoInicio'] as String? ?? '',
      );

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
    this.quirofanoId,
  });

  factory PropuestaCirugia.fromJson(Map<String, dynamic> json) => PropuestaCirugia(
        quirofanoId: (json['quirofanoId'] as num?)?.toInt(),
        quirofano: json['quirofano'] as String? ?? '',
        especialidad: Especialidad.desdeCodigo(json['especialidad'] as String? ?? ''),
        fecha: json['fecha'] as String? ?? '',
        inicio: json['inicio'] as String? ?? '',
        fin: json['fin'] as String? ?? '',
        limpiezaHasta: json['limpiezaHasta'] as String? ?? '',
        limpiezaMin: (json['limpiezaMin'] as num?)?.toInt() ?? 0,
        desplaza: json['desplaza'] == null
            ? null
            : Desplazamiento.fromJson(Map<String, dynamic>.from(json['desplaza'] as Map)),
      );

  /// Id del quirofano en el backend (null en los datos de prueba).
  final int? quirofanoId;
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
