import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../models/catalogo/consultorio.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Catalogo de consultorios. El Figma no tiene esta pantalla: sigue el
/// mismo formato que la de medicos (tabla + panel "Nuevo / editar").
class CatalogoConsultoriosView extends StatefulWidget {
  const CatalogoConsultoriosView({super.key, required this.controller});

  final CatalogoController controller;

  @override
  State<CatalogoConsultoriosView> createState() => _CatalogoConsultoriosViewState();
}

class _CatalogoConsultoriosViewState extends State<CatalogoConsultoriosView> {
  final _formulario = GlobalKey<FormState>();
  final _nombre = TextEditingController();

  Consultorio? _seleccionado;
  String? _error;
  bool _guardando = false;

  CatalogoController get _c => widget.controller;

  @override
  void dispose() {
    _nombre.dispose();
    super.dispose();
  }

  void _seleccionar(Consultorio? c) {
    setState(() {
      _seleccionado = c;
      _error = null;
      _nombre.text = c?.nombre ?? '';
    });
  }

  Future<void> _guardar() async {
    if (!_formulario.currentState!.validate()) {
      return;
    }
    final consultorio = Consultorio(
      id: _seleccionado?.id ?? 0,
      nombre: _nombre.text.trim(),
      activo: _seleccionado?.activo ?? true,
    );
    setState(() => _guardando = true);
    final error = await _c.guardarConsultorio(consultorio);
    if (!mounted) {
      return;
    }
    setState(() {
      _guardando = false;
      _error = error;
    });
    if (error == null) {
      avisar(context, consultorio.id == 0 ? 'Consultorio registrado' : 'Consultorio actualizado');
      _seleccionar(null);
    }
  }

  Future<void> _darDeBaja() async {
    final c = _seleccionado!;
    final ok = await confirmar(context, 'Dar de baja', '¿Dar de baja "${c.nombre}"? No se borra.');
    if (!ok || !mounted) {
      return;
    }
    final error = await _c.darDeBajaConsultorio(c.id);
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    if (error == null) {
      avisar(context, 'Consultorio dado de baja');
      _seleccionar(null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final lista = _c.consultorios;
        final i = lista.indexWhere((c) => c.id == _seleccionado?.id);
        return DosColumnas(
          principal: TablaCatalogo(
            columnas: const [ColumnaTabla('N.º'), ColumnaTabla('Consultorio', flex: 4), ColumnaTabla('Estado', flex: 2)],
            filas: [
              for (final c in lista) [Text('${c.id}'), Text(c.nombre), EstadoChip(activo: c.activo)],
            ],
            seleccionada: i < 0 ? null : i,
            alTocarFila: (fila) => _seleccionar(lista[fila]),
            textoVacio: 'No hay consultorios registrados',
          ),
          lateral: _panel(),
        );
      },
    );
  }

  Widget _panel() {
    final editando = _seleccionado != null;
    return PanelCatalogo(
      titulo: editando ? 'Editar consultorio' : 'Nuevo consultorio',
      child: Form(
        key: _formulario,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            CampoCns(
              etiqueta: 'Nombre',
              child: TextFormField(
                key: const Key('consultorio_nombre'),
                controller: _nombre,
                decoration: const InputDecoration(hintText: 'Ej. Consultorio 7'),
                validator: validarTexto,
              ),
            ),
            MensajeError(_error),
            FilledButton(
              key: const Key('consultorio_guardar'),
              onPressed: _guardando ? null : _guardar,
              child: Text(_guardando ? 'Guardando…' : 'Guardar'),
            ),
            const SizedBox(height: 10),
            BotonPeligro(texto: 'Dar de baja', alTocar: editando && _seleccionado!.activo ? _darDeBaja : null),
            if (editando)
              TextButton(onPressed: () => _seleccionar(null), child: const Text('Nuevo consultorio')),
            const SizedBox(height: 8),
            Text('No se da de baja si tiene citas programadas.', style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}
