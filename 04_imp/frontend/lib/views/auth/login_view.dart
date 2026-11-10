import 'package:flutter/material.dart';

import '../../controllers/sesion_controller.dart';
import '../../models/auth/sesion.dart';

/// CU-01: iniciar sesion (celular y escritorio). Al entrar llama [alEntrar] con la sesion;
/// [alRegistrarse] abre la creacion de cuenta del titular.
class LoginView extends StatefulWidget {
  const LoginView({super.key, required this.controller, required this.alEntrar, required this.alRegistrarse});

  final SesionController controller;
  final void Function(Sesion sesion) alEntrar;
  final VoidCallback alRegistrarse;

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  final _usuario = TextEditingController();
  final _clave = TextEditingController();
  String? _error;
  bool _verClave = false;

  @override
  void dispose() {
    _usuario.dispose();
    _clave.dispose();
    super.dispose();
  }

  Future<void> _entrar() async {
    final error = await widget.controller.entrar(_usuario.text, _clave.text);
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    final sesion = widget.controller.sesion;
    if (error == null && sesion != null) {
      widget.alEntrar(sesion);
    }
  }

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: ListenableBuilder(
                listenable: widget.controller,
                builder: (context, _) => Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Icon(Icons.local_hospital_rounded, size: 56, color: tema.colorScheme.primary),
                    const SizedBox(height: 12),
                    Text('CNS Hospital Obrero',
                        textAlign: TextAlign.center, style: tema.textTheme.headlineSmall),
                    const SizedBox(height: 4),
                    Text('Inicie sesión para gestionar sus citas',
                        textAlign: TextAlign.center, style: tema.textTheme.bodyMedium),
                    const SizedBox(height: 28),
                    TextField(
                      key: const Key('login_usuario'),
                      controller: _usuario,
                      textInputAction: TextInputAction.next,
                      autofillHints: const [AutofillHints.username],
                      decoration: const InputDecoration(labelText: 'Usuario', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 14),
                    TextField(
                      key: const Key('login_clave'),
                      controller: _clave,
                      obscureText: !_verClave,
                      autofillHints: const [AutofillHints.password],
                      onSubmitted: (_) => _entrar(),
                      decoration: InputDecoration(
                        labelText: 'Contraseña',
                        border: const OutlineInputBorder(),
                        suffixIcon: IconButton(
                          tooltip: _verClave ? 'Ocultar' : 'Mostrar',
                          icon: Icon(_verClave ? Icons.visibility_off : Icons.visibility),
                          onPressed: () => setState(() => _verClave = !_verClave),
                        ),
                      ),
                    ),
                    if (_error != null) ...[
                      const SizedBox(height: 12),
                      Text(_error!, key: const Key('login_error'), style: TextStyle(color: tema.colorScheme.error)),
                    ],
                    const SizedBox(height: 20),
                    FilledButton(
                      key: const Key('login_entrar'),
                      onPressed: widget.controller.cargando ? null : _entrar,
                      child: Padding(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        child: Text(widget.controller.cargando ? 'Ingresando…' : 'Ingresar'),
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextButton(
                      key: const Key('login_registrarse'),
                      onPressed: widget.alRegistrarse,
                      child: const Text('¿Es titular asegurado? Cree su cuenta'),
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
