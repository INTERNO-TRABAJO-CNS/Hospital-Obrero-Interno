import 'package:flutter/material.dart';

import '../../controllers/sesion_controller.dart';
import '../../models/auth/paciente.dart';

/// CU-03: el titular registra un beneficiario con su afiliacion y el CI del beneficiario.
/// La fecha de nacimiento es obligatoria (Pediatria solo menores de 10 anios).
class RegistroBeneficiarioView extends StatefulWidget {
  const RegistroBeneficiarioView({super.key, required this.controller, required this.alTerminar});

  final SesionController controller;
  final VoidCallback alTerminar;

  @override
  State<RegistroBeneficiarioView> createState() => _RegistroBeneficiarioViewState();
}

class _RegistroBeneficiarioViewState extends State<RegistroBeneficiarioView> {
  late final _afiliado = TextEditingController(text: widget.controller.numeroAfiliado ?? '');
  final _ci = TextEditingController();
  final _nombres = TextEditingController();
  final _apellidos = TextEditingController();
  DateTime? _nacimiento;
  String? _error;

  @override
  void dispose() {
    for (final c in [_afiliado, _ci, _nombres, _apellidos]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _elegirFecha() async {
    final hoy = DateTime.now();
    final elegida = await showDatePicker(
      context: context,
      initialDate: _nacimiento ?? DateTime(hoy.year - 5),
      firstDate: DateTime(1900),
      lastDate: hoy,
      helpText: 'Fecha de nacimiento',
    );
    if (elegida != null) {
      setState(() => _nacimiento = elegida);
    }
  }

  Future<void> _registrar() async {
    if (_nacimiento == null) {
      setState(() => _error = 'Elija la fecha de nacimiento.');
      return;
    }
    final error = await widget.controller.registrarBeneficiario(RegistroBeneficiario(
      numeroAfiliadoTitular: _afiliado.text,
      ci: _ci.text,
      nombres: _nombres.text,
      apellidos: _apellidos.text,
      fechaNacimiento: _nacimiento!,
    ));
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    if (error == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Beneficiario registrado')));
      widget.alTerminar();
    }
  }

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final fecha = _nacimiento;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Registrar beneficiario'),
        leading: IconButton(icon: const Icon(Icons.arrow_back), onPressed: widget.alTerminar),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 480),
              child: ListenableBuilder(
                listenable: widget.controller,
                builder: (context, _) => Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    for (final (clave, etiqueta, c) in [
                      ('beneficiario_afiliado', 'Número de afiliado del titular', _afiliado),
                      ('beneficiario_ci', 'CI del beneficiario', _ci),
                      ('beneficiario_nombres', 'Nombres', _nombres),
                      ('beneficiario_apellidos', 'Apellidos', _apellidos),
                    ])
                      Padding(
                        padding: const EdgeInsets.only(bottom: 14),
                        child: TextField(
                          key: Key(clave),
                          controller: c,
                          decoration: InputDecoration(labelText: etiqueta, border: const OutlineInputBorder()),
                        ),
                      ),
                    OutlinedButton.icon(
                      key: const Key('beneficiario_fecha'),
                      onPressed: _elegirFecha,
                      icon: const Icon(Icons.cake_outlined),
                      label: Text(fecha == null
                          ? 'Fecha de nacimiento'
                          : 'Nació el ${fecha.day.toString().padLeft(2, '0')}/'
                              '${fecha.month.toString().padLeft(2, '0')}/${fecha.year}'),
                    ),
                    const SizedBox(height: 16),
                    if (_error != null) ...[
                      Text(_error!, key: const Key('beneficiario_error'),
                          style: TextStyle(color: tema.colorScheme.error)),
                      const SizedBox(height: 12),
                    ],
                    FilledButton(
                      key: const Key('beneficiario_guardar'),
                      onPressed: widget.controller.cargando ? null : _registrar,
                      child: const Padding(
                        padding: EdgeInsets.symmetric(vertical: 12),
                        child: Text('Registrar'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
