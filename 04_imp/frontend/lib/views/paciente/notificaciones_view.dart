import 'package:flutter/material.dart';

import '../../controllers/notificaciones_controller.dart';

class NotificacionesView extends StatefulWidget {
  const NotificacionesView({super.key, required this.controller});

  final NotificacionesController controller;

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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificaciones'),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Center(
              child: Badge(
                label: Text('${controller.notificaciones.length}'),
                child: const Icon(Icons.notifications),
              ),
            ),
          ),
        ],
      ),
      body: controller.isLoading && controller.notificaciones.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : _buildList(controller),
    );
  }

  Widget _buildList(NotificacionesController controller) {
    if (controller.errorMessage != null && controller.notificaciones.isEmpty) {
      return Center(child: Text(controller.errorMessage!, key: const Key('notificaciones-error')));
    }
    if (controller.notificaciones.isEmpty) {
      return const Center(child: Text('No tienes notificaciones.'));
    }
    return ListView.builder(
      itemCount: controller.notificaciones.length,
      itemBuilder: (context, index) {
        final notificacion = controller.notificaciones[index];
        return ListTile(
          key: Key('notificacion-${notificacion.citaId}-$index'),
          leading: const Icon(Icons.notifications_active),
          title: Text(notificacion.mensaje),
          subtitle: Text(notificacion.tipo),
        );
      },
    );
  }
}