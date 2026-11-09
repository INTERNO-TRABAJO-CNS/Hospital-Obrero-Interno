import 'package:flutter/material.dart';

import '../services/catalogo_service.dart';
import '../services/fake_catalogo_service.dart';
import '../views/admin/catalogos_view.dart';
import '../views/shell/app_shell.dart';
import 'api_client.dart';
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
      pagina = AppShell(
        rutaActual: nombre,
        destinos: const [
          DestinoShell(etiqueta: 'Catálogos', icono: Icons.inventory_2_outlined, ruta: AppRutas.admin),
          DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.login),
        ],
        child: CatalogosView(servicio: _servicioCatalogo()),
      );
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

/// CU-02: catalogos contra el backend, o en memoria con
/// `flutter run --dart-define=CATALOGO_FAKE=true`.
CatalogoService _servicioCatalogo() {
  const usarFake = bool.fromEnvironment('CATALOGO_FAKE');
  if (usarFake) {
    return FakeCatalogoService(conDatosDeEjemplo: true);
  }
  // TEMPORAL hasta el login real (T100-05): el backend lee el rol de estos headers.
  final api = ApiClient()
    ..usuarioId = 1
    ..rol = 'ADMINISTRADOR'
    ..personaId = 1;
  return ApiCatalogoService(api);
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
