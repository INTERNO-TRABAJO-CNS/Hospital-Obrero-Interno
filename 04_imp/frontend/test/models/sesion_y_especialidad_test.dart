import 'package:cns_app/models/auth/sesion.dart';
import 'package:cns_app/models/catalogo/especialidad.dart';
import 'package:cns_app/models/catalogo/medico.dart';
import 'package:cns_app/models/personal/cuenta.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('Especialidad', () {
    test('13 especialidades: 3 basicas y 10 internas, como TipoEspecialidad del backend', () {
      expect(Especialidad.values, hasLength(13));
      expect(
          Especialidad.basicas, [Especialidad.consultorioFamiliar, Especialidad.odontologia, Especialidad.pediatria]);
      expect(Especialidad.internas, hasLength(10));
    });

    test('rangos del Figma y del backend', () {
      expect(Especialidad.gastroenterologia.rango, '20–30 min');
      expect(Especialidad.terapiaIntensiva.rango, '30–60 min');
      expect(Especialidad.dermatologia.duracionMin, 15);
    });

    test('solo Gastroenterologia, Hematologia y Dermatologia operan', () {
      expect(Especialidad.queOperan,
          [Especialidad.gastroenterologia, Especialidad.hematologia, Especialidad.dermatologia]);
    });

    test('un codigo desconocido no se cambia por otra especialidad', () {
      expect(Especialidad.desdeCodigo('MEDICINA_TRABAJO'), Especialidad.medicinaTrabajo);
      expect(() => Especialidad.desdeCodigo('ONCOLOGIA'), throwsFormatException);
    });

    test('Medico con especialidad interna se lee del JSON', () {
      final m = Medico.desdeJson({
        'medicoId': 4,
        'nombres': 'Raúl',
        'apellidos': 'Vargas',
        'especialidad': 'GASTROENTEROLOGIA',
        'duracionCitaMin': 25,
        'cupoMaximoDia': 6,
        'activo': true,
      });

      expect(m.especialidad.esInterna, isTrue);
    });
  });

  group('Sesion', () {
    Map<String, dynamic> json({String rol = 'ENFERMERIA', String? token = 'tok'}) => {
          'usuarioId': 2,
          'rol': rol,
          'personaId': 2,
          'nombre': 'María Flores',
          'token': token,
          'venceEn': '2026-11-13T18:00:00',
          'numeroAfiliado': null,
        };

    test('lee el token y la etiqueta para la pildora', () {
      final s = Sesion.fromJson(json());

      expect(s.token, 'tok');
      expect(s.venceEn, DateTime(2026, 11, 13, 18));
      expect(s.etiqueta, 'Enfermería · María Flores');
    });

    test('un rol desconocido ya no cae en PACIENTE', () {
      expect(() => Sesion.fromJson(json(rol: 'SUPERADMIN')), throwsFormatException);
    });

    test('una sesion sin token es invalida', () {
      expect(() => Sesion.fromJson(json(token: null)), throwsFormatException);
    });
  });

  group('NuevaCuenta', () {
    const base = NuevaCuenta(usuario: 'mflores', clave: 'segura1', rol: Rol.enfermeria, nombre: 'María Flores');

    test('valida formato como ReglasCuenta del backend', () {
      expect(base.validar(), isNull);
      expect(
          const NuevaCuenta(usuario: 'x', clave: '12345', rol: Rol.enfermeria, nombre: 'X').validar(), contains('6'));
      expect(const NuevaCuenta(usuario: 'x', clave: 'segura1', rol: Rol.medico).validar(),
          'Elija el médico del catálogo.');
      expect(const NuevaCuenta(usuario: 'x', clave: 'segura1', rol: Rol.paciente, nombre: 'X').validar(),
          contains('registro del titular'));
    });

    test('la contrasena no puede pasar de 72 bytes (BCrypt)', () {
      final larga = NuevaCuenta(usuario: 'x', clave: 'ñ' * 37, rol: Rol.enfermeria, nombre: 'X');

      expect(larga.validar(), contains('72'));
    });

    test('para MEDICO viaja el medicoId como personaId', () {
      const c = NuevaCuenta(usuario: 'dr.vargas', clave: 'segura1', rol: Rol.medico, personaId: 4);

      expect(c.aJson(), {'usuario': 'dr.vargas', 'clave': 'segura1', 'rol': 'MEDICO', 'personaId': 4, 'nombre': ''});
    });
  });
}
