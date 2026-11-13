/// BASICA: la pide el paciente. INTERNA: solo por derivacion (ficha interna).
enum CategoriaEspecialidad {
  basica('BÁSICA'),
  interna('INTERNA');

  const CategoriaEspecialidad(this.etiqueta);

  final String etiqueta;
}

/// Especialidades del hospital con el rango de duracion de la ficha y, si operan, de la cirugia
/// (minutos). Debe coincidir con `TipoEspecialidad` del backend (decision del 10/11/2026:
/// 3 basicas y 10 internas; operan Gastroenterologia, Hematologia y Dermatologia).
enum Especialidad {
  consultorioFamiliar('CONSULTORIO_FAMILIAR', 'Consultorio Familiar', CategoriaEspecialidad.basica, 30, 45),
  odontologia('ODONTOLOGIA', 'Odontología', CategoriaEspecialidad.basica, 45, 60),
  pediatria('PEDIATRIA', 'Pediatría', CategoriaEspecialidad.basica, 30, 45, nota: '< 10 años'),
  cardiologia('CARDIOLOGIA', 'Cardiología', CategoriaEspecialidad.interna, 20, 40),
  nefrologia('NEFROLOGIA', 'Nefrología', CategoriaEspecialidad.interna, 20, 40),
  neumologia('NEUMOLOGIA', 'Neumología', CategoriaEspecialidad.interna, 20, 30),
  endocrinologia('ENDOCRINOLOGIA', 'Endocrinología', CategoriaEspecialidad.interna, 20, 40),
  gastroenterologia('GASTROENTEROLOGIA', 'Gastroenterología', CategoriaEspecialidad.interna, 20, 30,
      cirugiaMin: 30, cirugiaMax: 120),
  hematologia('HEMATOLOGIA', 'Hematología', CategoriaEspecialidad.interna, 20, 40, cirugiaMin: 30, cirugiaMax: 120),
  infectologia('INFECTOLOGIA', 'Infectología', CategoriaEspecialidad.interna, 30, 45),
  dermatologia('DERMATOLOGIA', 'Dermatología', CategoriaEspecialidad.interna, 15, 30, cirugiaMin: 15, cirugiaMax: 120),
  medicinaTrabajo('MEDICINA_TRABAJO', 'Medicina del Trabajo', CategoriaEspecialidad.interna, 20, 40),
  terapiaIntensiva('TERAPIA_INTENSIVA', 'Terapia Intensiva', CategoriaEspecialidad.interna, 30, 60);

  const Especialidad(
    this.codigo,
    this.nombre,
    this.categoria,
    this.duracionMin,
    this.duracionMax, {
    this.cirugiaMin = 0,
    this.cirugiaMax = 0,
    this.nota,
  });

  /// Valor que viaja en el JSON.
  final String codigo;
  final String nombre;
  final CategoriaEspecialidad categoria;
  final int duracionMin;
  final int duracionMax;

  /// 0 si la especialidad no opera.
  final int cirugiaMin;
  final int cirugiaMax;

  /// Aclaracion que muestra el Figma junto al rango (Pediatria: "< 10 años").
  final String? nota;

  bool get esBasica => categoria == CategoriaEspecialidad.basica;
  bool get esInterna => categoria == CategoriaEspecialidad.interna;
  bool get operaEnQuirofano => cirugiaMax > 0;

  /// "30–45 min" (y la nota, si tiene).
  String get rango => nota == null ? '$duracionMin–$duracionMax min' : '$duracionMin–$duracionMax · $nota';

  static List<Especialidad> get basicas => [
        for (final e in values)
          if (e.esBasica) e
      ];
  static List<Especialidad> get internas => [
        for (final e in values)
          if (e.esInterna) e
      ];
  static List<Especialidad> get queOperan => [
        for (final e in values)
          if (e.operaEnQuirofano) e
      ];

  /// Un codigo desconocido es un error de contrato: no se cambia por otra especialidad.
  static Especialidad desdeCodigo(String codigo) => values.firstWhere((e) => e.codigo == codigo,
      orElse: () => throw FormatException('especialidad desconocida "$codigo"'));
}
