// Respuestas del backend de reportes (CU-19) e indicadores (CU-20): GET /api/v1/admin/reportes/...

/// Citas del periodo en un estado (ReporteEstadoDTO).
class ConteoEstado {
  const ConteoEstado({required this.estado, required this.cantidad, required this.automaticas});

  factory ConteoEstado.desdeJson(Map<String, dynamic> json) => ConteoEstado(
        estado: json['estado'] as String,
        cantidad: (json['cantidad'] as num?)?.toInt() ?? 0,
        automaticas: (json['automaticas'] as num?)?.toInt() ?? 0,
      );

  final String estado;
  final int cantidad;

  /// Solo en CANCELADA: las que cancelo el sistema (no confirmo hasta T-30).
  final int automaticas;
}

/// Fichas atendidas de un medico frente a la meta diaria (CumplimientoMedicoDTO).
class CumplimientoMedico {
  const CumplimientoMedico({
    required this.medicoId,
    required this.nombreMedico,
    required this.dias,
    required this.fichasAtendidas,
    required this.promedioDiario,
    required this.bajoMeta,
  });

  factory CumplimientoMedico.desdeJson(Map<String, dynamic> json) => CumplimientoMedico(
        medicoId: (json['medicoId'] as num).toInt(),
        nombreMedico: json['nombreMedico'] as String? ?? '',
        dias: (json['dias'] as num?)?.toInt() ?? 0,
        fichasAtendidas: (json['fichasAtendidas'] as num?)?.toInt() ?? 0,
        promedioDiario: (json['promedioDiario'] as num?)?.toDouble() ?? 0,
        bajoMeta: json['bajoMeta'] as bool? ?? false,
      );

  final int medicoId;
  final String nombreMedico;
  final int dias;
  final int fichasAtendidas;
  final double promedioDiario;
  final bool bajoMeta;
}

/// Cantidad por especialidad o por consultorio (ReporteGrupoDTO).
class ConteoGrupo {
  const ConteoGrupo({required this.grupo, required this.cantidad});

  factory ConteoGrupo.desdeJson(Map<String, dynamic> json) =>
      ConteoGrupo(grupo: json['grupo'] as String, cantidad: (json['cantidad'] as num?)?.toInt() ?? 0);

  final String grupo;
  final int cantidad;
}

/// Indicadores de gestion (IndicadoresDTO). Un valor sin datos que lo respalden llega en `null` (CU-20 3a).
class IndicadoresGestion {
  const IndicadoresGestion({
    required this.solicitudes,
    required this.sinCupo,
    required this.canceladas,
    required this.canceladasAutomaticas,
    this.fichasPorMedicoDia,
    this.minutosConfirmacionFin,
  });

  factory IndicadoresGestion.desdeJson(Map<String, dynamic> json) => IndicadoresGestion(
        solicitudes: (json['solicitudes'] as num?)?.toInt() ?? 0,
        sinCupo: (json['sinCupo'] as num?)?.toInt() ?? 0,
        canceladas: (json['canceladas'] as num?)?.toInt() ?? 0,
        canceladasAutomaticas: (json['canceladasAutomaticas'] as num?)?.toInt() ?? 0,
        fichasPorMedicoDia: (json['fichasPorMedicoDia'] as num?)?.toDouble(),
        minutosConfirmacionFin: (json['minutosConfirmacionFin'] as num?)?.toDouble(),
      );

  final int solicitudes;
  final int sinCupo;
  final int canceladas;
  final int canceladasAutomaticas;
  final double? fichasPorMedicoDia;
  final double? minutosConfirmacionFin;
}
