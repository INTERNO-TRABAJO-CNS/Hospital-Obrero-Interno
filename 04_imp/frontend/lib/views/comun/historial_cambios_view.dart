import 'package:flutter/material.dart';

import '../../controllers/historial_cambios_controller.dart';
import '../../core/app_theme.dart';
import '../../models/comun/fechas.dart';
import '../../services/historial_cambios_service.dart';
import 'cns_widgets.dart';

/// Figma "ADMIN HISTORIAL DE CAMBIOS" (tambien para enfermeria): quien cambio cada cita, cuando
/// y por que. Solo consulta.
class HistorialCambiosView extends StatefulWidget {
  const HistorialCambiosView({super.key, required this.servicio});

  final HistorialCambiosService servicio;

  @override
  State<HistorialCambiosView> createState() => _HistorialCambiosViewState();
}

class _HistorialCambiosViewState extends State<HistorialCambiosView> {
  late final HistorialCambiosController _c = HistorialCambiosController(widget.servicio);

  @override
  void initState() {
    super.initState();
    _c.buscar();
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  Future<void> _elegirRango() async {
    final hoy = DateTime.now();
    final rango = await showDateRangePicker(
      context: context,
      firstDate: DateTime(hoy.year - 2),
      lastDate: DateTime(hoy.year, hoy.month, hoy.day),
      initialDateRange: _c.desde != null && _c.hasta != null ? DateTimeRange(start: _c.desde!, end: _c.hasta!) : null,
    );
    if (rango != null) {
      _c.elegirRango(rango.start, rango.end);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final rango = _c.desde == null ? 'Desde – Hasta' : '${diaMes(_c.desde!)} – ${diaMes(_c.hasta ?? _c.desde!)}';
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Wrap(
              spacing: 10,
              runSpacing: 10,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                SizedBox(
                  width: 150,
                  child: TextField(
                    key: const Key('filtro_cita'),
                    onChanged: (v) => _c.cita = v,
                    onSubmitted: (_) => _c.buscar(),
                    decoration: decoracionBlanca('Cita (ficha)'),
                  ),
                ),
                SizedBox(
                  width: 170,
                  child: TextField(
                    key: const Key('filtro_usuario'),
                    onChanged: (v) => _c.usuario = v,
                    onSubmitted: (_) => _c.buscar(),
                    decoration: decoracionBlanca('Usuario'),
                  ),
                ),
                Material(
                  color: ColoresCns.blanco,
                  borderRadius: BorderRadius.circular(8),
                  child: InkWell(
                    key: const Key('filtro_rango'),
                    borderRadius: BorderRadius.circular(8),
                    onTap: _elegirRango,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
                      child: Text(rango,
                          style: TextStyle(
                              fontSize: 13, color: _c.desde == null ? ColoresCns.textoSuave : ColoresCns.texto)),
                    ),
                  ),
                ),
                if (_c.desde != null)
                  IconButton(
                    tooltip: 'Quitar fechas',
                    onPressed: () => _c.elegirRango(null, null),
                    icon: const Icon(Icons.close, size: 18),
                  ),
                FilledButton(key: const Key('historial_buscar'), onPressed: _c.buscar, child: const Text('Buscar')),
              ],
            ),
            const SizedBox(height: 16),
            if (_c.error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
              ),
            TablaCns(
              columnas: const [
                ColumnaTabla('Fecha y hora', flex: 2),
                ColumnaTabla('Cita', flex: 2),
                ColumnaTabla('Acción', flex: 3),
                ColumnaTabla('Usuario', flex: 3),
                ColumnaTabla('Motivo', flex: 3),
              ],
              filas: [
                for (final c in _c.cambios)
                  [Text(diaMesHora(c.fecha)), Text(c.cita), Text(c.accion), Text(c.usuario), Text(c.motivo)],
              ],
              textoVacio: _c.cargando ? 'Buscando…' : 'No hay cambios con esos filtros',
            ),
            const NotaPie('Los registros no se modifican ni se borran. Consulta para enfermería y administrador.'),
            const AvisoDatosPrueba(),
          ],
        );
      },
    );
  }
}
