import 'package:flutter/material.dart';

import 'controllers/login_controller.dart';
import 'models/sesion_dto.dart';
import 'services/api_client.dart';
import 'services/auth_api.dart';
import 'views/login_page.dart';
import 'views/menu_page.dart';

void main() {
  final authApi = RestAuthApi(ApiClient());
  runApp(CnsApp(authApi: authApi));
}

class CnsApp extends StatelessWidget {
  const CnsApp({super.key, required this.authApi});

  final AuthApi authApi;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CNS',
      theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal)),
      home: LoginPage(
        controller: LoginController(authApi),
        onAuthenticated: (sesion) => _openMenu(context, sesion),
      ),
    );
  }

  void _openMenu(BuildContext context, SesionDto sesion) {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => MenuPage(
          sesion: sesion,
          onLogout: () => _openLogin(context),
        ),
      ),
    );
  }

  void _openLogin(BuildContext context) {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => LoginPage(
          controller: LoginController(authApi),
          onAuthenticated: (sesion) => _openMenu(context, sesion),
        ),
      ),
    );
  }
}