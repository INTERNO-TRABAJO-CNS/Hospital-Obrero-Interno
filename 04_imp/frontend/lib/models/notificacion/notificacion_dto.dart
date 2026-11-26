class NotificacionDto {
  const NotificacionDto({
    required this.pacienteId,
    required this.citaId,
    required this.tipo,
    required this.mensaje,
    required this.creadaEn,
  });

  final int pacienteId;
  final int citaId;
  final String tipo;
  final String mensaje;
  final DateTime creadaEn;

  factory NotificacionDto.fromJson(Map<String, dynamic> json) {
    return NotificacionDto(
      pacienteId: (json['pacienteId'] as num?)?.toInt() ?? 0,
      citaId: (json['citaId'] as num?)?.toInt() ?? 0,
      tipo: json['tipo'] as String? ?? '',
      mensaje: json['mensaje'] as String? ?? '',
      creadaEn: DateTime.tryParse(json['fechaHora'] as String? ?? '') ??
          DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'pacienteId': pacienteId,
      'citaId': citaId,
      'tipo': tipo,
      'mensaje': mensaje,
      'fechaHora': creadaEn.toIso8601String(),
    };
  }
}
