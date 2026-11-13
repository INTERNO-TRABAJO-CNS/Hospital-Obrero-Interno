import 'package:flutter/foundation.dart';

/// `notifyListeners` que no falla si la vista ya cerro el controlador (por ejemplo, la sesion
/// vencio a mitad de una carga y la app volvio al login).
mixin AvisoSeguro on ChangeNotifier {
  bool _cerrado = false;

  void notificar() {
    if (!_cerrado) {
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _cerrado = true;
    super.dispose();
  }
}
