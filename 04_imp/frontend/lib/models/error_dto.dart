class ErrorDto {
  const ErrorDto({required this.codigo, required this.mensaje});

  final String codigo;
  final String mensaje;

  factory ErrorDto.fromJson(Map<String, dynamic> json) {
    return ErrorDto(
      codigo: json['codigo'] as String? ?? 'ERROR_DESCONOCIDO',
      mensaje: json['mensaje'] as String? ?? 'Ocurrio un error inesperado.',
    );
  }

  Map<String, dynamic> toJson() {
    return {'codigo': codigo, 'mensaje': mensaje};
  }
}