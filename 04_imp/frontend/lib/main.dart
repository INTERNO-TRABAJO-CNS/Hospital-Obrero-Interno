import 'package:flutter/material.dart';

import 'controllers/login_controller.dart';
import 'services/api_client.dart';
import 'services/auth_api.dart';
import 'views/login_page.dart';

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
      home: LoginPage(controller: LoginController(authApi)),
    );
  }
}