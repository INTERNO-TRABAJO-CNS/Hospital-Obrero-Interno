import 'package:cns_app/controllers/fichas_controller.dart';
import 'package:cns_app/models/fichas/tv_ficha_model.dart';
import 'package:cns_app/views/tv/tv_fichas_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

class _FakeFichasController implements FichasController {
  _FakeFichasController(this._fichas);

  final List<TvFichaModel> _fichas;

  @override
  Future<List<TvFichaModel>> obtenerListadoParaTv() async => _fichas;
}

void main() {
  testWidgets('TvFichasView muestra ficha sin datos sensibles', (tester) async {
    final controller = _FakeFichasController([
      TvFichaModel.fromJson({
        'numeroFicha': 12,
        'hora': '10:00',
        'especialidad': 'CARDIOLOGIA',
        'consultorio': 3,
        'estado': 'PROGRAMADA',
      }),
    ]);

    await tester.pumpWidget(
      MaterialApp(
        home: TvFichasView(controller: controller),
      ),
    );

    await tester.pump();
    await tester.pump(const Duration(milliseconds: 100));

    expect(find.text('Ficha: 12'), findsOneWidget);
    expect(
      find.text(
        'Hora: 10:00\n'
        'Especialidad: CARDIOLOGIA\n'
        'Consultorio: 3',
      ),
      findsOneWidget,
    );
    expect(find.text('PROGRAMADA'), findsOneWidget);
  });
}