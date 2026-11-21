
class TvFichaModel {
  final int numeroFicha;
  final String hora;
  final String especialidad;
  final int consultorio;
  final String estado;

  const TvFichaModel({
    required this.numeroFicha,
    required this.hora,
    required this.especialidad,
    required this.consultorio,
    required this.estado,
  });

  factory TvFichaModel.fromJson(Map<String, dynamic> json) {
    return TvFichaModel(
      numeroFicha: json['numeroFicha'] ?? 0,
      hora: json['hora']?.toString() ?? '',
      especialidad: json['especialidad']?.toString() ?? '',
      consultorio: json['consultorio'] ?? 0,
      estado: json['estado']?.toString() ?? '',
    );
  }
}
