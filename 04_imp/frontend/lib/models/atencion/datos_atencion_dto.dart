class DatosAtencionDto {
  const DatosAtencionDto({
    required this.citaId,
    required this.medicoId,
    required this.afiliado,
    required this.ficha,
    required this.fecha,
    required this.hora,
    required this.consulta,
    required this.estado,
  });

  final int citaId;
  final int medicoId;
  final String afiliado;
  final String ficha;
  final String fecha;
  final String hora;
  final String consulta;
  final String estado;

  factory DatosAtencionDto.fromJson(Map<String, dynamic> json) {
    return DatosAtencionDto(
      citaId: (json['citaId'] as num?)?.toInt() ?? 0,
      medicoId: (json['medicoId'] as num?)?.toInt() ?? 0,
      afiliado: json['afiliado'] as String? ?? '',
      ficha: json['ficha'] as String? ?? '',
      fecha: json['fecha'] as String? ?? '',
      hora: json['hora'] as String? ?? '',
      consulta: json['consulta'] as String? ?? '',
      estado: json['estado'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'citaId': citaId,
      'medicoId': medicoId,
      'afiliado': afiliado,
      'ficha': ficha,
      'fecha': fecha,
      'hora': hora,
      'consulta': consulta,
      'estado': estado,
    };
  }
}