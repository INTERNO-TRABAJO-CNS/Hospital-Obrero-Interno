import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../controllers/cuentas_controller.dart';
import '../../core/app_theme.dart';
import '../../models/auth/sesion.dart';
import '../../models/personal/cuenta.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// Cuentas del personal (`/api/v1/admin/usuarios`). El Figma no tiene esta pantalla: sigue el
/// formato de los otros catalogos (tabla + panel). La contrasena inicial la entrega el
/// administrador en persona; nunca se vuelve a mostrar.
class CatalogoCuentasView extends StatefulWidget {
  const CatalogoCuentasView({super.key, required this.controller, required this.catalogo});

  final CuentasController controller;

  /// Para elegir el medico de una cuenta MEDICO.
  final CatalogoController catalogo;

  @override
  State<CatalogoCuentasView> createState() => _CatalogoCuentasViewState();
}

class _CatalogoCuentasViewState extends State<CatalogoCuentasView> {
  final _usuario = TextEditingController();
  final _nombre = TextEditingController();
  final _clave = TextEditingController();
  Rol _rol = Rol.enfermeria;
  int? _medicoId;
  Cuenta? _seleccionada;
  String? _error;
  bool _guardando = false;
  int _version = 0;

  CuentasController get _c => widget.controller;

  @override
  void dispose() {
    _usuario.dispose();
    _nombre.dispose();
    _clave.dispose();
    super.dispose();
  }

  void _limpiar() {
    _usuario.clear();
    _nombre.clear();
    _clave.clear();
    _medicoId = null;
    _version++;
  }

  Future<void> _crear() async {
    String nombre = _nombre.text;
    if (_rol == Rol.medico) {
      final medico = widget.catalogo.medicosActivos.where((m) => m.id == _medicoId);
      nombre = medico.isEmpty ? '' : medico.first.nombreCompleto;
    }
    final datos = NuevaCuenta(
      usuario: _usuario.text,
      clave: _clave.text,
      rol: _rol,
      personaId: _rol == Rol.medico ? _medicoId : null,
      nombre: nombre,
    );
    setState(() => _guardando = true);
    final error = await _c.crear(datos);
    if (!mounted) {
      return;
    }
    setState(() {
      _guardando = false;
      _error = error;
      if (error == null) {
        _limpiar();
      }
    });
    if (error == null) {
      avisar(context, 'Cuenta "${datos.usuario.trim()}" creada');
    }
  }

  Future<void> _darDeBaja() async {
    final cuenta = _seleccionada!;
    final ok = await confirmar(context, 'Dar de baja la cuenta',
        '¿Dar de baja la cuenta "${cuenta.usuario}"? Ya no podrá iniciar sesión (no se borra).');
    if (!ok || !mounted) {
      return;
    }
    final error = await _c.darDeBaja(cuenta.usuarioId);
    if (!mounted) {
      return;
    }
    setState(() {
      _error = error;
      if (error == null) {
        _seleccionada = null;
      }
    });
    if (error == null) {
      avisar(context, 'Cuenta dada de baja');
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: Listenable.merge([_c, widget.catalogo]),
      builder: (context, _) {
        if (_c.cargando && _c.cuentas.isEmpty) {
          return const Cargando();
        }
        final lista = _c.cuentas;
        final i = lista.indexWhere((c) => c.usuarioId == _seleccionada?.usuarioId);
        return DosColumnas(
          principal: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (_c.errorCarga != null) MensajeError(_c.errorCarga),
              TablaCatalogo(
                columnas: const [
                  ColumnaTabla('Usuario', flex: 2),
                  ColumnaTabla('Nombre', flex: 3),
                  ColumnaTabla('Rol', flex: 2),
                  ColumnaTabla('Estado', flex: 2),
                ],
                filas: [
                  for (final c in lista)
                    [
                      Text(c.usuario),
                      Text(c.nombre),
                      ChipEstado('ROL', texto: c.rol.etiqueta.toUpperCase()),
                      EstadoChip(activo: c.activo),
                    ],
                ],
                seleccionada: i < 0 ? null : i,
                alTocarFila: (fila) => setState(() {
                  _seleccionada = lista[fila];
                  _error = null;
                }),
                textoVacio: 'No hay cuentas del personal',
              ),
              const NotaPie('Las cuentas de pacientes las crea cada titular al registrarse en la app. '
                  'Una cuenta dada de baja ya no puede iniciar sesión.'),
            ],
          ),
          lateral: _panel(),
        );
      },
    );
  }

  Widget _panel() {
    final medicos = widget.catalogo.medicosActivos;
    final seleccion = _seleccionada;
    return PanelCatalogo(
      titulo: 'Nueva cuenta del personal',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CampoCns(
            etiqueta: 'Rol',
            child: DropdownButtonFormField<Rol>(
              key: const Key('cuenta_rol'),
              isExpanded: true,
              initialValue: _rol,
              items: [
                for (final r in const [Rol.medico, Rol.enfermeria, Rol.administrador])
                  DropdownMenuItem(value: r, child: Text(r.etiqueta)),
              ],
              onChanged: (r) => setState(() => _rol = r ?? _rol),
            ),
          ),
          if (_rol == Rol.medico)
            CampoCns(
              etiqueta: 'Médico',
              ayuda: 'El nombre se toma del catálogo de médicos.',
              child: DropdownButtonFormField<int>(
                key: ValueKey('cuenta_medico_$_version'),
                isExpanded: true,
                initialValue: _medicoId,
                hint: const Text('Elegir médico'),
                items: [
                  for (final m in medicos)
                    DropdownMenuItem(value: m.id, child: Text('${m.nombreCompleto} · ${m.especialidad.nombre}')),
                ],
                onChanged: (id) => setState(() => _medicoId = id),
              ),
            )
          else
            CampoCns(
              etiqueta: 'Nombre completo',
              child: TextField(
                key: const Key('cuenta_nombre'),
                controller: _nombre,
                maxLength: nombreMaximo,
                decoration: const InputDecoration(hintText: 'Ej. María Flores', counterText: ''),
              ),
            ),
          CampoCns(
            etiqueta: 'Usuario',
            child: TextField(
              key: const Key('cuenta_usuario'),
              controller: _usuario,
              maxLength: usuarioMaximo,
              decoration: const InputDecoration(hintText: 'Ej. mflores', counterText: ''),
            ),
          ),
          CampoCns(
            etiqueta: 'Contraseña inicial',
            ayuda: 'Mínimo $claveMinima caracteres. Entréguela en persona: después no se puede volver a ver.',
            child: TextField(
              key: const Key('cuenta_clave'),
              controller: _clave,
              obscureText: true,
              decoration: const InputDecoration(hintText: 'Contraseña'),
            ),
          ),
          MensajeError(_error),
          FilledButton(
            key: const Key('cuenta_crear'),
            onPressed: _guardando ? null : _crear,
            child: Text(_guardando ? 'Creando…' : 'Crear cuenta'),
          ),
          if (seleccion != null) ...[
            const SizedBox(height: 10),
            BotonPeligro(
              key: const Key('cuenta_baja'),
              texto: 'Dar de baja a ${seleccion.usuario}',
              alTocar: seleccion.activo ? _darDeBaja : null,
            ),
          ],
          const SizedBox(height: 6),
          const Text('No puede dar de baja su propia cuenta.',
              style: TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
        ],
      ),
    );
  }
}
