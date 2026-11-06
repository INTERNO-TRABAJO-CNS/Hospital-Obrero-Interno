import 'package:cns_app/core/responsive.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('dispositivoDe', () {
    test('599 es celular', () {
      expect(dispositivoDe(599), Dispositivo.celular);
    });

    test('600 es escritorio', () {
      expect(dispositivoDe(600), Dispositivo.escritorio);
    });

    test('1599 sigue siendo escritorio', () {
      expect(dispositivoDe(1599), Dispositivo.escritorio);
    });

    test('1600 es tv', () {
      expect(dispositivoDe(1600), Dispositivo.tv);
    });

    test('la ruta /tv es tv aunque la ventana sea chica', () {
      expect(dispositivoDe(400, ruta: '/tv'), Dispositivo.tv);
    });
  });
}
