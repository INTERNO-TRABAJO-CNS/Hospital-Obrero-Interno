import 'package:flutter/material.dart';

import '../../controllers/reportes_controller.dart';
import '../../core/app_theme.dart';
import '../../models/reportes/reporte.dart';
import '../../services/reportes_service.dart';
import '../comun/cns_widgets.dart';

/// Figma "ADMIN REPORTES E INDICADORES v2" y "ADMIN REPORTES FICHAS INTERNAS Y QUIROFANOS".
class ReportesView extends StatefulWidget {
  const ReportesView({super.key, required this.servicio});

  final ReportesService servicio;

  @override
  State<ReportesView> createState() => _ReportesViewState();
}

class _ReportesViewState extends State<ReportesView> {
  late final ReportesController _c = ReportesController(widget.servicio);

  @override
  void initState() {
    super.initState();
    _c.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final r = _c.reporte;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilaOpciones<TipoReporte>(
              opciones: [for (final t in TipoReporte.values) (t, t.titulo)],
              elegida: _c.tipo,
              alElegir: _c.elegir,
            ),
            const SizedBox(height: 14),
            if (_c.error != null)
              Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro))
            else if (r == null)
              const Cargando()
            else ...[
              GraficoBarras(titulo: r.titulo, filas: r.filas, maximo: r.maximo),
              const SizedBox(height: 16),
              FilaIndicadores(indicadores: [
                for (final i in r.indicadores) TarjetaIndicador(valor: i.valor, etiqueta: i.etiqueta),
              ]),
              NotaPie(r.nota),
            ],
            const AvisoDatosPrueba(),
          ],
        );
      },
    );
  }
}
