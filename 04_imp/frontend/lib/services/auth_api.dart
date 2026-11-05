import '../models/error_dto.dart';
import '../models/sesion_dto.dart';
import 'api_client.dart';

abstract interface class AuthApi {
  Future<SesionDto> login(String usuario, String clave);
}

class RestAuthApi implements AuthApi {
  RestAuthApi(this._client);

  final ApiClient _client;

  @override
  Future<SesionDto> login(String usuario, String clave) async {
    final json = await _client.post(
      '/auth/login',
      {'usuario': usuario, 'clave': clave},
    );
    return SesionDto.fromJson(json);
  }
}

class FakeAuthApi implements AuthApi {
  const FakeAuthApi();

  @override
  Future<SesionDto> login(String usuario, String clave) async {
    if (usuario.trim().isEmpty || clave.isEmpty) {
      throw const ApiException(
        ErrorDto(codigo: 'CREDENCIALES_INVALIDAS', mensaje: 'Completa usuario y clave.'),
      );
    }
    return SesionDto(
      usuarioId: 1,
      nombreUsuario: usuario.trim(),
      rol: 'PACIENTE',
      permisos: const ['CITAS_CONSULTAR'],
    );
  }
}