class FichaAgendaModel {
  final int citaId;
  final int numeroFicha;
  final String hora;
  final String nombrePaciente;
  final String estado;

  FichaAgendaModel({
    required this.citaId,
    required this.numeroFicha,
    required this.hora,
    required this.nombrePaciente,
    required this.estado,
  });

  factory FichaAgendaModel.fromJson(Map<String, dynamic> json) {
    return FichaAgendaModel(
      citaId: json['citaId'] ?? 0,
      numeroFicha: json['numeroFicha'] ?? 0,
      hora: json['hora'] ?? '',
      nombrePaciente: json['nombrePaciente'] ?? '',
      estado: json['estado'] ?? '',
    );
  }
}

class AgendaDiaModel {
  final String fecha;
  final int cantidad;
  final List<FichaAgendaModel> fichas;

  AgendaDiaModel({
    required this.fecha,
    required this.cantidad,
    required this.fichas,
  });

  factory AgendaDiaModel.fromJson(Map<String, dynamic> json) {
    var list = (json['fichas'] as List?) ?? [];
    List<FichaAgendaModel> fichasLista = list.map((i) => FichaAgendaModel.fromJson(i)).toList();

    return AgendaDiaModel(
      fecha: json['fecha'] ?? '',
      cantidad: json['cantidad'] ?? 0,
      fichas: fichasLista,
    );
  }
}