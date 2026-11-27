import 'package:flutter/material.dart';

import 'core/app_router.dart';
import 'core/app_theme.dart';

void main() {
  runApp(const CnsApp());
}

class CnsApp extends StatelessWidget {
  const CnsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CNS Hospital Obrero',
      debugShowCheckedModeBanner: false,
      navigatorKey: navegadorApp,
      scaffoldMessengerKey: mensajesApp,
      theme: AppTheme.claro(),
      // El Figma no tiene modo oscuro: siempre el tema claro, aunque el sistema este en modo oscuro.
      themeMode: ThemeMode.light,
      initialRoute: AppRutas.login,
      onGenerateRoute: generarRuta,
    );
  }
}
