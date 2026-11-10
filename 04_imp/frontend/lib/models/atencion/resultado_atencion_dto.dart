class ResultadoAtencionDto {
  const ResultadoAtencionDto({required this.resultado});

  final String resultado;

  factory ResultadoAtencionDto.fromJson(dynamic json) {
    if (json is String) {
      return ResultadoAtencionDto(resultado: json);
    }
    if (json is! Map<String, dynamic>) {
      return const ResultadoAtencionDto(resultado: '');
    }
    return ResultadoAtencionDto(
      resultado: json['resultado'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() => {'resultado': resultado};
}