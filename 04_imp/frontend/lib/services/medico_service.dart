import '../core/api_client.dart';
import '../models/medico/agenda_medico.dart';
import '../models/medico/atencion.dart';
import '../models/medico/historial_paciente.dart';
import '../models/medico/medico_actual.dart';

/// Datos del médico de la sesión.
abstract class MedicoService {
  /// `GET /api/v1/medico/yo`.
  Future<MedicoActual> yo();
}

/// CU-13: agenda del médico.
abstract class AgendaMedicoService {
  /// `GET /api/v1/medico/agenda?fecha=YYYY-MM-DD` (sin fecha = hoy).
  Future<AgendaDia> agenda([String? fecha]);
}

/// CU-15: atención y receta.
abstract class AtencionService {
  /// `GET /api/v1/citas/{id}/atencion`.
  Future<DatosAtencion> datos(int citaId);

  /// `POST /api/v1/citas/{id}/atencion`: guarda la receta y finaliza la cita (ATENDIDA).
  Future<void> registrar(int citaId, String medicacion);
}

/// CU-14: historial del paciente (solo lectura, solo pacientes de su agenda).
abstract class HistorialPacienteService {
  /// `GET /api/v1/medico/pacientes/{pacienteId}/historial`.
  Future<HistorialPaciente> historial(int pacienteId);

  /// `GET /api/v1/medico/pacientes?afiliado=`.
  Future<List<PacienteDeAgenda>> buscar(String numeroAfiliado);
}

class ApiMedicoService implements MedicoService {
  ApiMedicoService(this._api);

  final ApiClient _api;

  @override
  Future<MedicoActual> yo() async {
    final json = await _api.get('/api/v1/medico/yo');
    return leerRespuesta(() => MedicoActual.fromJson(json));
  }
}

class ApiAgendaMedicoService implements AgendaMedicoService {
  ApiAgendaMedicoService(this._api);

  final ApiClient _api;

  @override
  Future<AgendaDia> agenda([String? fecha]) async {
    final json = await _api.get('/api/v1/medico/agenda', consulta: fecha == null ? null : {'fecha': fecha});
    return leerRespuesta(() => AgendaDia.fromJson(json));
  }
}

class ApiAtencionService implements AtencionService {
  ApiAtencionService(this._api);

  final ApiClient _api;

  @override
  Future<DatosAtencion> datos(int citaId) async {
    final json = await _api.get('/api/v1/citas/$citaId/atencion');
    return leerRespuesta(() => DatosAtencion.fromJson(json));
  }

  @override
  Future<void> registrar(int citaId, String medicacion) =>
      _api.post('/api/v1/citas/$citaId/atencion', {'medicacion': medicacion});
}

class ApiHistorialPacienteService implements HistorialPacienteService {
  ApiHistorialPacienteService(this._api);

  final ApiClient _api;

  @override
  Future<HistorialPaciente> historial(int pacienteId) async {
    final json = await _api.get('/api/v1/medico/pacientes/$pacienteId/historial');
    return leerRespuesta(() => HistorialPaciente.fromJson(json));
  }

  @override
  Future<List<PacienteDeAgenda>> buscar(String numeroAfiliado) async {
    final json = await _api.get('/api/v1/medico/pacientes', consulta: {'afiliado': numeroAfiliado.trim()});
    return leerRespuesta(() => [
          for (final p in (json['datos'] as List? ?? const []))
            PacienteDeAgenda.fromJson(Map<String, dynamic>.from(p as Map))
        ]);
  }
}
