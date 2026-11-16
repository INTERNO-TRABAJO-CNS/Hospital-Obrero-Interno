class CitaModel {
  final int citaId;
  final int pacienteId;
  final String nombrePaciente;
  final String documentoPaciente;
  final int medicoId;
  final String nombreMedico;
  final String especialidad;
  final int consultorio;
  final String fecha;
  final String hora;
  final int turno;
  final String estado;

  CitaModel({
    required this.citaId,
    required this.pacienteId,
    required this.nombrePaciente,
    required this.documentoPaciente,
    required this.medicoId,
    required this.nombreMedico,
    required this.especialidad,
    required this.consultorio,
    required this.fecha,
    required this.hora,
    required this.turno,
    required this.estado,
  });

  factory CitaModel.fromJson(Map<String, dynamic> json) {
    return CitaModel(
      citaId: json['citaId'] ?? 0,
      pacienteId: json['pacienteId'] ?? 0,
      nombrePaciente: json['nombrePaciente'] ?? '',
      documentoPaciente: json['documentoPaciente'] ?? '',
      medicoId: json['medicoId'] ?? 0,
      nombreMedico: json['nombreMedico'] ?? '',
      especialidad: json['especialidad'] ?? '',
      consultorio: json['consultorio'] ?? 0,
      fecha: json['fecha'] ?? '',
      hora: json['hora'] ?? '',
      turno: json['turno'] ?? 0,
      estado: json['estado'] ?? '',
    );
  }
}