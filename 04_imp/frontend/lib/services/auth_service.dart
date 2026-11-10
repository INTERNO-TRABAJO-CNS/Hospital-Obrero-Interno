import '../core/api_client.dart';
import '../models/auth/sesion.dart';

/// CU-01: iniciar sesion.
abstract class AuthService {
  /// `POST /api/v1/auth/login`. Credenciales invalidas -> ApiException('CREDENCIALES_INVALIDAS').
  Future<Sesion> login(String usuario, String clave);
}

class ApiAuthService implements AuthService {
  ApiAuthService(this._api);

  final ApiClient _api;

  @override
  Future<Sesion> login(String usuario, String clave) async {
    final json = await _api.post('/api/v1/auth/login', {'usuario': usuario.trim(), 'clave': clave});
    return Sesion.fromJson(json);
  }
}

/// Sin backend: `flutter run --dart-define=AUTH_FAKE=true`. Usuarios: paciente/paciente123, enfermera/enfermera123.
class FakeAuthService implements AuthService {
  final Map<String, (String, Sesion)> cuentas = {
    'paciente': ('paciente123',
        const Sesion(usuarioId: 1, rol: Rol.paciente, personaId: 1, nombre: 'Juan Carlos Choque Mamani')),
    'enfermera': ('enfermera123',
        const Sesion(usuarioId: 2, rol: Rol.enfermeria, personaId: 2, nombre: 'Marta Limachi')),
  };

  @override
  Future<Sesion> login(String usuario, String clave) async {
    final cuenta = cuentas[usuario.trim().toLowerCase()];
    if (cuenta == null || cuenta.$1 != clave) {
      throw const ApiException('CREDENCIALES_INVALIDAS', 'Usuario o contraseña incorrectos', estado: 401);
    }
    return cuenta.$2;
  }
}
