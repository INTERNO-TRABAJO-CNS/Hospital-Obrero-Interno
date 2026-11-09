import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../services/catalogo_service.dart';
import 'catalogo_consultorios_view.dart';
import 'catalogo_dias_view.dart';
import 'catalogo_horarios_view.dart';
import 'catalogo_medicos_view.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Pantalla "Catalogos" del administrador (escritorio).
/// Titulo, chips de seccion (Medicos, Consultorios, Horarios y dias no habiles)
/// y el contenido de la seccion elegida.
class CatalogosView extends StatefulWidget {
  const CatalogosView({super.key, required this.servicio});

  final CatalogoService servicio;

  @override
  State<CatalogosView> createState() => _CatalogosViewState();
}

class _CatalogosViewState extends State<CatalogosView> {
  late final CatalogoController _c = CatalogoController(widget.servicio);

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
      builder: (context, _) => SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _encabezado(context),
            const SizedBox(height: 16),
            _chips(),
            const SizedBox(height: 20),
            if (_c.cargando)
              const Center(child: Padding(padding: EdgeInsets.all(32), child: CircularProgressIndicator()))
            else if (_c.errorCarga != null)
              _errorCarga()
            else
              _seccion(),
          ],
        ),
      ),
    );
  }

  Widget _encabezado(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            'Catálogos',
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.w800),
          ),
        ),
        const Chip(avatar: Icon(Icons.admin_panel_settings_outlined, size: 18), label: Text('Administrador')),
      ],
    );
  }

  Widget _chips() {
    ChoiceChip chip(String texto, SeccionCatalogo s) => ChoiceChip(
          label: Text(texto),
          selected: _c.seccion == s,
          onSelected: (_) => _c.cambiarSeccion(s),
        );
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        chip('Médicos', SeccionCatalogo.medicos),
        chip('Consultorios', SeccionCatalogo.consultorios),
        chip('Horarios y días no hábiles', SeccionCatalogo.horarios),
        // Pacientes queda fuera de esta tarea (T100-01): se muestra deshabilitado.
        const Tooltip(
          message: 'Disponible en otra tarea',
          child: ChoiceChip(label: Text('Pacientes'), selected: false, onSelected: null),
        ),
      ],
    );
  }

  Widget _errorCarga() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        MensajeError(_c.errorCarga),
        const SizedBox(height: 12),
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
            return Column(children: [horarios, const SizedBox(height: 20), dias]);
          }
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(flex: 3, child: horarios),
              const SizedBox(width: 20),
              Expanded(flex: 2, child: dias),
            ],
          );
        });
    }
  }
}
