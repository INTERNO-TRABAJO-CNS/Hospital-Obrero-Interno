import 'package:flutter/material.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/solicitud/solicitud.dart';

class ResultadoSolicitudView extends StatelessWidget {
  const ResultadoSolicitudView({
    super.key,
    required this.resultado,
  });

  final ResultadoSolicitud resultado;

  String _nombreEstado() {
    switch (resultado.estado) {
      case EstadoSolicitudApp.pendiente:
        return 'Pendiente';
      case EstadoSolicitudApp.enEspera:
        return 'En espera';
      case EstadoSolicitudApp.asignada:
        return 'Cita asignada';
      case EstadoSolicitudApp.sinCupo:
        return 'Sin cupo';
    }
  }

  IconData _iconoEstado() {
    switch (resultado.estado) {
      case EstadoSolicitudApp.pendiente:
        return Icons.hourglass_empty;
      case EstadoSolicitudApp.enEspera:
      case EstadoSolicitudApp.sinCupo:
        return Icons.event_busy;
      case EstadoSolicitudApp.asignada:
        return Icons.check_circle_outline;
    }
  }

  String _nombreEspecialidad() {
    switch (resultado.especialidad) {
      case Especialidad.consultorioFamiliar:
        return 'Atención General';
      case Especialidad.odontologia:
        return 'Odontología';
      case Especialidad.pediatria:
        return 'Pediatría';
      default:
        return resultado.especialidad.nombre;
    }
  }

  String _formatearFecha(DateTime fecha) {
    return '${fecha.day.toString().padLeft(2, '0')}/'
        '${fecha.month.toString().padLeft(2, '0')}/'
        '${fecha.year}';
  }

  @override
  Widget build(BuildContext context) {
    final colores = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Resultado de solicitud'),
        automaticallyImplyLeading: false,
      ),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 600),
          child: ListView(
            padding: const EdgeInsets.all(24),
            children: [
              const SizedBox(height: 20),
              Icon(
                _iconoEstado(),
                size: 80,
                color: colores.primary,
              ),
              const SizedBox(height: 20),
              Center(
                child: Text(
                  _nombreEstado(),
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
              ),
              const SizedBox(height: 16),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        resultado.mensaje,
                        style: Theme.of(context).textTheme.bodyLarge,
                      ),
                      const Divider(height: 32),
                      _dato('Número de solicitud',
                          resultado.solicitudId.toString()),
                      _dato('Especialidad', _nombreEspecialidad()),
                      _dato(
                        'Buscando cupo desde',
                        _formatearFecha(resultado.diaSolicitado),
                      ),
                      _dato('Orden de llegada',
                          resultado.ordenLlegada.toString()),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                height: 50,
                child: FilledButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Volver'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _dato(String titulo, String valor) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            titulo,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          Text(valor),
        ],
      ),
    );
  }
}