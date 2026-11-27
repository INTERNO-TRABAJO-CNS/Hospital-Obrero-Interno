import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/citas/cita_paciente.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/solicitud/solicitud.dart';
import 'cancelacion_service.dart';

/// CU-08 y CU-10: las citas del titular autenticado y de sus beneficiarios (RN-05). El backend saca al titular
/// del token y verifica que el `pacienteId` de un beneficiario sea de su misma afiliacion.
abstract class CitasPacienteService {
  /// `GET /api/v1/citas/mias`: de la mas reciente a la mas antigua.
  Future<List<CitaPaciente>> misCitas();

  /// `GET /api/v1/citas/{id}`. Error: NO_ES_DEL_PACIENTE, NO_ENCONTRADA.
  Future<CitaPaciente> detalle(int citaId);

  /// `POST /api/v1/citas/{id}/reprogramar`: cancela la cita y deja una solicitud (prioridad 2) para el
  /// siguiente dia habil. Errores: ESTADO_NO_PERMITE, SIN_DIA_DISPONIBLE, NO_ES_DEL_PACIENTE.
  Future<ResultadoSolicitud> reprogramar(int citaId, {int? pacienteId});
}

class ApiCitasPacienteService implements CitasPacienteService {
  ApiCitasPacienteService(this._api);

  final ApiClient _api;

  @override
  Future<List<CitaPaciente>> misCitas() async {
    final json = await _api.get('/api/v1/citas/mias');
    return leerRespuesta(() {
      final datos = json['datos'];
      if (datos is! List) {
        throw const FormatException('se esperaba una lista de citas');
      }
      return [for (final c in datos) CitaPaciente.fromJson(Map<String, dynamic>.from(c as Map))];
    });
  }

  @override
  Future<CitaPaciente> detalle(int citaId) async {
    final json = await _api.get('/api/v1/citas/$citaId');
    return leerRespuesta(() => CitaPaciente.fromJson(json));
  }

  @override
  Future<ResultadoSolicitud> reprogramar(int citaId, {int? pacienteId}) async {
    final json = await _api.post('/api/v1/citas/$citaId/reprogramar${conPaciente(pacienteId)}');
    return leerRespuesta(() => ResultadoSolicitud.fromJson(json));
  }
}

/// En memoria (AUTH_FAKE y pruebas). Tambien cancela, para que "Mis citas" se vea coherente.
class FakeCitasPacienteService implements CitasPacienteService, CancelacionService {
  FakeCitasPacienteService({DateTime Function()? reloj, bool conDatosDeEjemplo = true})
      : _reloj = reloj ?? DateTime.now {
    if (conDatosDeEjemplo) {
      _cargarEjemplo();
    }
  }

  final DateTime Function() _reloj;
  final List<CitaPaciente> citas = [];
  int _siguienteSolicitud = 900;

  @override
  Future<List<CitaPaciente>> misCitas() async => [...citas]..sort((a, b) => b.inicio.compareTo(a.inicio));

  @override
  Future<CitaPaciente> detalle(int citaId) async => _buscar(citaId);

  @override
  Future<void> cancelar(int citaId, {int? pacienteId}) async {
    final cita = _buscar(citaId);
    if (!cita.sePuedeModificar) {
      throw const ApiException('ESTADO_NO_PERMITE', 'Solo se puede cancelar una cita PROGRAMADA', estado: 409);
    }
    _reemplazar(cita.copiar(estado: EstadoCita.cancelada));
  }

  @override
  Future<ResultadoSolicitud> reprogramar(int citaId, {int? pacienteId}) async {
    final cita = _buscar(citaId);
    if (!cita.sePuedeModificar) {
      throw const ApiException('ESTADO_NO_PERMITE', 'Solo se puede reprogramar una cita PROGRAMADA', estado: 409);
    }
    final hoy = DateTime(_reloj().year, _reloj().month, _reloj().day);
    var dia = DateTime(cita.inicio.year, cita.inicio.month, cita.inicio.day);
    if (dia.isBefore(hoy)) {
      dia = hoy;
    }
    do {
      dia = DateTime(dia.year, dia.month, dia.day + 1);
    } while (dia.weekday == DateTime.saturday || dia.weekday == DateTime.sunday);
    _reemplazar(cita.copiar(estado: EstadoCita.cancelada));
    return ResultadoSolicitud(
      solicitudId: ++_siguienteSolicitud,
      estado: EstadoSolicitudApp.pendiente,
      mensaje: 'Su solicitud fue registrada. El sistema le asignará médico, día y hora en cuanto haya un cupo '
          'disponible.',
      especialidad: cita.especialidad,
      diaSolicitado: dia,
      prioridad: 2,
      ordenLlegada: _siguienteSolicitud,
    );
  }

  CitaPaciente _buscar(int citaId) => citas.firstWhere((c) => c.citaId == citaId,
      orElse: () => throw const ApiException('NO_ENCONTRADA', 'No existe esa cita', estado: 404));

  void _reemplazar(CitaPaciente nueva) {
    final i = citas.indexWhere((c) => c.citaId == nueva.citaId);
    citas[i] = nueva;
  }

  void _cargarEjemplo() {
    final hoy = _reloj();
    // Dias habiles (lunes a viernes) contando desde hoy, como los asigna el backend.
    DateTime dia(int dias, int hora, int minuto) {
      var d = DateTime(hoy.year, hoy.month, hoy.day + dias, hora, minuto);
      while (d.weekday == DateTime.saturday || d.weekday == DateTime.sunday) {
        d = DateTime(d.year, d.month, d.day + (dias < 0 ? -1 : 1), hora, minuto);
      }
      return d;
    }

    citas.addAll([
      CitaPaciente(
          citaId: 31,
          numeroFicha: 3,
          inicio: dia(2, 9, 30),
          especialidad: Especialidad.consultorioFamiliar,
          medico: 'Dr. Ramos',
          consultorio: '4',
          estado: EstadoCita.programada),
      CitaPaciente(
          citaId: 25,
          numeroFicha: 5,
          inicio: dia(0, 10, 15),
          especialidad: Especialidad.odontologia,
          medico: 'Dr. Mamani',
          consultorio: '2',
          estado: EstadoCita.asistenciaConfirmada),
      CitaPaciente(
          citaId: 12,
          numeroFicha: 2,
          inicio: dia(-23, 8, 45),
          especialidad: Especialidad.pediatria,
          medico: 'Dra. Quispe',
          consultorio: '6',
          estado: EstadoCita.atendida),
      CitaPaciente(
          citaId: 8,
          numeroFicha: 4,
          inicio: dia(-31, 11, 0),
          especialidad: Especialidad.consultorioFamiliar,
          medico: 'Dr. Ramos',
          consultorio: '4',
          estado: EstadoCita.cancelada),
    ]);
  }
}
