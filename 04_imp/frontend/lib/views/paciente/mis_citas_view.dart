import 'package:flutter/material.dart';

import '../../controllers/solicitud_controller.dart';
import '../../models/solicitud/solicitud.dart';

class MisCitasView extends StatefulWidget {
  const MisCitasView({
    super.key,
    required this.controller,
  });

  final SolicitudController controller;

  @override
  State<MisCitasView> createState() => _MisCitasViewState();
}

class _MisCitasViewState extends State<MisCitasView> {
  @override
  void initState() {
    super.initState();
    widget.controller.listar();
  }

  String _estado(EstadoSolicitudApp estado) {
    switch (estado) {
      case EstadoSolicitudApp.pendiente:
        return 'Pendiente';
      case EstadoSolicitudApp.enEspera:
        return 'En espera';
      case EstadoSolicitudApp.asignada:
        return 'Asignada';
      case EstadoSolicitudApp.sinCupo:
        return 'Sin cupo';
    }
  }

  Color _colorEstado(EstadoSolicitudApp estado) {
    switch (estado) {
      case EstadoSolicitudApp.pendiente:
        return Colors.orange;
      case EstadoSolicitudApp.enEspera:
        return Colors.blue;
      case EstadoSolicitudApp.asignada:
        return Colors.green;
      case EstadoSolicitudApp.sinCupo:
        return Colors.red;
    }
  }

  String _fecha(DateTime fecha) {
    final dia = fecha.day.toString().padLeft(2, '0');
    final mes = fecha.month.toString().padLeft(2, '0');

    return '$dia/$mes/${fecha.year}';
  }

  Future<void> _actualizar() async {
    await widget.controller.listar();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis citas'),
      ),
      body: AnimatedBuilder(
        animation: widget.controller,
        builder: (context, child) {
          final controller = widget.controller;

          if (controller.cargando &&
              controller.solicitudes.isEmpty) {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }

          if (controller.error != null &&
              controller.solicitudes.isEmpty) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(
                      Icons.error_outline,
                      size: 48,
                      color: Colors.red,
                    ),
                    const SizedBox(height: 12),
                    Text(
                      controller.error!,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _actualizar,
                      child: const Text('Reintentar'),
                    ),
                  ],
                ),
              ),
            );
          }

          if (controller.solicitudes.isEmpty) {
            return RefreshIndicator(
              onRefresh: _actualizar,
              child: ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                children: const [
                  SizedBox(height: 130),
                  Icon(
                    Icons.calendar_today_outlined,
                    size: 65,
                    color: Colors.grey,
                  ),
                  SizedBox(height: 16),
                  Center(
                    child: Text(
                      'Todavía no tienes solicitudes de cita.',
                      textAlign: TextAlign.center,
                    ),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: _actualizar,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                const Text(
                  'Mis solicitudes de cita',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Aquí puedes consultar el estado de tus solicitudes.',
                ),
                const SizedBox(height: 16),
                for (final solicitud in controller.solicitudes)
                  _tarjeta(solicitud),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _tarjeta(ResultadoSolicitud solicitud) {
    final color = _colorEstado(solicitud.estado);

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              solicitud.especialidad.nombre,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(
                  Icons.calendar_today_outlined,
                  size: 18,
                ),
                const SizedBox(width: 8),
                Text(
                  'Buscando cupo desde: ${_fecha(solicitud.diaSolicitado)}',
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Text(
                  'Estado: ',
                  style: TextStyle(fontWeight: FontWeight.w600),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: color.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    _estado(solicitud.estado),
                    style: TextStyle(
                      color: color,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Número de solicitud: ${solicitud.solicitudId}',
            ),
          ],
        ),
      ),
    );
  }
}