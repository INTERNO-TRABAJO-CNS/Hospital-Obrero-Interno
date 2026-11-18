import 'package:flutter/material.dart';

import '../controllers/enfermeria_controller.dart';
import '../controllers/medico_controller.dart';
import '../controllers/sesion_controller.dart';
import '../models/auth/sesion.dart';
import '../models/catalogo/especialidad.dart';
import '../models/medico/medico_actual.dart';
import '../services/auth_service.dart';
import '../services/catalogo_service.dart';
import '../services/cirugias_service.dart';
import '../services/cuentas_service.dart';
import '../services/enfermeria_service.dart';
import '../services/fake_catalogo_service.dart';
import '../services/fake_medico_service.dart';
import '../services/fichas_internas_service.dart';
import '../services/historial_cambios_service.dart';
import '../services/medico_service.dart';
import '../services/paciente_service.dart';
import '../services/quirofano_service.dart';
import '../services/reportes_service.dart';
import '../services/supervision_service.dart';
import '../views/admin/catalogos_view.dart';
import '../views/admin/reportes_view.dart';
import '../views/admin/supervision_view.dart';
import '../views/auth/login_view.dart';
import '../views/auth/registro_titular_view.dart';
import '../views/comun/historial_cambios_view.dart';
import '../views/comun/marco_personal.dart';
import '../views/enfermeria/confirmar_asistencia_view.dart';
import '../views/enfermeria/fichas_dia_view.dart';
import '../views/medico/agenda_medico_view.dart';
import '../views/medico/atencion_view.dart';
import '../views/medico/cirugias_view.dart';
import '../views/medico/cola_prioridad_view.dart';
import '../views/medico/fichas_internas_view.dart';
import '../views/medico/historial_paciente_view.dart';
import '../views/medico/ticket_interno_view.dart';
import '../views/paciente/home_paciente_view.dart';
import '../views/paciente/registro_beneficiario_view.dart';
import '../views/shell/app_shell.dart';
import 'api_client.dart';
import 'responsive.dart';
import 'rutas_medico.dart';

/// Nombres de las rutas de la app.
class AppRutas {
  const AppRutas._();

  static const String login = '/login';
  static const String salir = '/salir';
  static const String registro = '/registro';
  static const String paciente = '/paciente';
  static const String beneficiario = '/paciente/beneficiario';

  /// Médico: lleva a la primera opción de su menú (ver [RutasMedico]).
  static const String medico = RutasMedico.prefijo;

  /// Enfermeria: "Fichas del día" es su inicio.
  static const String enfermeria = '/enfermeria';
  static const String enfermeriaConfirmar = '/enfermeria/confirmar';
  static const String enfermeriaHistorial = '/enfermeria/historial';

  /// Administrador: "Catálogos" es su inicio.
  static const String admin = '/admin';
  static const String adminSupervision = '/admin/supervision';
  static const String adminReportes = '/admin/reportes';
  static const String adminHistorial = '/admin/historial';
  static const String tv = rutaTv;
}

/// Menu lateral de enfermeria (Figma "ENFERMERIA ...").
const List<OpcionMenu> menuEnfermeria = [
  OpcionMenu('Fichas del día', AppRutas.enfermeria),
  OpcionMenu('Confirmar asistencia', AppRutas.enfermeriaConfirmar),
  OpcionMenu('Historial de cambios', AppRutas.enfermeriaHistorial),
];

/// Menu lateral del administrador (Figma "ADMIN ...").
const List<OpcionMenu> menuAdmin = [
  OpcionMenu('Catálogos', AppRutas.admin),
  OpcionMenu('Supervisión', AppRutas.adminSupervision),
  OpcionMenu('Reportes', AppRutas.adminReportes),
  OpcionMenu('Historial de cambios', AppRutas.adminHistorial),
];

/// Menu lateral del medico segun su tipo (Figma "WEB MEDICO" y "WEB ESPECIALISTA"):
/// Medico General y Pediatria: Agenda, Atencion, Historial, Ticket interno y Cola de prioridad.
/// Odontologia: sin ticket interno ni cola (no deriva). Especialistas: agenda de fichas internas y,
/// si su especialidad opera, Cirugias.
List<OpcionMenu> menuMedico(MedicoActual medico) {
  if (!medico.especialidad.esBasica) {
    return [
      const OpcionMenu('Agenda de fichas internas', RutasMedico.fichasInternas),
      if (medico.operaEnQuirofano && medico.especialidad != Especialidad.terapiaIntensiva)
        const OpcionMenu('Cirugías', RutasMedico.cirugias),
    ];
  }
  return [
    const OpcionMenu('Agenda', RutasMedico.agenda),
    const OpcionMenu('Atención', RutasMedico.atencion),
    const OpcionMenu('Historial', RutasMedico.historial),
    if (medico.puedeDerivar) ...[
      const OpcionMenu('Ticket interno', RutasMedico.ticketInterno),
      const OpcionMenu('Cola de prioridad', RutasMedico.cola),
    ],
  ];
}

/// Pantalla provisional. Cada dueno de caso de uso la reemplaza por su pantalla real.
class PantallaMarcador extends StatelessWidget {
  const PantallaMarcador({super.key, required this.titulo});

  final String titulo;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(titulo, textAlign: TextAlign.center, style: Theme.of(context).textTheme.headlineSmall),
      ),
    );
  }
}

/// Navegador y mensajes de la app (para volver al login cuando la sesion vence).
final GlobalKey<NavigatorState> navegadorApp = GlobalKey<NavigatorState>();
final GlobalKey<ScaffoldMessengerState> mensajesApp = GlobalKey<ScaffoldMessengerState>();

/// Se usa en `MaterialApp.onGenerateRoute`.
Route<dynamic> generarRuta(RouteSettings ajustes) {
  final nombre = ajustes.name ?? AppRutas.login;
  final pagina = _pagina(nombre);
  if (nombre.startsWith(AppRutas.enfermeria) ||
      nombre.startsWith(AppRutas.admin) ||
      nombre.startsWith(RutasMedico.prefijo)) {
    // El menu lateral cambia de pantalla sin animacion, como una pagina web.
    return PageRouteBuilder<void>(
      settings: ajustes,
      pageBuilder: (_, __, ___) => pagina,
      transitionDuration: Duration.zero,
      reverseTransitionDuration: Duration.zero,
    );
  }
  return MaterialPageRoute<void>(settings: ajustes, builder: (_) => pagina);
}

Widget _pagina(String nombre) {
  switch (nombre) {
    case AppRutas.salir:
      sesionApp.salir();
      _olvidarMedico();
      return _login();
    case AppRutas.registro:
      return Builder(
        builder: (context) => RegistroTitularView(
          controller: sesionApp,
          alRegistrar: (sesion) => _irSegunRol(context, sesion),
          alVolver: () => Navigator.of(context).pushReplacementNamed(AppRutas.login),
        ),
      );
    case AppRutas.paciente:
      return _conRol(
          {Rol.paciente},
          (sesion) => Builder(
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
              ));
    case AppRutas.beneficiario:
      return _conRol(
          {Rol.paciente},
          (_) => Builder(
                builder: (context) => RegistroBeneficiarioView(
                  controller: sesionApp,
                  alTerminar: () => Navigator.of(context).maybePop(),
                ),
              ));
    case AppRutas.enfermeria:
      return _personal(Rol.enfermeria, nombre, 'Fichas del día', menuEnfermeria,
          (s) => FichasDiaView(servicio: enfermeriaApp, usuario: _usuarioHistorial(s)));
    case AppRutas.enfermeriaConfirmar:
      return _personal(Rol.enfermeria, nombre, 'Confirmar asistencia', menuEnfermeria,
          (s) => ConfirmarAsistenciaView(servicio: enfermeriaApp, usuario: _usuarioHistorial(s)));
    case AppRutas.enfermeriaHistorial:
      return _personal(Rol.enfermeria, nombre, 'Historial de cambios', menuEnfermeria,
          (_) => HistorialCambiosView(servicio: historialApp));
    case AppRutas.admin:
      return _personal(Rol.administrador, nombre, 'Catálogos', menuAdmin,
          (_) => CatalogosView(servicio: catalogoApp, quirofanos: quirofanosApp, cuentas: cuentasApp));
    case AppRutas.adminSupervision:
      return _personal(Rol.administrador, nombre, 'Supervisión en tiempo real', menuAdmin,
          (_) => SupervisionView(servicio: supervisionApp));
    case AppRutas.adminReportes:
      return _personal(
          Rol.administrador, nombre, 'Reportes e indicadores', menuAdmin, (_) => ReportesView(servicio: reportesApp));
    case AppRutas.adminHistorial:
      return _personal(Rol.administrador, nombre, 'Historial de cambios', menuAdmin,
          (_) => HistorialCambiosView(servicio: historialApp));
    case AppRutas.tv:
      return const AppShell(
        rutaActual: AppRutas.tv,
        destinos: [],
        child: PantallaMarcador(titulo: 'Fichas del dia'),
      );
    default:
      if (nombre.startsWith(RutasMedico.prefijo)) {
        return _paginaMedico(nombre);
      }
      return _login();
  }
}

/// Pantallas del medico (antes la web Nuxt). Cada pagina solo para el tipo de medico que la tiene
/// en su menu: si entra otro (un medico general escribiendo /medico/cirugias), ve su inicio.
Widget _paginaMedico(String nombre) {
  return _conRol({Rol.medico}, (sesion) {
    final medico = medicoApp.medico;
    if (medico == null) {
      // Sin los datos de /medico/yo (p. ej. se recargo la pagina): se vuelve a entrar.
      return _login();
    }
    final menu = menuMedico(medico);
    final uri = Uri.parse(nombre);
    final camino = uri.path.startsWith('/medico/pacientes/') ? RutasMedico.historial : uri.path;
    final coinciden = [
      for (final op in menu)
        if (camino == op.ruta || camino.startsWith('${op.ruta}/')) op.ruta
    ]..sort((a, b) => b.length - a.length);
    final permitida = coinciden.isNotEmpty;
    final ruta = permitida ? uri.path : menu.first.ruta;
    final rutaMenu = permitida ? coinciden.first : menu.first.ruta;
    final (titulo, contenido) = _vistaMedico(ruta, uri.queryParameters, medico);
    return Builder(
      builder: (context) => MarcoPersonal(
        titulo: titulo,
        etiqueta: medico.etiqueta,
        opciones: menu,
        rutaActual: rutaMenu,
        alSalir: () => _cerrarSesion(context),
        child: contenido,
      ),
    );
  });
}

(String, Widget) _vistaMedico(String ruta, Map<String, String> consulta, MedicoActual medico) {
  final partes = Uri.parse(ruta).pathSegments;
  if (partes.length == 3 && partes[1] == 'atencion') {
    final citaId = int.tryParse(partes[2]);
    if (citaId != null) {
      return (
        'Atención',
        AtencionView(
            key: ValueKey(ruta),
            citaId: citaId,
            servicio: atencionApp,
            historial: historialPacienteApp,
            fichas: fichasInternasApp,
            cirugias: cirugiasApp),
      );
    }
  }
  if (partes.length == 4 && partes[1] == 'pacientes') {
    final pacienteId = int.tryParse(partes[2]);
    if (pacienteId != null) {
      return (
        'Historial del paciente',
        HistorialPacienteView(
            key: ValueKey(ruta),
            pacienteId: pacienteId,
            servicio: historialPacienteApp,
            fichas: fichasInternasApp,
            cirugias: cirugiasApp),
      );
    }
  }
  switch (ruta) {
    case RutasMedico.atencion:
      return ('Atención', AtencionInicioView(agenda: agendaMedicoApp));
    case RutasMedico.historial:
      return ('Historial del paciente', HistorialInicioView(agenda: agendaMedicoApp, servicio: historialPacienteApp));
    case RutasMedico.ticketInterno:
      return (
        'Ticket interno',
        TicketInternoView(
          medico: medico,
          agenda: agendaMedicoApp,
          fichas: fichasInternasApp,
          historial: historialPacienteApp,
          citaId: int.tryParse(consulta['citaId'] ?? ''),
        ),
      );
    case RutasMedico.cola:
      return ('Cola de prioridad', ColaPrioridadView(medico: medico, fichas: fichasInternasApp));
    case RutasMedico.fichasInternas:
      return ('Fichas internas de hoy', FichasInternasView(medico: medico, fichas: fichasInternasApp));
    case RutasMedico.cirugias:
      return ('Cirugías', CirugiasView(medico: medico, servicio: cirugiasApp));
    case RutasMedico.programarCirugia:
      return (
        'Programar cirugía',
        ProgramarCirugiaView(
          medico: medico,
          cirugias: cirugiasApp,
          fichas: fichasInternasApp,
          fichaTicket: int.tryParse(consulta['ficha'] ?? ''),
        ),
      );
    default:
      return ('Agenda', AgendaMedicoView(agenda: agendaMedicoApp, medico: medico));
  }
}

/// Solo con sesion y con uno de [roles]; si no, el login (antes /admin se abria sin sesion).
Widget _conRol(Set<Rol> roles, Widget Function(Sesion sesion) pagina) {
  final sesion = sesionApp.sesion;
  if (sesion == null || !roles.contains(sesion.rol)) {
    return _login();
  }
  return pagina(sesion);
}

/// Pantalla del personal dentro del marco del Figma (menu verde + titulo + pildora).
Widget _personal(Rol rol, String ruta, String titulo, List<OpcionMenu> menu, Widget Function(Sesion s) contenido) {
  return _conRol(
      {rol},
      (sesion) => Builder(
            builder: (context) => MarcoPersonal(
              titulo: titulo,
              etiqueta: sesion.etiqueta,
              opciones: menu,
              rutaActual: ruta,
              alSalir: () => _cerrarSesion(context),
              child: contenido(sesion),
            ),
          ));
}

String _usuarioHistorial(Sesion s) => usuarioParaHistorial(s.rol.etiqueta, s.nombre);

Future<void> _cerrarSesion(BuildContext context) async {
  final navegador = Navigator.of(context);
  await sesionApp.salir();
  _olvidarMedico();
  navegador.pushNamedAndRemoveUntil(AppRutas.login, (_) => false);
}

/// Cliente HTTP compartido: al iniciar sesion lleva el token (Authorization: Bearer).
final ApiClient apiApp = ApiClient()..alPerderSesion = _alPerderSesion;

/// El backend respondio 401 a una peticion con token: la sesion vencio o se cerro.
void _alPerderSesion(ApiException error) {
  if (!sesionApp.autenticado) {
    return;
  }
  sesionApp.olvidar();
  _olvidarMedico();
  navegadorApp.currentState?.pushNamedAndRemoveUntil(AppRutas.login, (_) => false);
  mensajesApp.currentState?.showSnackBar(
    const SnackBar(content: Text('Tu sesión venció o se cerró. Vuelve a iniciar sesión.')),
  );
}

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

/// CU-02: catalogos y cuentas contra el backend con el token de la sesion, o en memoria con
/// `flutter run --dart-define=CATALOGO_FAKE=true`.
const bool _catalogoFake = bool.fromEnvironment('CATALOGO_FAKE');
final CatalogoService catalogoApp =
    _catalogoFake ? FakeCatalogoService(conDatosDeEjemplo: true) : ApiCatalogoService(apiApp);
final CuentasService cuentasApp = _catalogoFake ? FakeCuentasService() : ApiCuentasService(apiApp);

// Sin backend todavia (enfermeria, historial, quirofanos, supervision y reportes): datos de prueba
// en memoria. Comparten estado: confirmar una llegada aparece en el historial y en la supervision.
final FakeHistorialCambiosService historialApp = FakeHistorialCambiosService();
final FakeEnfermeriaService enfermeriaApp = FakeEnfermeriaService(historial: historialApp);

/// Quirofanos (T100-19): en PostgreSQL por el backend, o en memoria con CATALOGO_FAKE.
final QuirofanoService quirofanosApp = _catalogoFake ? FakeQuirofanoService() : ApiQuirofanoService(apiApp);
final SupervisionService supervisionApp = FakeSupervisionService(enfermeria: enfermeriaApp, quirofanos: quirofanosApp);
final ReportesService reportesApp = FakeReportesService();

/// Medico (CU-13/14/15) contra el backend, o en memoria con `--dart-define=AUTH_FAKE=true`
/// (o `MEDICO_FAKE=true`). Sin token, el backend responderia 401: por eso con AUTH_FAKE tambien
/// son de prueba.
const bool _medicoFake = bool.fromEnvironment('AUTH_FAKE') || bool.fromEnvironment('MEDICO_FAKE');
final MedicoService medicoServicioApp =
    _medicoFake ? FakeMedicoService(sesion: () => sesionApp.sesion) : ApiMedicoService(apiApp);
final AtencionService atencionApp = _medicoFake ? FakeAtencionService() : ApiAtencionService(apiApp);
final HistorialPacienteService historialPacienteApp =
    _medicoFake ? FakeHistorialPacienteService() : ApiHistorialPacienteService(apiApp);

// Fichas internas (CU-21..CU-23, CU-25): en PostgreSQL por el backend (T100-20..22), o en memoria con
// AUTH_FAKE / MEDICO_FAKE.
final FichasInternasService fichasInternasApp =
    _medicoFake ? FakeFichasInternasService() : ApiFichasInternasService(apiApp);

// Cirugias (CU-24): en PostgreSQL por el backend (T100-23), o en memoria con AUTH_FAKE / MEDICO_FAKE.
final CirugiasService cirugiasApp = _medicoFake ? FakeCirugiasService(fichasInternasApp) : ApiCirugiasService(apiApp);

/// Datos del medico de la sesion y su agenda (la fecha elegida se recuerda entre pantallas).
final MedicoController medicoApp = MedicoController(medicoServicioApp);
final AgendaMedicoController agendaMedicoApp = AgendaMedicoController(
    _medicoFake ? FakeAgendaMedicoService() : ApiAgendaMedicoService(apiApp),
    fichas: fichasInternasApp);

void _olvidarMedico() {
  medicoApp.olvidar();
  agendaMedicoApp.olvidar();
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

/// Cada rol a su pantalla. El medico primero carga sus datos (`/medico/yo`) para armar su menu.
Future<void> _irSegunRol(BuildContext context, Sesion sesion) async {
  switch (sesion.rol) {
    case Rol.paciente:
      Navigator.of(context).pushReplacementNamed(AppRutas.paciente);
    case Rol.enfermeria:
      Navigator.of(context).pushReplacementNamed(AppRutas.enfermeria);
    case Rol.administrador:
      Navigator.of(context).pushReplacementNamed(AppRutas.admin);
    case Rol.medico:
      final navegador = Navigator.of(context);
      final mensajes = ScaffoldMessenger.of(context);
      final resultado = await medicoApp.cargar();
      if (resultado == CargaMedico.ok) {
        navegador.pushReplacementNamed(menuMedico(medicoApp.medico!).first.ruta);
        return;
      }
      final motivo = medicoApp.error ?? 'No se pudieron cargar tus datos de médico.';
      await sesionApp.salir();
      _olvidarMedico();
      mensajes.showSnackBar(SnackBar(content: Text(motivo)));
  }
}

void _pronto(BuildContext context, String pantalla) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$pantalla estará disponible pronto.')));
}
