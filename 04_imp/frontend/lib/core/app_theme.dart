import 'package:flutter/material.dart';

/// Tema claro y oscuro de la app.
class AppTheme {
  const AppTheme._();

  static const Color _colorBase = Color(0xFF0B6E4F);

  static ThemeData claro() => _tema(Brightness.light);

  static ThemeData oscuro() => _tema(Brightness.dark);

  static ThemeData _tema(Brightness brillo) {
    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: _colorBase, brightness: brillo),
    );
  }
}
