import '../core/api_client.dart';
import '../models/auth/paciente.dart';

/// CU-03: registrar pacientes (titular crea su cuenta; beneficiario con la afiliacion del titular).
abstract class PacienteService {
  /// `POST /api/v1/pacientes/titular`. Errores: AFILIADO_NO_ENCONTRADO, DATOS_INVALIDOS, YA_REGISTRADO, USUARIO_DUPLICADO.
  Future<Paciente> registrarTitular(RegistroTitular datos);

  /// `POST /api/v1/pacientes/beneficiario` (con la sesion del titular o de enfermeria).
  /// Errores: AFILIADO_NO_ENCONTRADO, DATOS_INVALIDOS, YA_REGISTRADO.
  Future<Paciente> registrarBeneficiario(RegistroBeneficiario datos);

  /// `GET /api/v1/pacientes?afiliado=&ci=` (enfermeria o administrador): titular y beneficiarios de la
  /// afiliacion, o solo el de ese CI. Lista vacia si no hay ninguno.
  Future<List<Paciente>> buscar(String afiliado, {String? ci});
}

class ApiPacienteService implements PacienteService {
  ApiPacienteService(this._api);

  final ApiClient _api;

  @override
  Future<Paciente> registrarTitular(RegistroTitular datos) async =>
      Paciente.fromJson(await _api.post('/api/v1/pacientes/titular', datos.toJson()));

  @override
  Future<Paciente> registrarBeneficiario(RegistroBeneficiario datos) async =>
      Paciente.fromJson(await _api.post('/api/v1/pacientes/beneficiario', datos.toJson()));

  @override
  Future<List<Paciente>> buscar(String afiliado, {String? ci}) async {
    final json = await _api.get('/api/v1/pacientes', consulta: {
      'afiliado': afiliado.trim(),
      if (ci != null && ci.trim().isNotEmpty) 'ci': ci.trim(),
    });
    return leerRespuesta(() {
      final datos = json['datos'];
      if (datos is! List) {
        throw const FormatException('se esperaba una lista de pacientes');
      }
      return [for (final p in datos) Paciente.fromJson(Map<String, dynamic>.from(p as Map))];
    });
  }
}

/// En memoria, con las mismas respuestas de error que el backend.
class FakePacienteService implements PacienteService {
  FakePacienteService({this.alRegistrarTitular, bool conDatosDeEjemplo = false}) {
    if (conDatosDeEjemplo) {
      pacientes.addAll([
        Paciente(
            pacienteId: 1,
            numeroAfiliado: 'AF-10001',
            ci: '4512301',
            nombres: 'Juan Carlos',
            apellidos: 'Choque Mamani',
            fechaNacimiento: DateTime(1985, 3, 14),
            titular: true),
        Paciente(
            pacienteId: 2,
            numeroAfiliado: 'AF-10001',
            ci: '9988776',
            nombres: 'Lucía',
            apellidos: 'Choque Rojas',
            fechaNacimiento: DateTime(2019, 6, 2),
            titular: false),
      ]);
    }
  }

  /// Avisa la cuenta creada (usuario, clave, pacienteId, nombre), para que el login falso la conozca.
  final void Function(String usuario, String clave, int pacienteId, String nombre)? alRegistrarTitular;

  /// Tabla interna de afiliados: numero -> (CI, nombres, apellidos, nacimiento).
  final Map<String, (String, String, String, DateTime)> afiliados = {
    'AF-10001': ('4512301', 'Juan Carlos', 'Choque Mamani', DateTime(1985, 3, 14)),
    'AF-10002': ('5123402', 'Ana María', 'Quispe Flores', DateTime(1990, 7, 22)),
  };

  final List<Paciente> pacientes = [];

  @override
  Future<Paciente> registrarTitular(RegistroTitular d) async {
    final afiliado = afiliados[d.numeroAfiliado.trim()];
    if (afiliado == null) {
      throw const ApiException('AFILIADO_NO_ENCONTRADO', 'El número de afiliado no está registrado en la CNS',
          estado: 404);
    }
    if (afiliado.$1 != d.ci.trim()) {
      throw const ApiException('DATOS_INVALIDOS', 'El CI no corresponde al titular de esa afiliación', estado: 400);
    }
    if (pacientes.any((p) => p.titular && p.numeroAfiliado == d.numeroAfiliado.trim())) {
      throw const ApiException('YA_REGISTRADO', 'El paciente ya está registrado', estado: 409);
    }
    final p = Paciente(
      pacienteId: pacientes.length + 1,
      numeroAfiliado: d.numeroAfiliado.trim(),
      ci: d.ci.trim(),
      nombres: afiliado.$2,
      apellidos: afiliado.$3,
      fechaNacimiento: afiliado.$4,
      titular: true,
    );
    pacientes.add(p);
    alRegistrarTitular?.call(d.usuario, d.clave, p.pacienteId, p.nombreCompleto);
    return p;
  }

  @override
  Future<Paciente> registrarBeneficiario(RegistroBeneficiario d) async {
    if (!afiliados.containsKey(d.numeroAfiliadoTitular.trim())) {
      throw const ApiException('AFILIADO_NO_ENCONTRADO', 'La afiliación del titular no existe en la CNS', estado: 404);
    }
    if (pacientes.any((p) => p.numeroAfiliado == d.numeroAfiliadoTitular.trim() && p.ci == d.ci.trim())) {
      throw const ApiException('YA_REGISTRADO', 'El paciente ya está registrado', estado: 409);
    }
    final p = Paciente(
      pacienteId: pacientes.length + 1,
      numeroAfiliado: d.numeroAfiliadoTitular.trim(),
      ci: d.ci.trim(),
      nombres: d.nombres.trim(),
      apellidos: d.apellidos.trim(),
      fechaNacimiento: d.fechaNacimiento,
      titular: false,
    );
    pacientes.add(p);
    return p;
  }

  @override
  Future<List<Paciente>> buscar(String afiliado, {String? ci}) async {
    if (afiliado.trim().isEmpty) {
      throw const ApiException('DATOS_INVALIDOS', 'Indique el numero de afiliado', estado: 400);
    }
    final buscado = ci?.trim() ?? '';
    return [
      for (final p in pacientes)
        if (p.numeroAfiliado == afiliado.trim() && (buscado.isEmpty || p.ci == buscado)) p
    ]..sort((a, b) => (a.titular == b.titular) ? 0 : (a.titular ? -1 : 1));
  }
}
