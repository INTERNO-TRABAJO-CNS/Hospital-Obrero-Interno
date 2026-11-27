import '../core/api_client.dart';
import '../models/personal/paciente_admin.dart';
import 'catalogo_service.dart';

/// Pacientes del administrador (Catálogos y Gestionar > Pacientes).
abstract class PacientesAdminService {
  Future<List<PacienteAdmin>> listar();

  /// Errores: DATOS_INVALIDOS, YA_REGISTRADO, USUARIO_DUPLICADO, AFILIADO_NO_ENCONTRADO (beneficiario).
  Future<PacienteAdmin> crear(DatosPacienteAdmin datos);

  /// Solo nombres, apellidos y fecha de nacimiento.
  Future<PacienteAdmin> modificar(int pacienteId, DatosPacienteAdmin datos);

  /// Baja lógica. CAMBIO_AFECTA_CITAS si tiene citas programadas.
  Future<void> darDeBaja(int pacienteId);

  Future<PacienteAdmin> reactivar(int pacienteId);
}

/// `/api/v1/admin/pacientes`.
class ApiPacientesAdminService implements PacientesAdminService {
  ApiPacientesAdminService(this._api);

  final ApiClient _api;

  static const String _base = '/api/v1/admin/pacientes';

  @override
  Future<List<PacienteAdmin>> listar() async => listaDe(await _api.get(_base), PacienteAdmin.desdeJson);

  @override
  Future<PacienteAdmin> crear(DatosPacienteAdmin datos) async =>
      unoDe(await _api.post(_base, datos.aJson()), PacienteAdmin.desdeJson);

  @override
  Future<PacienteAdmin> modificar(int pacienteId, DatosPacienteAdmin datos) async =>
      unoDe(await _api.put('$_base/$pacienteId', datos.aJson()), PacienteAdmin.desdeJson);

  @override
  Future<void> darDeBaja(int pacienteId) => _api.delete('$_base/$pacienteId');

  @override
  Future<PacienteAdmin> reactivar(int pacienteId) async =>
      unoDe(await _api.post('$_base/$pacienteId/reactivar'), PacienteAdmin.desdeJson);
}

/// En memoria (con `--dart-define=CATALOGO_FAKE=true`), con los mismos códigos de error.
class FakePacientesAdminService implements PacientesAdminService {
  FakePacientesAdminService({this.conCitas = const {}});

  /// Pacientes que tienen citas programadas (no se pueden dar de baja).
  final Set<int> conCitas;

  final List<PacienteAdmin> pacientes = [
    PacienteAdmin(
        pacienteId: 1,
        numeroAfiliado: 'AF-10001',
        ci: '4512301',
        nombres: 'Juan Carlos',
        apellidos: 'Choque Mamani',
        fechaNacimiento: DateTime(1985, 3, 14),
        titular: true,
        activo: true,
        usuario: 'juanc'),
    PacienteAdmin(
        pacienteId: 2,
        numeroAfiliado: 'AF-10001',
        ci: '8123612',
        nombres: 'Sofía',
        apellidos: 'Choque Ramos',
        fechaNacimiento: DateTime(2018, 10, 5),
        titular: false,
        activo: true),
    PacienteAdmin(
        pacienteId: 3,
        numeroAfiliado: 'AF-10002',
        ci: '5123402',
        nombres: 'Ana María',
        apellidos: 'Quispe Flores',
        fechaNacimiento: DateTime(1990, 7, 22),
        titular: true,
        activo: true),
  ];

  @override
  Future<List<PacienteAdmin>> listar() async => List.of(pacientes);

  @override
  Future<PacienteAdmin> crear(DatosPacienteAdmin d) async {
    final error = d.validar(nuevo: true);
    if (error != null) {
      throw ApiException('DATOS_INVALIDOS', error, estado: 400);
    }
    final numero = d.numeroAfiliado.trim();
    final ci = d.ci.trim().toLowerCase();
    if (pacientes.any((p) => p.numeroAfiliado == numero && (p.ci.toLowerCase() == ci || (d.titular && p.titular)))) {
      throw const ApiException('YA_REGISTRADO', 'El paciente ya está registrado', estado: 409);
    }
    if (!d.titular && !pacientes.any((p) => p.numeroAfiliado == numero && p.titular && p.activo)) {
      throw const ApiException('AFILIADO_NO_ENCONTRADO', 'La afiliación del titular no existe', estado: 404);
    }
    if (d.conCuenta && pacientes.any((p) => p.usuario?.toLowerCase() == d.usuario.trim().toLowerCase())) {
      throw const ApiException('USUARIO_DUPLICADO', 'Ese nombre de usuario ya está en uso', estado: 409);
    }
    final nuevo = PacienteAdmin(
      pacienteId: pacientes.fold(0, (m, p) => p.pacienteId > m ? p.pacienteId : m) + 1,
      numeroAfiliado: numero,
      ci: d.ci.trim(),
      nombres: d.nombres.trim(),
      apellidos: d.apellidos.trim(),
      fechaNacimiento: d.fechaNacimiento!,
      titular: d.titular,
      activo: true,
      usuario: d.conCuenta ? d.usuario.trim() : null,
    );
    pacientes.add(nuevo);
    return nuevo;
  }

  @override
  Future<PacienteAdmin> modificar(int pacienteId, DatosPacienteAdmin d) async {
    final i = _indice(pacienteId);
    final error = d.validar(nuevo: false);
    if (error != null) {
      throw ApiException('DATOS_INVALIDOS', error, estado: 400);
    }
    pacientes[i] = pacientes[i]
        .copiar(nombres: d.nombres.trim(), apellidos: d.apellidos.trim(), fechaNacimiento: d.fechaNacimiento);
    return pacientes[i];
  }

  @override
  Future<void> darDeBaja(int pacienteId) async {
    final i = _indice(pacienteId);
    if (conCitas.contains(pacienteId)) {
      throw const ApiException('CAMBIO_AFECTA_CITAS', 'El paciente tiene citas programadas: no se puede dar de baja',
          estado: 409);
    }
    pacientes[i] = pacientes[i].copiar(activo: false);
  }

  @override
  Future<PacienteAdmin> reactivar(int pacienteId) async {
    final i = _indice(pacienteId);
    pacientes[i] = pacientes[i].copiar(activo: true);
    return pacientes[i];
  }

  int _indice(int pacienteId) {
    final i = pacientes.indexWhere((p) => p.pacienteId == pacienteId);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'No se encontró el paciente', estado: 404);
    }
    return i;
  }
}
