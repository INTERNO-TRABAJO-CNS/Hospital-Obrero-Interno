import '../core/api_client.dart';
import '../models/catalogo/consultorio.dart';
import '../models/catalogo/dia_no_habil.dart';
import '../models/catalogo/especialidad.dart';
import '../models/catalogo/horario.dart';
import '../models/catalogo/medico.dart';

/// CU-02: llamadas a los catalogos del administrador.
///
/// Las reglas (cupo 5-7, duracion por especialidad, cruces de horario, citas
/// programadas) las decide el backend; si algo no se puede, llega una
/// [ApiException] con su codigo (VALIDACION_CUPO, CAMBIO_AFECTA_CITAS, ...).
abstract class CatalogoService {
  Future<List<Medico>> listarMedicos({Especialidad? filtro});
  Future<Medico> crearMedico(Medico medico);
  Future<Medico> modificarMedico(int id, Medico medico);
  Future<void> darDeBajaMedico(int id);

  Future<List<Consultorio>> listarConsultorios();
  Future<Consultorio> crearConsultorio(Consultorio consultorio);
  Future<Consultorio> modificarConsultorio(int id, Consultorio consultorio);
  Future<void> darDeBajaConsultorio(int id);

  Future<List<Horario>> listarHorarios();
  Future<Horario> definirHorario(Horario horario);
  Future<Horario> modificarHorario(int id, Horario horario);
  Future<void> darDeBajaHorario(int id);

  Future<List<DiaNoHabil>> listarDiasNoHabiles();
  Future<DiaNoHabil> registrarDiaNoHabil(DiaNoHabil dia);
  Future<DiaNoHabil> modificarDiaNoHabil(int id, DiaNoHabil dia);
  Future<void> darDeBajaDiaNoHabil(int id);
}

/// Implementacion real: REST bajo `/api/v1/admin`.
class ApiCatalogoService implements CatalogoService {
  ApiCatalogoService(this._api);

  final ApiClient _api;

  static const String _base = '/api/v1/admin';

  @override
  Future<List<Medico>> listarMedicos({Especialidad? filtro}) async {
    final r = await _api.get('$_base/medicos', consulta: filtro == null ? null : {'especialidad': filtro.codigo});
    return _lista(r, Medico.desdeJson);
  }

  @override
  Future<Medico> crearMedico(Medico medico) async =>
      Medico.desdeJson(await _api.post('$_base/medicos', medico.aJson()));

  @override
  Future<Medico> modificarMedico(int id, Medico medico) async =>
      Medico.desdeJson(await _api.put('$_base/medicos/$id', medico.aJson()));

  @override
  Future<void> darDeBajaMedico(int id) => _api.delete('$_base/medicos/$id');

  @override
  Future<List<Consultorio>> listarConsultorios() async =>
      _lista(await _api.get('$_base/consultorios'), Consultorio.desdeJson);

  @override
  Future<Consultorio> crearConsultorio(Consultorio consultorio) async =>
      Consultorio.desdeJson(await _api.post('$_base/consultorios', consultorio.aJson()));

  @override
  Future<Consultorio> modificarConsultorio(int id, Consultorio consultorio) async =>
      Consultorio.desdeJson(await _api.put('$_base/consultorios/$id', consultorio.aJson()));

  @override
  Future<void> darDeBajaConsultorio(int id) => _api.delete('$_base/consultorios/$id');

  @override
  Future<List<Horario>> listarHorarios() async => _lista(await _api.get('$_base/horarios'), Horario.desdeJson);

  @override
  Future<Horario> definirHorario(Horario horario) async =>
      Horario.desdeJson(await _api.post('$_base/horarios', horario.aJson()));

  @override
  Future<Horario> modificarHorario(int id, Horario horario) async =>
      Horario.desdeJson(await _api.put('$_base/horarios/$id', horario.aJson()));

  @override
  Future<void> darDeBajaHorario(int id) => _api.delete('$_base/horarios/$id');

  @override
  Future<List<DiaNoHabil>> listarDiasNoHabiles() async =>
      _lista(await _api.get('$_base/dias-no-habiles'), DiaNoHabil.desdeJson);

  @override
  Future<DiaNoHabil> registrarDiaNoHabil(DiaNoHabil dia) async =>
      DiaNoHabil.desdeJson(await _api.post('$_base/dias-no-habiles', dia.aJson()));

  @override
  Future<DiaNoHabil> modificarDiaNoHabil(int id, DiaNoHabil dia) async =>
      DiaNoHabil.desdeJson(await _api.put('$_base/dias-no-habiles/$id', dia.aJson()));

  @override
  Future<void> darDeBajaDiaNoHabil(int id) => _api.delete('$_base/dias-no-habiles/$id');

  /// ApiClient entrega las listas JSON dentro de {'datos': [...]}.
  static List<T> _lista<T>(Map<String, dynamic> respuesta, T Function(Map<String, dynamic>) convertir) {
    final datos = respuesta['datos'] as List? ?? const [];
    return [for (final e in datos) convertir(Map<String, dynamic>.from(e as Map))];
  }
}
