/// Pestañas de "Reportes e indicadores".
enum TipoReporte {
  porEstado('Por estado'),
  porMedico('Por médico'),
  porEspecialidad('Por especialidad'),
  porConsultorio('Por consultorio'),
  internasYQuirofanos('Fichas internas y quirófanos');

  const TipoReporte(this.titulo);

  final String titulo;
}

/// Una barra del grafico.
class FilaGrafico {
  const FilaGrafico({required this.etiqueta, required this.valor, required this.texto, this.alerta = false});

  final String etiqueta;
  final num valor;

  /// Texto a la derecha ("7 fichas", "4 fichas · bajo la meta").
  final String texto;

  /// Barra y texto en rojo (p. ej. bajo la meta).
  final bool alerta;
}

/// Tarjeta de indicador ("86 solicitudes" / "Demanda (semana)").
class Indicador {
  const Indicador(this.valor, this.etiqueta);

  final String valor;
  final String etiqueta;
}

class Reporte {
  const Reporte({
    required this.titulo,
    required this.filas,
    required this.maximo,
    required this.indicadores,
    required this.nota,
  });

  final String titulo;
  final List<FilaGrafico> filas;

  /// Valor que llena la barra completa.
  final num maximo;
  final List<Indicador> indicadores;
  final String nota;
}
