import 'package:flutter/material.dart';

import '../../controllers/notificaciones_controller.dart';
import '../../core/app_theme.dart';
import '../comun/cns_widgets.dart';
import 'detalle_cita_view.dart';

/// Avisos del paciente (Figma "NOTIFICACIONES v2"): fondo verde, encabezado con logo y "Salir" y una tarjeta
/// blanca por aviso. Los nuevos llegan en vivo y se muestran tambien en un aviso abajo.
class NotificacionesView extends StatefulWidget {
  const NotificacionesView({super.key, required this.controller, this.alSalir});

  final NotificacionesController controller;

  /// Cerrar sesión / cambiar de usuario. Sin él no se muestra el botón.
  final VoidCallback? alSalir;

  @override
  State<NotificacionesView> createState() => _NotificacionesViewState();
}

class _NotificacionesViewState extends State<NotificacionesView> {
  String? _ultimaMostrada;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_onChanged);
    widget.controller.cargar();
    widget.controller.conectar();
  }

  @override
  void dispose() {
    widget.controller.removeListener(_onChanged);
    super.dispose();
  }

  void _onChanged() {
    if (!mounted) return;
    final ultima = widget.controller.ultima;
    if (ultima != null && ultima.mensaje != _ultimaMostrada) {
      _ultimaMostrada = ultima.mensaje;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(ultima.mensaje)),
      );
    }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final controller = widget.controller;
    return Theme(
      data: AppTheme.claro(),
      child: ColoredBox(
        color: ColoresCns.fondo,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(18, 14, 18, 24),
          children: [
            EncabezadoPaciente(titulo: 'Avisos', alSalir: widget.alSalir),
            const SizedBox(height: 18),
            ..._lista(controller),
          ],
        ),
      ),
    );
  }

  List<Widget> _lista(NotificacionesController controller) {
    if (controller.isLoading && controller.notificaciones.isEmpty) {
      return const [Cargando()];
    }
    if (controller.errorMessage != null && controller.notificaciones.isEmpty) {
      return [
        TarjetaBlanca(
          child: Text(controller.errorMessage!,
              key: const Key('notificaciones-error'), style: const TextStyle(color: ColoresCns.peligro)),
        ),
      ];
    }
    if (controller.notificaciones.isEmpty) {
      return const [TarjetaBlanca(child: Text('No tienes notificaciones.'))];
    }
    return [
      for (final (index, notificacion) in controller.notificaciones.indexed) ...[
        TarjetaBlanca(
          key: Key('notificacion-${notificacion.citaId}-$index'),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Icon(Icons.notifications_active_outlined, color: ColoresCns.menu),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(notificacion.mensaje, style: const TextStyle(fontSize: 14)),
                    const SizedBox(height: 4),
                    Text(notificacion.tipo, style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
      ],
    ];
  }
}
