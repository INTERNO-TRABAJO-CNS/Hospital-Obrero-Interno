import 'package:flutter/material.dart';

import '../../controllers/sesion_controller.dart';
import '../../core/app_theme.dart';
import '../../models/auth/sesion.dart';
import '../comun/cns_widgets.dart';

/// CU-01: iniciar sesion (Figma "LOGIN ESCRITORIO"; en celular es la misma tarjeta).
/// Al entrar llama [alEntrar] con la sesion; [alRegistrarse] abre la creacion de cuenta del titular.
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
    const nota = TextStyle(fontSize: 12, color: ColoresCns.textoSuave);
    return Theme(
      data: AppTheme.claro(),
      child: Scaffold(
        backgroundColor: ColoresCns.fondo,
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
                      const Center(child: LogoCns(tamano: 120)),
                      const SizedBox(height: 24),
                      TarjetaBlanca(
                        padding: const EdgeInsets.all(20),
                        child: AutofillGroup(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              const Text('Iniciar sesión', style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800)),
                              const SizedBox(height: 16),
                              CampoCns(
                                etiqueta: 'Usuario',
                                child: TextField(
                                  key: const Key('login_usuario'),
                                  controller: _usuario,
                                  textInputAction: TextInputAction.next,
                                  autofillHints: const [AutofillHints.username],
                                  decoration: const InputDecoration(hintText: 'Ingresar usuario'),
                                ),
                              ),
                              CampoCns(
                                etiqueta: 'Contraseña',
                                child: TextField(
                                  key: const Key('login_clave'),
                                  controller: _clave,
                                  obscureText: !_verClave,
                                  autofillHints: const [AutofillHints.password],
                                  onSubmitted: (_) => _entrar(),
                                  decoration: InputDecoration(
                                    hintText: 'Ingresar contraseña',
                                    suffixIcon: IconButton(
                                      tooltip: _verClave ? 'Ocultar contraseña' : 'Mostrar contraseña',
                                      icon: Icon(_verClave ? Icons.visibility_off : Icons.visibility, size: 20),
                                      onPressed: () => setState(() => _verClave = !_verClave),
                                    ),
                                  ),
                                ),
                              ),
                              if (_error != null)
                                Padding(
                                  padding: const EdgeInsets.only(bottom: 12),
                                  child: Text(
                                    _error!,
                                    key: const Key('login_error'),
                                    style: const TextStyle(color: ColoresCns.peligro, fontSize: 13),
                                  ),
                                ),
                              FilledButton(
                                key: const Key('login_entrar'),
                                onPressed: widget.controller.cargando ? null : _entrar,
                                child: Text(widget.controller.cargando ? 'Ingresando…' : 'Iniciar Sesión'),
                              ),
                              const SizedBox(height: 14),
                              Wrap(
                                crossAxisAlignment: WrapCrossAlignment.center,
                                children: [
                                  const Text('¿Aún no tienes cuenta? Si eres asegurado titular activo, ', style: nota),
                                  InkWell(
                                    key: const Key('login_registrarse'),
                                    onTap: widget.alRegistrarse,
                                    child: Text(
                                      'regístrate aquí.',
                                      style: nota.copyWith(
                                        color: ColoresCns.menu,
                                        fontWeight: FontWeight.w700,
                                        decoration: TextDecoration.underline,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
