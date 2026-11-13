import 'package:flutter/material.dart';

/// Colores del Figma "HOSPITAL OBRERO CNS" (los mismos tokens que la web Nuxt).
class ColoresCns {
  const ColoresCns._();

  /// Fondo verde de las pantallas.
  static const Color fondo = Color(0xFF74C070);

  /// Menu lateral, encabezado de tablas y textos destacados.
  static const Color menu = Color(0xFF227E28);

  /// Opcion activa y boton principal.
  static const Color amarillo = Color(0xFFE1EB60);
  static const Color blanco = Color(0xFFFFFFFF);
  static const Color texto = Color(0xFF000000);
  static const Color textoSuave = Color(0xFF6D6161);
  static const Color nota = Color(0xFF2F4F2F);

  /// Cajas de texto y botones grises.
  static const Color entrada = Color(0xFFD9D9D9);
  static const Color filaPar = Color(0xFFECF6EB);
  static const Color filaElegida = Color(0xFFFBF8D0);
  static const Color peligro = Color(0xFFC83C32);
  static const Color fondoError = Color(0xFFFDE3E1);

  // Chips de estado.
  static const Color atendida = Color(0xFFC8D2E1);
  static const Color confirmada = Color(0xFF96D796);
  static const Color programada = Color(0xFFE1EB60);
  static const Color cancelada = Color(0xFFF0A8A0);
  static const Color interna = Color(0xFFBFE3F5);
  static const Color media = Color(0xFFF5C26B);
  static const Color basica = Color(0xFFE6E6E6);
  static const Color limpieza = Color(0xFFB5B5B5);
}

/// Tema claro y oscuro de la app. Las pantallas del personal y el login usan siempre
/// el claro (el Figma no tiene modo oscuro).
class AppTheme {
  const AppTheme._();

  static ThemeData claro() {
    final esquema = ColorScheme.fromSeed(
      seedColor: ColoresCns.menu,
      primary: ColoresCns.menu,
      secondary: ColoresCns.amarillo,
      error: ColoresCns.peligro,
    );
    final redondeo = RoundedRectangleBorder(borderRadius: BorderRadius.circular(8));
    const negrita = TextStyle(fontWeight: FontWeight.w800, fontSize: 14);
    return ThemeData(
      useMaterial3: true,
      colorScheme: esquema,
      // Texto de cajas y desplegables a 14 px, como en el Figma (Material usa 16).
      textTheme: const TextTheme(bodyLarge: TextStyle(fontSize: 14), titleMedium: TextStyle(fontSize: 14)),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: ColoresCns.entrada,
        isDense: true,
        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 13),
        hintStyle: const TextStyle(color: ColoresCns.textoSuave, fontSize: 13),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
        enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: ColoresCns.menu, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: ColoresCns.peligro),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: ColoresCns.amarillo,
          foregroundColor: ColoresCns.texto,
          disabledBackgroundColor: ColoresCns.entrada,
          minimumSize: const Size(0, 44),
          textStyle: negrita,
          shape: redondeo,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: ColoresCns.texto,
          side: const BorderSide(color: ColoresCns.menu),
          minimumSize: const Size(0, 44),
          textStyle: negrita,
          shape: redondeo,
        ),
      ),
      textButtonTheme: TextButtonThemeData(style: TextButton.styleFrom(foregroundColor: ColoresCns.menu)),
      cardTheme: CardThemeData(
        color: ColoresCns.blanco,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: ColoresCns.blanco,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    );
  }

  static ThemeData oscuro() {
    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: ColoresCns.menu, brightness: Brightness.dark),
    );
  }
}
