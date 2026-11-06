import 'package:flutter/material.dart';

import '../views/shell/app_shell.dart';
import 'responsive.dart';

/// Nombres de las rutas de la app.
class AppRutas {
  const AppRutas._();

  static const String login = '/login';
  static const String paciente = '/paciente';
  static const String medico = '/medico';
  static const String enfermeria = '/enfermeria';
  static const String admin = '/admin';
  static const String tv = rutaTv;
}

/// Pantalla provisional. Cada dueno de caso de uso la reemplaza por su pantalla real.
class PantallaMarcador extends StatelessWidget {
  const PantallaMarcador({super.key, required this.titulo});

  final String titulo;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(titulo, style: Theme.of(context).textTheme.headlineMedium),
    );
  }
}

/// Se usa en `MaterialApp.onGenerateRoute`.
Route<dynamic> generarRuta(RouteSettings ajustes) {
  final nombre = ajustes.name ?? AppRutas.login;

  final Widget pagina;
  switch (nombre) {
    case AppRutas.paciente:
      pagina = _conShell(nombre, 'Paciente');
    case AppRutas.medico:
      pagina = _conShell(nombre, 'Medico');
    case AppRutas.enfermeria:
      pagina = _conShell(nombre, 'Enfermeria');
    case AppRutas.admin:
      pagina = _conShell(nombre, 'Administrador');
    case AppRutas.tv:
      pagina = const AppShell(
        rutaActual: AppRutas.tv,
        destinos: [],
        child: PantallaMarcador(titulo: 'Fichas del dia'),
      );
    default:
      pagina = const PantallaMarcador(titulo: 'Iniciar sesion');
  }

  return MaterialPageRoute<void>(settings: ajustes, builder: (_) => pagina);
}

/// Marco con el menu minimo. Al agregar pantallas, agregar sus destinos aqui.
Widget _conShell(String ruta, String titulo) {
  return AppShell(
    rutaActual: ruta,
    destinos: [
      DestinoShell(etiqueta: 'Inicio', icono: Icons.home_outlined, ruta: ruta),
      const DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.login),
    ],
    child: PantallaMarcador(titulo: titulo),
  );
}
