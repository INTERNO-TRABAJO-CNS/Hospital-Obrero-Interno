import '../core/api_client.dart';
import '../models/catalogo/especialidad.dart';
import '../models/comun/fechas.dart';
import '../models/enfermeria/ficha_dia.dart';
import '../models/historial/cambio_cita.dart';
import 'historial_cambios_service.dart';

/// Pantallas de enfermeria: fichas del dia (solo lectura) y confirmar asistencia.
abstract class EnfermeriaService {
  /// Citas comunes de hoy en todos los consultorios.
  Future<List<FichaDelDia>> fichasDeHoy();

  /// Pasa la cita a ASISTENCIA_CONFIRMADA. Errores: FUERA_DE_PLAZO (pasaron los 30 minutos
  /// previos: la cita se cancela sola), ESTADO_INVALIDO, NO_ENCONTRADA.
  Future<FichaDelDia> confirmarLlegada(int citaId, {required String usuario});
}

/// Con el backend (CU-09 y CU-12). El usuario que confirma sale del token de la sesion, no se envia.
class ApiEnfermeriaService implements EnfermeriaService {
  ApiEnfermeriaService(this._api);

  final ApiClient _api;

  /// `GET /api/v1/fichas/hoy` (rol ENFERMERIA): todas las citas de hoy, en cualquier estado.
  @override
  Future<List<FichaDelDia>> fichasDeHoy() async {
    final json = await _api.get('/api/v1/fichas/hoy');
    return leerRespuesta(() {
      final datos = json['datos'];
      if (datos is! List) {
        throw const FormatException('se esperaba una lista de fichas');
      }
      return [for (final f in datos) FichaDelDia.fromJson(Map<String, dynamic>.from(f as Map))]
        ..sort((a, b) => a.inicio.compareTo(b.inicio));
    });
  }

  /// `POST /api/v1/citas/{id}/confirmar-asistencia`. Errores del backend: FUERA_DE_PLAZO,
  /// ESTADO_NO_PERMITE, NO_ES_DE_HOY, NO_ENCONTRADA.
  @override
  Future<FichaDelDia> confirmarLlegada(int citaId, {required String usuario}) async {
    await _api.post('/api/v1/citas/$citaId/confirmar-asistencia');
    final hoy = await fichasDeHoy();
    return hoy.firstWhere((f) => f.citaId == citaId,
        orElse: () => throw const ApiException('NO_ENCONTRADA', 'La cita confirmada ya no figura entre las de hoy'));
  }
}

/// En memoria (con `--dart-define=ENFERMERIA_FAKE=true` o AUTH_FAKE). Los horarios se arman
/// alrededor de la hora actual para que siempre haya citas atendidas y otras por confirmar.
class FakeEnfermeriaService implements EnfermeriaService {
  FakeEnfermeriaService({DateTime Function()? reloj, FakeHistorialCambiosService? historial})
      : _reloj = reloj ?? DateTime.now,
        _historial = historial {
    _cargarEjemplo();
  }

  final DateTime Function() _reloj;
  final FakeHistorialCambiosService? _historial;
  final List<FichaDelDia> fichas = [];

  @override
  Future<List<FichaDelDia>> fichasDeHoy() async {
    _cancelarVencidas();
    final hoy = soloFecha(_reloj());
    return [
      for (final f in fichas)
        if (soloFecha(f.inicio) == hoy) f
    ];
  }

  @override
  Future<FichaDelDia> confirmarLlegada(int citaId, {required String usuario}) async {
    final ahora = _reloj();
    final i = fichas.indexWhere((f) => f.citaId == citaId);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'La cita no existe', estado: 404);
    }
    final f = fichas[i];
    if (f.estado == EstadoCita.programada && ahora.isAfter(f.limiteConfirmacion)) {
      _cancelarVencidas();
      throw ApiException(
        'FUERA_DE_PLAZO',
        'Se podía confirmar hasta las ${horaTexto(f.limiteConfirmacion)}; la cita se canceló automáticamente.',
        estado: 409,
      );
    }
    if (f.estado != EstadoCita.programada) {
      throw ApiException(
          'ESTADO_INVALIDO', 'Solo se confirma la llegada de una cita PROGRAMADA (esta está ${f.estado.codigo}).',
          estado: 409);
    }
    final confirmada = fichas[i] = f.copiar(estado: EstadoCita.asistenciaConfirmada);
    _historial?.registrar(CambioCita(
      fecha: ahora,
      cita: textoCita(f),
      accion: 'Asistencia confirmada',
      usuario: usuario,
      motivo: 'Paciente llegó al hospital',
    ));
    return confirmada;
  }

  /// Pasado el plazo, la cita PROGRAMADA se cancela sola (Figma "Confirmar asistencia").
  void _cancelarVencidas() {
    final ahora = _reloj();
    for (var i = 0; i < fichas.length; i++) {
      final f = fichas[i];
      if (f.estado == EstadoCita.programada && ahora.isAfter(f.limiteConfirmacion)) {
        fichas[i] = f.copiar(estado: EstadoCita.cancelada);
        _historial?.registrar(CambioCita(
          fecha: f.limiteConfirmacion,
          cita: textoCita(f),
          accion: 'Cancelada (automática)',
          usuario: 'Sistema',
          motivo: 'No confirmó asistencia',
        ));
      }
    }
  }

  void _cargarEjemplo() {
    final ahora = _reloj();
    // Ancla: la hora actual redondeada a 5 minutos, sin pasar de las 21:30.
    var ancla = DateTime(ahora.year, ahora.month, ahora.day, ahora.hour, ahora.minute - ahora.minute % 5);
    final tope = DateTime(ahora.year, ahora.month, ahora.day, 21, 30);
    if (ancla.isAfter(tope)) {
      ancla = tope;
    }
    var id = 500;
    FichaDelDia ficha(int numero, int minutos, String paciente, String afiliado, Especialidad e, String medico,
            String consultorio, EstadoCita estado) =>
        FichaDelDia(
          citaId: ++id,
          numeroFicha: numero,
          inicio: ancla.add(Duration(minutes: minutos)),
          paciente: paciente,
          numeroAfiliado: afiliado,
          especialidad: e,
          medico: medico,
          consultorio: consultorio,
          estado: estado,
        );
    const familiar = Especialidad.consultorioFamiliar;
    const odonto = Especialidad.odontologia;
    const pediatria = Especialidad.pediatria;
    fichas.addAll([
      ficha(1, -150, 'Marcos Condori', '4512-01', familiar, 'Dr. Ramos', '4', EstadoCita.atendida),
      ficha(2, -105, 'Ana Mamani', '4533-07', familiar, 'Dr. Ramos', '4', EstadoCita.atendida),
      ficha(3, -60, 'Elena Copa', '4587-12', familiar, 'Dr. Ramos', '4', EstadoCita.asistenciaConfirmada),
      ficha(4, 82, 'Luis Quispe', '4601-03', familiar, 'Dr. Ramos', '4', EstadoCita.programada),
      ficha(1, -120, 'Elena Rojas', '4478-22', odonto, 'Dr. Mamani', '2', EstadoCita.atendida),
      ficha(2, 37, 'Pedro Alanoca', '4499-15', odonto, 'Dr. Mamani', '2', EstadoCita.programada),
      ficha(1, -90, 'Sofía Vargas', '4520-31', pediatria, 'Dra. Quispe', '6', EstadoCita.cancelada),
      ficha(2, 52, 'Mateo Flores', '4562-09', pediatria, 'Dra. Quispe', '6', EstadoCita.programada),
    ]);
    final confirmada = fichas[2];
    _historial?.registrar(CambioCita(
      fecha: confirmada.inicio.subtract(const Duration(minutes: 65)),
      cita: textoCita(confirmada),
      accion: 'Asistencia confirmada',
      usuario: 'Enfermería · M. Flores',
      motivo: 'Paciente llegó al hospital',
    ));
    final cancelada = fichas[6];
    _historial?.registrar(CambioCita(
      fecha: cancelada.inicio.subtract(const Duration(hours: 3)),
      cita: textoCita(cancelada),
      accion: 'Cancelada por paciente',
      usuario: 'Paciente · S. Vargas',
      motivo: 'Paciente canceló',
    ));
  }
}

/// "Ficha 3 · Ramos" (como en el historial de cambios).
String textoCita(FichaDelDia f) => 'Ficha ${f.numeroFicha} · ${f.medico.split(' ').last}';
