import '../core/api_client.dart';
import '../models/auth/sesion.dart';
import '../models/personal/cuenta.dart';
import 'catalogo_service.dart';

/// Cuentas del personal (solo el administrador).
abstract class CuentasService {
  Future<List<Cuenta>> listar();

  /// Errores: USUARIO_DUPLICADO, DATOS_INVALIDOS, NO_ENCONTRADA (medico inexistente o de baja).
  Future<Cuenta> crear(NuevaCuenta datos);

  /// No se borra. La propia cuenta no se puede dar de baja (NO_PERMITIDO).
  Future<void> darDeBaja(int usuarioId);
}

/// `GET/POST /api/v1/admin/usuarios` y `DELETE /api/v1/admin/usuarios/{id}`.
class ApiCuentasService implements CuentasService {
  ApiCuentasService(this._api);

  final ApiClient _api;

  static const String _base = '/api/v1/admin/usuarios';

  @override
  Future<List<Cuenta>> listar() async => listaDe(await _api.get(_base), Cuenta.desdeJson);

  @override
  Future<Cuenta> crear(NuevaCuenta datos) async => unoDe(await _api.post(_base, datos.aJson()), Cuenta.desdeJson);

  @override
  Future<void> darDeBaja(int usuarioId) => _api.delete('$_base/$usuarioId');
}

/// En memoria (con `--dart-define=CATALOGO_FAKE=true`), con los mismos codigos de error.
class FakeCuentasService implements CuentasService {
  final List<Cuenta> cuentas = [
    const Cuenta(
        usuarioId: 1,
        usuario: 'admin',
        rol: Rol.administrador,
        personaId: 1,
        nombre: 'Administrador CNS',
        activo: true),
    const Cuenta(
        usuarioId: 2, usuario: 'enfermera', rol: Rol.enfermeria, personaId: 2, nombre: 'María Flores', activo: true),
    const Cuenta(
        usuarioId: 3, usuario: 'dr.ramos', rol: Rol.medico, personaId: 1, nombre: 'Carlos Ramos', activo: true),
  ];

  @override
  Future<List<Cuenta>> listar() async => List.of(cuentas);

  @override
  Future<Cuenta> crear(NuevaCuenta datos) async {
    final error = datos.validar();
    if (error != null) {
      throw ApiException('DATOS_INVALIDOS', error, estado: 400);
    }
    final usuario = datos.usuario.trim().toLowerCase();
    if (cuentas.any((c) => c.usuario.toLowerCase() == usuario)) {
      throw const ApiException('USUARIO_DUPLICADO', 'Ese nombre de usuario ya está en uso', estado: 409);
    }
    final nueva = Cuenta(
      usuarioId: cuentas.fold(0, (m, c) => c.usuarioId > m ? c.usuarioId : m) + 1,
      usuario: datos.usuario.trim(),
      rol: datos.rol,
      personaId: datos.personaId ?? 0,
      nombre: datos.nombre.trim(),
      activo: true,
    );
    cuentas.add(nueva);
    return nueva;
  }

  @override
  Future<void> darDeBaja(int usuarioId) async {
    final i = cuentas.indexWhere((c) => c.usuarioId == usuarioId);
    if (i < 0) {
      throw const ApiException('NO_ENCONTRADA', 'Cuenta no encontrada', estado: 404);
    }
    cuentas[i] = cuentas[i].copiar(activo: false);
  }
}
