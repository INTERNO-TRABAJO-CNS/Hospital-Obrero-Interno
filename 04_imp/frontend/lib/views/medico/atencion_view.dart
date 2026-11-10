import 'package:flutter/material.dart';

import '../../controllers/atencion_controller.dart';

class AtencionView extends StatefulWidget {
  const AtencionView({super.key, required this.controller});

  final AtencionController controller;

  @override
  State<AtencionView> createState() => _AtencionViewState();
}

class _AtencionViewState extends State<AtencionView> {
  late final TextEditingController _medicacionController;

  @override
  void initState() {
    super.initState();
    _medicacionController = TextEditingController();
    widget.controller.addListener(_refresh);
    widget.controller.cargar();
  }

  @override
  void dispose() {
    widget.controller.removeListener(_refresh);
    _medicacionController.dispose();
    super.dispose();
  }

  void _refresh() {
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final controller = widget.controller;
    return Scaffold(
      appBar: AppBar(title: const Text('Finalizar atencion')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: controller.isLoading && controller.datos == null
            ? const Center(child: CircularProgressIndicator())
            : _buildContent(controller),
      ),
    );
  }

  Widget _buildContent(AtencionController controller) {
    if (controller.errorMessage != null && controller.datos == null) {
      return Text(
        controller.errorMessage!,
        key: const Key('atencion-error'),
      );
    }

    final datos = controller.datos!;
    return ListView(
      children: [
        Text('Afiliado: ${datos.afiliado}'),
        Text('Ficha: ${datos.ficha}'),
        Text('Fecha: ${datos.fecha}'),
        Text('Hora: ${datos.hora}'),
        Text('Consulta: ${datos.consulta}'),
        const SizedBox(height: 24),
        TextField(
          key: const Key('medicacion-field'),
          controller: _medicacionController,
          maxLength: 2000,
          maxLines: 5,
          decoration: const InputDecoration(
            labelText: 'Medicacion',
            border: OutlineInputBorder(),
          ),
          enabled: !controller.isLoading,
        ),
        if (controller.errorMessage != null)
          Text(
            controller.errorMessage!,
            key: const Key('atencion-error'),
          ),
        if (controller.resultado?.resultado == 'REGISTRADA')
          const Text(
            'Atencion finalizada correctamente.',
            key: Key('atencion-success'),
          ),
        const SizedBox(height: 16),
        FilledButton(
          key: const Key('finalizar-atencion-button'),
          onPressed: controller.isLoading
              ? null
              : () => controller.registrar(_medicacionController.text),
          child: const Text('Finalizar atencion'),
        ),
      ],
    );
  }
}