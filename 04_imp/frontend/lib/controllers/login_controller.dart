import 'package:flutter/foundation.dart';

import '../models/sesion_dto.dart';
import '../services/api_client.dart';
import '../services/auth_api.dart';

class LoginController extends ChangeNotifier {
  LoginController(this._authApi);

  SesionDto? sesion;
  final AuthApi _authApi;
  bool isLoading = false;
  String? errorMessage;

  Future<void> login(String usuario, String clave) async {
    errorMessage = null;
    sesion = null;
    if (usuario.trim().isEmpty || clave.isEmpty) {
      errorMessage = 'Completa usuario y clave.';
      notifyListeners();
      return;
    }

    isLoading = true;
    notifyListeners();
    try {
      sesion = await _authApi.login(usuario.trim(), clave);
    } on ApiException catch (exception) {
      errorMessage = exception.error.mensaje;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}