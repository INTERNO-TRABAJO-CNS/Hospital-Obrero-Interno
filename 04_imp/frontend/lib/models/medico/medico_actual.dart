import '../catalogo/especialidad.dart';

/// `GET /api/v1/medico/yo`: el médico de la sesión.
class MedicoActual {
  const MedicoActual({
    required this.medicoId,
    required this.nombre,
    required this.especialidad,
    required this.puedeDerivar,
    required this.operaEnQuirofano,
  });

  final int medicoId;
  final String nombre;
  final Especialidad especialidad;

  /// Médico General (Consultorio Familiar) y Pediatría generan fichas internas.
  final bool puedeDerivar;
  final bool operaEnQuirofano;

  /// El pediatra deriva a la tabla pediátrica; el médico general, a la general.
  TablaFicha get tabla => especialidad == Especialidad.pediatria ? TablaFicha.pediatria : TablaFicha.general;

  /// "Médico General", "Pediatría", "Odontología" o "Especialista".
  String get tipo {
    if (especialidad == Especialidad.consultorioFamiliar) {
      return 'Médico General';
    }
    return especialidad.esBasica ? especialidad.nombre : 'Especialista';
  }

  /// Píldora de la barra superior: "Médico General · Carlos Ramos" o
  /// "Especialista · Raúl Vargas · Gastroenterología".
  String get etiqueta => especialidad.esBasica ? '$tipo · $nombre' : '$tipo · $nombre · ${especialidad.nombre}';

  factory MedicoActual.fromJson(Map<String, dynamic> json) => MedicoActual(
        medicoId: (json['medicoId'] as num).toInt(),
        nombre: json['nombre'] as String? ?? '',
        especialidad: Especialidad.desdeCodigo(json['especialidad'] as String? ?? ''),
        puedeDerivar: json['puedeDerivar'] as bool? ?? false,
        operaEnQuirofano: json['operaEnQuirofano'] as bool? ?? false,
      );
}

/// Las 20 tablas de fichas internas: cada especialidad tiene una GENERAL y una PEDIATRIA.
enum TablaFicha {
  general('GENERAL', 'GENERAL'),
  pediatria('PEDIATRIA', 'PEDIATRÍA');

  const TablaFicha(this.codigo, this.texto);

  final String codigo;
  final String texto;
}
