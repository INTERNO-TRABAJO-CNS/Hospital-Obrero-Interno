import '../core/api_client.dart';
import '../models/auth/paciente.dart';

/// CU-03: registrar pacientes (titular crea su cuenta; beneficiario con la afiliacion del titular).
abstract class PacienteService {
  /// `POST /api/v1/pacientes/titular`. Errores: AFILIADO_NO_ENCONTRADO, DATOS_INVALIDOS, YA_REGISTRADO, USUARIO_DUPLICADO.
  Future<Paciente> registrarTitular(RegistroTitular datos);

  /// `POST /api/v1/pacientes/beneficiario` (con la sesion del titular).
  Future<Paciente> registrarBeneficiario(RegistroBeneficiario datos);
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
}

/// En memoria, con las mismas respuestas de error que el backend.
class FakePacienteService implements PacienteService {
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
    return p;
  }

  @override
  Future<Paciente> registrarBeneficiario(RegistroBeneficiario d) async {
    if (!afiliados.containsKey(d.numeroAfiliadoTitular.trim())) {
      throw const ApiException('AFILIADO_NO_ENCONTRADO', 'La afiliación del titular no existe en la CNS',
          estado: 404);
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
}
