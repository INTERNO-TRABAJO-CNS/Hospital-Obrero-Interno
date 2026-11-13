import '../core/api_client.dart';
import '../models/auth/sesion.dart';

/// CU-01: iniciar y cerrar sesion.
abstract class AuthService {
  /// `POST /api/v1/auth/login`. Credenciales invalidas -> ApiException('CREDENCIALES_INVALIDAS');
  /// 5 intentos fallidos -> INTENTOS_EXCEDIDOS (429).
  Future<Sesion> login(String usuario, String clave);

  /// `POST /api/v1/auth/logout`: el backend invalida el token.
  Future<void> logout();
}

class ApiAuthService implements AuthService {
  ApiAuthService(this._api);

  final ApiClient _api;

  @override
  Future<Sesion> login(String usuario, String clave) async {
    final json = await _api.post('/api/v1/auth/login', {'usuario': usuario.trim(), 'clave': clave});
    return leerRespuesta(() => Sesion.fromJson(json));
  }

  @override
  Future<void> logout() => _api.post('/api/v1/auth/logout');
}

/// Sin backend: `flutter run --dart-define=AUTH_FAKE=true`. Usuarios de prueba:
/// paciente/paciente123, enfermera/enfermera123 y admin/admin123.
class FakeAuthService implements AuthService {
  final Map<String, (String, Sesion)> cuentas = {
    'paciente': (
      'paciente123',
      const Sesion(
          usuarioId: 1,
          rol: Rol.paciente,
          personaId: 1,
          nombre: 'Juan Carlos Choque Mamani',
          token: 'prueba-1',
          numeroAfiliado: 'AF-10001'),
    ),
    'enfermera': (
      'enfermera123',
      const Sesion(usuarioId: 2, rol: Rol.enfermeria, personaId: 2, nombre: 'María Flores', token: 'prueba-2'),
    ),
    'admin': (
      'admin123',
      const Sesion(usuarioId: 3, rol: Rol.administrador, personaId: 3, nombre: 'Administrador CNS', token: 'prueba-3'),
    ),
  };

  int _ultimoUsuarioId = 3;

  /// Cuenta de un titular recien registrado (la usa FakePacienteService).
  void agregarCuentaPaciente(String usuario, String clave, int pacienteId, String nombre) {
    final id = ++_ultimoUsuarioId;
    cuentas[usuario.trim().toLowerCase()] = (
      clave,
      Sesion(usuarioId: id, rol: Rol.paciente, personaId: pacienteId, nombre: nombre, token: 'prueba-$id'),
    );
  }

  @override
  Future<Sesion> login(String usuario, String clave) async {
    final cuenta = cuentas[usuario.trim().toLowerCase()];
    if (cuenta == null || cuenta.$1 != clave) {
      throw const ApiException('CREDENCIALES_INVALIDAS', 'Usuario o contraseña incorrectos', estado: 401);
    }
    return cuenta.$2;
  }

  @override
  Future<void> logout() async {}
}
