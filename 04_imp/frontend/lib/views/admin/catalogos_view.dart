import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../controllers/cuentas_controller.dart';
import '../../controllers/pacientes_admin_controller.dart';
import '../../controllers/quirofanos_controller.dart';
import '../../services/catalogo_service.dart';
import '../../services/cuentas_service.dart';
import '../../services/pacientes_admin_service.dart';
import '../../services/quirofano_service.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_consultorios_view.dart';
import 'catalogo_cuentas_view.dart';
import 'catalogo_dias_view.dart';
import 'catalogo_horarios_view.dart';
import 'catalogo_medicos_view.dart';
import 'catalogo_pacientes_view.dart';
import 'catalogo_quirofanos_view.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Pantallas "Catálogos" y "Gestionar" del administrador. El titulo y la pildora los pone el marco;
/// aqui van los chips de seccion y el contenido de la seccion elegida.
///
/// - "Catálogos" ([editable] = false): solo consulta de las tablas.
/// - "Gestionar" ([editable] = true): las mismas tablas con el panel "Nuevo / editar" para crear, editar y dar
///   de baja médicos, consultorios, horarios, quirófanos, pacientes y cuentas.
class CatalogosView extends StatefulWidget {
  const CatalogosView({
    super.key,
    required this.servicio,
    this.quirofanos,
    this.cuentas,
    this.pacientes,
    this.editable = false,
  });

  final CatalogoService servicio;

  /// Sin servicio, la seccion no aparece.
  final QuirofanoService? quirofanos;
  final CuentasService? cuentas;
  final PacientesAdminService? pacientes;

  final bool editable;

  @override
  State<CatalogosView> createState() => _CatalogosViewState();
}

class _CatalogosViewState extends State<CatalogosView> {
  late final CatalogoController _c = CatalogoController(widget.servicio);
  late final QuirofanosController? _quirofanos =
      widget.quirofanos == null ? null : QuirofanosController(widget.quirofanos!);
  late final CuentasController? _cuentas = widget.cuentas == null ? null : CuentasController(widget.cuentas!);
  late final PacientesAdminController? _pacientes =
      widget.pacientes == null ? null : PacientesAdminController(widget.pacientes!);

  bool get _soloLectura => !widget.editable;

  @override
  void initState() {
    super.initState();
    _c.cargar();
    _quirofanos?.cargar();
    _cuentas?.cargar();
    _pacientes?.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    _quirofanos?.dispose();
    _cuentas?.dispose();
    _pacientes?.dispose();
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
              if (_pacientes != null) (SeccionCatalogo.pacientes, 'Pacientes'),
              if (_cuentas != null) (SeccionCatalogo.cuentas, 'Cuentas'),
            ],
            elegida: _c.seccion,
            alElegir: _c.cambiarSeccion,
          ),
          if (_soloLectura)
            const Padding(
              padding: EdgeInsets.only(top: 8),
              child: NotaPie('Solo consulta. Para crear, editar o dar de baja use "Gestionar" en el menú.'),
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
        return CatalogoMedicosView(controller: _c, soloLectura: _soloLectura);
      case SeccionCatalogo.consultorios:
        return CatalogoConsultoriosView(controller: _c, soloLectura: _soloLectura);
      case SeccionCatalogo.horarios:
        return LayoutBuilder(builder: (context, r) {
          final horarios = CatalogoHorariosView(controller: _c, soloLectura: _soloLectura);
          final dias = CatalogoDiasView(controller: _c, soloLectura: _soloLectura);
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
        return CatalogoQuirofanosView(controller: _quirofanos!, soloLectura: _soloLectura);
      case SeccionCatalogo.cuentas:
        return CatalogoCuentasView(controller: _cuentas!, catalogo: _c, soloLectura: _soloLectura);
      case SeccionCatalogo.pacientes:
        return CatalogoPacientesView(controller: _pacientes!, soloLectura: _soloLectura);
    }
  }
}
