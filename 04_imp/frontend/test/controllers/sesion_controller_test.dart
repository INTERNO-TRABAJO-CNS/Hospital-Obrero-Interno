import 'package:cns_app/controllers/sesion_controller.dart';
import 'package:cns_app/core/api_client.dart';
import 'package:cns_app/models/auth/paciente.dart';
import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/services/auth_service.dart';
import 'package:cns_app/services/paciente_service.dart';
import 'package:flutter_test/flutter_test.dart';

/// Agrega al FakeAuthService la cuenta creada por el registro, como lo hace el backend.
class _AuthQueAprende extends FakeAuthService {
  void agregar(String usuario, String clave, Sesion sesion) => cuentas[usuario] = (clave, sesion);
}

void main() {
  late ApiClient api;
  late _AuthQueAprende auth;
  late FakePacienteService pacientes;
  late SesionController controller;

  setUp(() {
    api = ApiClient(urlBase: 'http://x');
    auth = _AuthQueAprende();
    pacientes = FakePacienteService();
    controller = SesionController(auth: auth, pacientes: pacientes, api: api);
  });

  test('login correcto pone los headers temporales del usuario', () async {
    expect(await controller.entrar('paciente', 'paciente123'), isNull);

    expect(controller.sesion!.rol, Rol.paciente);
    expect(api.rol, 'PACIENTE');
    expect(api.personaId, 1);
  });

  test('[2a] credenciales invalidas devuelven el mensaje del backend', () async {
    expect(await controller.entrar('paciente', 'mala'), 'Usuario o contraseña incorrectos');
    expect(controller.autenticado, isFalse);
  });

  test('registrar titular con afiliado inexistente no crea nada', () async {
    final error = await controller.registrarTitular(
        const RegistroTitular(numeroAfiliado: 'AF-00000', ci: '1', usuario: 'x', clave: 'secreta1'));

    expect(error, 'El número de afiliado no está registrado en la CNS');
    expect(pacientes.pacientes, isEmpty);
  });

  test('registrar titular y entrar con su cuenta', () async {
    auth.agregar('juanc', 'secreta1',
        const Sesion(usuarioId: 9, rol: Rol.paciente, personaId: 1, nombre: 'Juan Carlos Choque'));

    final error = await controller.registrarTitular(
        const RegistroTitular(numeroAfiliado: 'AF-10001', ci: '4512301', usuario: 'juanc', clave: 'secreta1'));

    expect(error, isNull);
    expect(controller.numeroAfiliado, 'AF-10001');
    expect(controller.sesion!.personaId, 1);
  });

  test('modo sin backend: la cuenta creada en el registro sirve para entrar', () async {
    final authFake = FakeAuthService();
    final c = SesionController(
        auth: authFake, pacientes: FakePacienteService(alRegistrarTitular: authFake.agregarCuentaPaciente));

    final error = await c.registrarTitular(
        const RegistroTitular(numeroAfiliado: 'AF-10001', ci: '4512301', usuario: 'Benjamin', clave: 'secreta123'));

    expect(error, isNull);
    expect(c.sesion!.nombre, 'Juan Carlos Choque Mamani');
    c.salir();
    expect(await c.entrar('benjamin', 'secreta123'), isNull);
  });

  test('contrasena corta se rechaza sin llamar al backend', () async {
    final error = await controller.registrarTitular(
        const RegistroTitular(numeroAfiliado: 'AF-10001', ci: '4512301', usuario: 'juanc', clave: '123'));

    expect(error, 'La contraseña debe tener al menos 6 caracteres.');
    expect(pacientes.pacientes, isEmpty);
  });

  test('beneficiario con fecha futura se rechaza', () async {
    final error = await controller.registrarBeneficiario(RegistroBeneficiario(
      numeroAfiliadoTitular: 'AF-10001',
      ci: '9000001',
      nombres: 'Sofía',
      apellidos: 'Choque',
      fechaNacimiento: DateTime.now().add(const Duration(days: 2)),
    ));

    expect(error, 'La fecha de nacimiento no puede ser futura.');
  });

  test('salir borra la sesion y los headers', () async {
    await controller.entrar('paciente', 'paciente123');

    controller.salir();

    expect(controller.autenticado, isFalse);
    expect(api.rol, isNull);
  });
}
