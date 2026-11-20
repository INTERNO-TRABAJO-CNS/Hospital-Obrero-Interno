import 'package:flutter/material.dart';
import '../../controllers/solicitud_controller.dart';
import 'resultado_solicitud_view.dart';
import '../../models/catalogo/especialidad.dart';

class SolicitarCitaView extends StatefulWidget {
  const SolicitarCitaView({
  super.key,
  required this.controller,
  this.alVolver,
});

final SolicitudController controller;

  final VoidCallback? alVolver;
  @override
  State<SolicitarCitaView> createState() => _SolicitarCitaViewState();
}

class _SolicitarCitaViewState extends State<SolicitarCitaView> {
  Especialidad? especialidadSeleccionada;

  Future<void> _continuar() async {
  if (especialidadSeleccionada == null) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Seleccione una especialidad.'),
      ),
    );
    return;
  }

  final resultado = await widget.controller.solicitar(
    especialidadSeleccionada!,
  );

  if (!mounted) return;

  if (resultado == null) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          widget.controller.error ?? 'No se pudo enviar la solicitud.',
        ),
      ),
    );
    return;
  }

  Navigator.of(context).push(
    MaterialPageRoute(
      builder: (_) => ResultadoSolicitudView(
        resultado: resultado,
      ),
    ),
  );
}
  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final colores = tema.colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Solicitar cita'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: widget.alVolver ?? () => Navigator.of(context).pop(),
        ),
      ),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 850),
          child: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text(
                'Solicite una cita médica',
                style: tema.textTheme.headlineSmall,
              ),
              const SizedBox(height: 8),
              Text(
                'Seleccione la especialidad. El sistema le asignará médico, día y hora '
                'en cuanto haya un cupo disponible y le avisará.',
                style: tema.textTheme.bodyMedium,
              ),
              const SizedBox(height: 28),

              Text(
                'Seleccione una especialidad',
                style: tema.textTheme.titleMedium,
              ),
              const SizedBox(height: 12),

              ...Especialidad.basicas.map((especialidad) {
                final seleccionada =
                    especialidadSeleccionada == especialidad;

                final titulo = switch (especialidad) {
                  Especialidad.consultorioFamiliar => 'Atención General',
                  Especialidad.odontologia => 'Odontología',
                  Especialidad.pediatria => 'Pediatría',
                  _ => especialidad.nombre,
                };

                final icono = switch (especialidad) {
                  Especialidad.consultorioFamiliar => Icons.medical_services,
                  Especialidad.odontologia => Icons.health_and_safety,
                  Especialidad.pediatria => Icons.child_care,
                  _ => Icons.local_hospital,
                };

                return Card(
                  margin: const EdgeInsets.only(bottom: 10),
                  color: seleccionada
                      ? colores.primaryContainer
                      : colores.surface,
                  child: ListTile(
                    leading: Icon(
                      icono,
                      size: 32,
                      color: colores.primary,
                    ),
                    title: Text(
                      titulo,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    subtitle: Text(especialidad.rango),
                    trailing: Icon(
                      seleccionada
                          ? Icons.radio_button_checked
                          : Icons.radio_button_unchecked,
                      color: colores.primary,
                    ),
                    onTap: () {
                      setState(() {
                        especialidadSeleccionada = especialidad;
                      });
                    },
                  ),
                );
              }),

              const SizedBox(height: 24),

              const SizedBox(height: 28),

              SizedBox(
  height: 52,
  child: FilledButton(
    onPressed: widget.controller.cargando ? null : _continuar,
    child: widget.controller.cargando
        ? const SizedBox(
            height: 24,
            width: 24,
            child: CircularProgressIndicator(
              strokeWidth: 2,
            ),
          )
        : const Text(
            'Continuar',
            style: TextStyle(fontSize: 16),
          ),
  ),
),
            ],
          ),
        ),
      ),
    );
  }
}