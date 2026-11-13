import '../models/comun/fechas.dart';
import '../models/historial/cambio_cita.dart';

/// Historial de cambios de las citas (consulta para enfermeria y administrador).
abstract class HistorialCambiosService {
  /// Registros que cumplen el filtro, del mas nuevo al mas viejo.
  Future<List<CambioCita>> buscar(FiltroCambios filtro);
}

/// En memoria: el backend todavia no guarda el historial. Las otras pantallas de prueba
/// (confirmar asistencia) registran aqui sus cambios con [registrar].
class FakeHistorialCambiosService implements HistorialCambiosService {
  FakeHistorialCambiosService({DateTime Function()? reloj}) : _reloj = reloj ?? DateTime.now {
    _cargarEjemplo();
  }

  final DateTime Function() _reloj;
  final List<CambioCita> _cambios = [];

  /// Solo agrega: los registros no se modifican ni se borran.
  void registrar(CambioCita cambio) => _cambios.add(cambio);

  List<CambioCita> get todos => List.unmodifiable(_cambios);

  @override
  Future<List<CambioCita>> buscar(FiltroCambios filtro) async => [
        for (final c in _cambios)
          if (filtro.acepta(c)) c
      ]..sort((a, b) => b.fecha.compareTo(a.fecha));

  void _cargarEjemplo() {
    final hoy = soloFecha(_reloj());
    DateTime haceDias(int dias, String hora) => conHora(DateTime(hoy.year, hoy.month, hoy.day - dias), hora);
    _cambios.addAll([
      CambioCita(
          fecha: haceDias(2, '09:12'),
          cita: 'Ficha 4 · Ramos',
          accion: 'Cita registrada',
          usuario: 'Sistema',
          motivo: 'Asignación automática'),
      CambioCita(
          fecha: haceDias(1, '11:02'),
          cita: 'Ficha 1 · Ramos',
          accion: 'Atención finalizada',
          usuario: 'Médico · Dr. Ramos',
          motivo: 'Receta registrada'),
      CambioCita(
          fecha: haceDias(1, '16:10'),
          cita: 'Ficha 2 · Mamani',
          accion: 'Cancelada por paciente',
          usuario: 'Paciente · R. Choque',
          motivo: 'Paciente canceló'),
    ]);
  }
}
