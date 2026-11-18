import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../controllers/cuentas_controller.dart';
import '../../controllers/quirofanos_controller.dart';
import '../../services/catalogo_service.dart';
import '../../services/cuentas_service.dart';
import '../../services/quirofano_service.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_consultorios_view.dart';
import 'catalogo_cuentas_view.dart';
import 'catalogo_dias_view.dart';
import 'catalogo_horarios_view.dart';
import 'catalogo_medicos_view.dart';
import 'catalogo_quirofanos_view.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Pantalla "Catálogos" del administrador. El titulo y la pildora los pone el marco;
/// aqui van los chips de seccion y el contenido de la seccion elegida.
class CatalogosView extends StatefulWidget {
  const CatalogosView({super.key, required this.servicio, this.quirofanos, this.cuentas});

  final CatalogoService servicio;

  /// Sin servicio, la seccion no aparece.
  final QuirofanoService? quirofanos;
  final CuentasService? cuentas;

  @override
  State<CatalogosView> createState() => _CatalogosViewState();
}

class _CatalogosViewState extends State<CatalogosView> {
  late final CatalogoController _c = CatalogoController(widget.servicio);
  late final QuirofanosController? _quirofanos =
      widget.quirofanos == null ? null : QuirofanosController(widget.quirofanos!);
  late final CuentasController? _cuentas = widget.cuentas == null ? null : CuentasController(widget.cuentas!);

  @override
  void initState() {
    super.initState();
    _c.cargar();
    _quirofanos?.cargar();
    _cuentas?.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    _quirofanos?.dispose();
    _cuentas?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          FilaOpciones<SeccionCatalogo>(
            opciones: [
              (SeccionCatalogo.medicos, 'Médicos'),
              (SeccionCatalogo.consultorios, 'Consultorios'),
              (SeccionCatalogo.horarios, 'Horarios y días no hábiles'),
              if (_quirofanos != null) (SeccionCatalogo.quirofanos, 'Quirófanos'),
              // Pacientes queda para otra tarea (T100-01): se muestra deshabilitado.
              (SeccionCatalogo.pacientes, 'Pacientes'),
              if (_cuentas != null) (SeccionCatalogo.cuentas, 'Cuentas'),
            ],
            elegida: _c.seccion,
            alElegir: _c.cambiarSeccion,
            deshabilitadas: const {SeccionCatalogo.pacientes},
          ),
          const SizedBox(height: 18),
          if (_c.cargando) const Cargando() else if (_c.errorCarga != null) _errorCarga() else _seccion(),
        ],
      ),
    );
  }

  Widget _errorCarga() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        MensajeError(_c.errorCarga),
        OutlinedButton.icon(onPressed: _c.cargar, icon: const Icon(Icons.refresh), label: const Text('Reintentar')),
      ],
    );
  }

  Widget _seccion() {
    switch (_c.seccion) {
      case SeccionCatalogo.medicos:
        return CatalogoMedicosView(controller: _c);
      case SeccionCatalogo.consultorios:
        return CatalogoConsultoriosView(controller: _c);
      case SeccionCatalogo.horarios:
        return LayoutBuilder(builder: (context, r) {
          final horarios = CatalogoHorariosView(controller: _c);
          final dias = CatalogoDiasView(controller: _c);
          if (r.maxWidth < anchoMinimoDosColumnas) {
            return Column(children: [horarios, const SizedBox(height: 24), dias]);
          }
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(child: horarios),
              const SizedBox(width: 20),
              Expanded(child: dias),
            ],
          );
        });
      case SeccionCatalogo.quirofanos:
        return CatalogoQuirofanosView(controller: _quirofanos!);
      case SeccionCatalogo.cuentas:
        return CatalogoCuentasView(controller: _cuentas!, catalogo: _c);
      case SeccionCatalogo.pacientes:
        return const SizedBox.shrink();
    }
  }
}
