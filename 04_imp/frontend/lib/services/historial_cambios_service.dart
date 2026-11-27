import '../core/api_client.dart';
import '../models/comun/fechas.dart';
import '../models/comun/fechas_iso.dart';
import '../models/historial/cambio_cita.dart';

/// Historial de cambios de las citas (consulta para enfermeria y administrador).
abstract class HistorialCambiosService {
  /// Registros que cumplen el filtro, del mas nuevo al mas viejo.
  Future<List<CambioCita>> buscar(FiltroCambios filtro);
}

/// Con el backend: `GET /api/v1/admin/cambios?cita=&usuario=&desde=&hasta=` (ENFERMERIA y ADMINISTRADOR).
/// Si "Cita" es un numero se busca esa cita en el backend; si es texto ("Ficha 3", "Ramos") se filtra aqui.
class ApiHistorialCambiosService implements HistorialCambiosService {
  ApiHistorialCambiosService(this._api);

  final ApiClient _api;

  @override
  Future<List<CambioCita>> buscar(FiltroCambios filtro) async {
    final textoCita = filtro.cita.trim().replaceFirst('#', '');
    final citaId = int.tryParse(textoCita);
    final json = await _api.get('/api/v1/admin/cambios', consulta: {
      if (citaId != null) 'cita': '$citaId',
      if (filtro.usuario.trim().isNotEmpty) 'usuario': filtro.usuario.trim(),
      if (filtro.desde != null) 'desde': isoDe(filtro.desde!),
      if (filtro.hasta != null) 'hasta': isoDe(filtro.hasta!),
    });
    final cambios = leerRespuesta(() {
      final datos = json['datos'];
      if (datos is! List) {
        throw const FormatException('se esperaba una lista de cambios');
      }
      return [for (final c in datos) CambioCita.fromJson(Map<String, dynamic>.from(c as Map))];
    });
    if (citaId != null || textoCita.isEmpty) {
      return cambios;
    }
    final soloTexto = FiltroCambios(cita: textoCita);
    return [
      for (final c in cambios)
        if (soloTexto.acepta(c)) c
    ];
  }
}

/// En memoria (con ENFERMERIA_FAKE o AUTH_FAKE). Las pantallas de prueba de enfermeria
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
