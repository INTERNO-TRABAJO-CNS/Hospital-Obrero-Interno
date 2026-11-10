import 'package:flutter/material.dart';

import '../../controllers/sesion_controller.dart';
import '../../models/auth/paciente.dart';
import '../../models/auth/sesion.dart';

/// CU-03: el titular crea su cuenta validando su numero de afiliado y su CI contra la
/// tabla interna de la CNS. Nombre y fecha de nacimiento salen de esa tabla.
class RegistroTitularView extends StatefulWidget {
  const RegistroTitularView({super.key, required this.controller, required this.alRegistrar, required this.alVolver});

  final SesionController controller;
  final void Function(Sesion sesion) alRegistrar;
  final VoidCallback alVolver;

  @override
  State<RegistroTitularView> createState() => _RegistroTitularViewState();
}

class _RegistroTitularViewState extends State<RegistroTitularView> {
  final _afiliado = TextEditingController();
  final _ci = TextEditingController();
  final _usuario = TextEditingController();
  final _clave = TextEditingController();
  final _repetir = TextEditingController();
  String? _error;

  @override
  void dispose() {
    for (final c in [_afiliado, _ci, _usuario, _clave, _repetir]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _registrar() async {
    if (_clave.text != _repetir.text) {
      setState(() => _error = 'Las contraseñas no coinciden.');
      return;
    }
    final error = await widget.controller.registrarTitular(RegistroTitular(
      numeroAfiliado: _afiliado.text,
      ci: _ci.text,
      usuario: _usuario.text,
      clave: _clave.text,
    ));
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    final sesion = widget.controller.sesion;
    if (error == null && sesion != null) {
      widget.alRegistrar(sesion);
    }
  }

  Widget _campo(String clave, String etiqueta, TextEditingController c,
      {bool oculto = false, String? ayuda, TextInputType? teclado}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: TextField(
        key: Key(clave),
        controller: c,
        obscureText: oculto,
        keyboardType: teclado,
        decoration: InputDecoration(labelText: etiqueta, helperText: ayuda, border: const OutlineInputBorder()),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Crear cuenta del titular'),
        leading: IconButton(icon: const Icon(Icons.arrow_back), onPressed: widget.alVolver),
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
                    Text('Solo el titular asegurado crea la cuenta. Sus beneficiarios se registran después, '
                        'desde su cuenta, con su CI.', style: tema.textTheme.bodyMedium),
                    const SizedBox(height: 20),
                    _campo('registro_afiliado', 'Número de afiliado', _afiliado, ayuda: 'Ejemplo: AF-10001'),
                    _campo('registro_ci', 'Carnet de identidad (CI)', _ci, teclado: TextInputType.number),
                    _campo('registro_usuario', 'Usuario', _usuario),
                    _campo('registro_clave', 'Contraseña', _clave, oculto: true, ayuda: 'Al menos 6 caracteres'),
                    _campo('registro_repetir', 'Repetir contraseña', _repetir, oculto: true),
                    if (_error != null) ...[
                      Text(_error!, key: const Key('registro_error'), style: TextStyle(color: tema.colorScheme.error)),
                      const SizedBox(height: 12),
                    ],
                    FilledButton(
                      key: const Key('registro_crear'),
                      onPressed: widget.controller.cargando ? null : _registrar,
                      child: Padding(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        child: Text(widget.controller.cargando ? 'Creando cuenta…' : 'Crear cuenta'),
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
