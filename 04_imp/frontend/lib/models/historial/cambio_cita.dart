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

  final DateTime fecha;

  /// "Ficha 3 · Ramos".
  final String cita;

  /// "Asistencia confirmada", "Cancelada (automática)"...
  final String accion;

  /// "Enfermería · M. Flores", "Sistema", "Médico · Dr. Ramos"...
  final String usuario;
  final String motivo;
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
