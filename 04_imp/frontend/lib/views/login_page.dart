import 'package:flutter/material.dart';

import '../controllers/login_controller.dart';
import '../models/sesion_dto.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key, required this.controller, this.onAuthenticated});

  final LoginController controller;
  final ValueChanged<SesionDto>? onAuthenticated;

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  late final TextEditingController _usuarioController;
  late final TextEditingController _claveController;
  bool _authenticationReported = false;

  @override
  void initState() {
    super.initState();
    _usuarioController = TextEditingController();
    _claveController = TextEditingController();
    widget.controller.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    widget.controller.removeListener(_onControllerChanged);
    _usuarioController.dispose();
    _claveController.dispose();
    super.dispose();
  }

  void _onControllerChanged() {
    final session = widget.controller.sesion;
    if (session != null && !_authenticationReported) {
      _authenticationReported = true;
      widget.onAuthenticated?.call(session);
    }
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CNS - Iniciar sesion')),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Form(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: _usuarioController,
                    decoration: const InputDecoration(labelText: 'Usuario'),
                    enabled: !widget.controller.isLoading,
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _claveController,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: 'Clave'),
                    enabled: !widget.controller.isLoading,
                  ),
                  if (widget.controller.errorMessage != null) ...[
                    const SizedBox(height: 16),
                    Text(
                      widget.controller.errorMessage!,
                      key: const Key('login-error'),
                      style: TextStyle(color: ThemeData.light().colorScheme.error),
                    ),
                  ],
                  const SizedBox(height: 24),
                  FilledButton(
                    onPressed: widget.controller.isLoading
                        ? null
                        : () => widget.controller.login(
                              _usuarioController.text,
                              _claveController.text,
                            ),
                    child: widget.controller.isLoading
                        ? const CircularProgressIndicator()
                        : const Text('Ingresar'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}