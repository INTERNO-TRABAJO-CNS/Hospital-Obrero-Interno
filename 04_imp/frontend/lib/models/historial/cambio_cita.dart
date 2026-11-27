/// Un registro del historial de cambios de las citas (Figma "ADMIN HISTORIAL DE CAMBIOS").
/// Los registros no se modifican ni se borran.
class CambioCita {
  const CambioCita({
    required this.fecha,
    required this.cita,
    required this.accion,
    required this.usuario,
    required this.motivo,
  });

  /// Una fila de `GET /api/v1/admin/cambios` (`CambioCitaVistaDTO` del backend).
  factory CambioCita.fromJson(Map<String, dynamic> json) {
    final citaId = (json['citaId'] as num).toInt();
    final ficha = (json['numeroFicha'] as num?)?.toInt() ?? 0;
    final medico = (json['medico'] as String?)?.trim() ?? '';
    final usuario = (json['usuario'] as String?)?.trim() ?? '';
    final accion = json['accion'] as String? ?? '';
    return CambioCita(
      fecha: DateTime.parse(json['fechaHora'] as String),
      cita: ficha == 0 ? 'Cita #$citaId' : 'Ficha $ficha · ${medico.isEmpty ? '—' : medico} (#$citaId)',
      accion: textoAccion(accion, usuario),
      usuario: usuario.toUpperCase() == 'SISTEMA' ? 'Sistema' : usuario,
      motivo: (json['motivo'] as String?) ?? '',
    );
  }

  final DateTime fecha;

  /// "Ficha 3 · Ramos".
  final String cita;

  /// "Asistencia confirmada", "Cancelada (automática)"...
  final String accion;

  /// "Enfermería · M. Flores", "Sistema", "Médico · Dr. Ramos"...
  final String usuario;
  final String motivo;
}

/// Texto del Figma para cada accion que guarda el backend.
String textoAccion(String accion, String usuario) {
  switch (accion) {
    case 'CREADA':
    case 'PROGRAMADA':
      return 'Cita registrada';
    case 'ASISTENCIA_CONFIRMADA':
      return 'Asistencia confirmada';
    case 'ATENDIDA':
      return 'Atención finalizada';
    case 'CANCELADA':
      return usuario.toUpperCase() == 'SISTEMA' ? 'Cancelada (automática)' : 'Cancelada por paciente';
    default:
      return accion;
  }
}

/// Filtros de la busqueda (todos opcionales).
class FiltroCambios {
  const FiltroCambios({this.cita = '', this.usuario = '', this.desde, this.hasta});

  final String cita;
  final String usuario;
  final DateTime? desde;
  final DateTime? hasta;

  bool acepta(CambioCita c) {
    bool contiene(String texto, String buscado) =>
        buscado.trim().isEmpty || texto.toLowerCase().contains(buscado.trim().toLowerCase());
    final dia = DateTime(c.fecha.year, c.fecha.month, c.fecha.day);
    return contiene(c.cita, cita) &&
        contiene(c.usuario, usuario) &&
        (desde == null || !dia.isBefore(DateTime(desde!.year, desde!.month, desde!.day))) &&
        (hasta == null || !dia.isAfter(DateTime(hasta!.year, hasta!.month, hasta!.day)));
  }
}
