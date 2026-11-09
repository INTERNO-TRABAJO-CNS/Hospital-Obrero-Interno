/// Especialidades del hospital con el rango de duracion de ficha (minutos).
/// Debe coincidir con `TipoEspecialidad` del backend.
enum Especialidad {
  consultorioFamiliar('CONSULTORIO_FAMILIAR', 'Consultorio Familiar', 30, 45),
  odontologia('ODONTOLOGIA', 'Odontología', 45, 60),
  pediatria('PEDIATRIA', 'Pediatría', 30, 45);

  const Especialidad(this.codigo, this.nombre, this.duracionMin, this.duracionMax);

  /// Valor que viaja en el JSON.
  final String codigo;
  final String nombre;
  final int duracionMin;
  final int duracionMax;

  String get rango => '$duracionMin–$duracionMax min';

  static Especialidad desdeCodigo(String codigo) =>
      values.firstWhere((e) => e.codigo == codigo, orElse: () => consultorioFamiliar);
}
