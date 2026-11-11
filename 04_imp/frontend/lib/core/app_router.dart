import 'package:flutter/material.dart';

import '../controllers/sesion_controller.dart';
import '../models/auth/sesion.dart';
import '../services/auth_service.dart';
import '../services/catalogo_service.dart';
import '../services/fake_catalogo_service.dart';
import '../services/paciente_service.dart';
import '../views/admin/catalogos_view.dart';
import '../views/auth/login_view.dart';
import '../views/auth/registro_titular_view.dart';
import '../views/paciente/home_paciente_view.dart';
import '../views/paciente/registro_beneficiario_view.dart';
import '../views/shell/app_shell.dart';
import 'api_client.dart';
import 'responsive.dart';

/// Nombres de las rutas de la app.
class AppRutas {
  const AppRutas._();

  static const String login = '/login';
  static const String salir = '/salir';
  static const String registro = '/registro';
  static const String paciente = '/paciente';
  static const String beneficiario = '/paciente/beneficiario';
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
    case AppRutas.salir:
      sesionApp.salir();
      pagina = _login();
    case AppRutas.registro:
      pagina = Builder(
        builder: (context) => RegistroTitularView(
          controller: sesionApp,
          alRegistrar: (sesion) => _irSegunRol(context, sesion),
          alVolver: () => Navigator.of(context).pushReplacementNamed(AppRutas.login),
        ),
      );
    case AppRutas.paciente:
      final sesion = sesionApp.sesion;
      pagina = sesion == null || sesion.rol != Rol.paciente
          ? _login()
          : Builder(
              builder: (context) => AppShell(
                rutaActual: nombre,
                destinos: const [
                  DestinoShell(etiqueta: 'Inicio', icono: Icons.home_outlined, ruta: AppRutas.paciente),
                  DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.salir),
                ],
                child: HomePacienteView(
                  sesion: sesion,
                  alSolicitarCita: () => _pronto(context, 'Solicitar cita'), // T100-06
                  alMisCitas: () => _pronto(context, 'Mis citas'), // T100-07
                  alRegistrarBeneficiario: () => Navigator.of(context).pushNamed(AppRutas.beneficiario),
                ),
              ),
            );
    case AppRutas.beneficiario:
      pagina = Builder(
        builder: (context) => RegistroBeneficiarioView(
          controller: sesionApp,
          alTerminar: () => Navigator.of(context).maybePop(),
        ),
      );
    case AppRutas.medico:
      pagina = _conShell(nombre, 'Medico');
    case AppRutas.enfermeria:
      pagina = _conShell(nombre, 'Enfermeria');
    case AppRutas.admin:
      pagina = AppShell(
        rutaActual: nombre,
        destinos: const [
          DestinoShell(etiqueta: 'Catálogos', icono: Icons.inventory_2_outlined, ruta: AppRutas.admin),
          DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.salir),
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
      pagina = _login();
  }

  return MaterialPageRoute<void>(settings: ajustes, builder: (_) => pagina);
}

/// Cliente HTTP compartido: al iniciar sesion lleva los headers temporales del usuario.
final ApiClient apiApp = ApiClient();

/// CU-01/CU-03 contra el backend, o en memoria con `flutter run --dart-define=AUTH_FAKE=true`.
final SesionController sesionApp = _crearSesion();

SesionController _crearSesion() {
  const usarFake = bool.fromEnvironment('AUTH_FAKE');
  if (usarFake) {
    final auth = FakeAuthService();
    // Como en el backend: la cuenta creada en el registro sirve para iniciar sesion.
    final pacientes = FakePacienteService(alRegistrarTitular: auth.agregarCuentaPaciente);
    return SesionController(auth: auth, pacientes: pacientes, api: apiApp);
  }
  return SesionController(auth: ApiAuthService(apiApp), pacientes: ApiPacienteService(apiApp), api: apiApp);
}

Widget _login() {
  return Builder(
    builder: (context) => LoginView(
      controller: sesionApp,
      alEntrar: (sesion) => _irSegunRol(context, sesion),
      alRegistrarse: () => Navigator.of(context).pushReplacementNamed(AppRutas.registro),
    ),
  );
}

/// Cada rol a su pantalla. Los medicos usan la web Nuxt (decision del 10/11/2026).
void _irSegunRol(BuildContext context, Sesion sesion) {
  switch (sesion.rol) {
    case Rol.paciente:
      Navigator.of(context).pushReplacementNamed(AppRutas.paciente);
    case Rol.enfermeria:
      Navigator.of(context).pushReplacementNamed(AppRutas.enfermeria);
    case Rol.administrador:
      Navigator.of(context).pushReplacementNamed(AppRutas.admin);
    case Rol.medico:
      sesionApp.salir();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Los médicos usan la web del médico (agenda, atención e historial).'),
      ));
  }
}

void _pronto(BuildContext context, String pantalla) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$pantalla estará disponible pronto.')));
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
      const DestinoShell(etiqueta: 'Salir', icono: Icons.logout, ruta: AppRutas.salir),
    ],
    child: PantallaMarcador(titulo: titulo),
  );
}
