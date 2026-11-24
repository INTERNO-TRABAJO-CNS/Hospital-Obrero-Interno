import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../models/catalogo/dia_no_habil.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

const List<String> _meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

/// "25 dic 2026".
String fechaCorta(DateTime f) => '${f.day} ${_meses[f.month - 1]} ${f.year}';

/// CU-02 · "Dias no habiles": carga manual (sin calendario oficial de feriados).
/// La primera fila (Sab–Dom) es informativa: los fines de semana no se atienden.
class CatalogoDiasView extends StatelessWidget {
  const CatalogoDiasView({super.key, required this.controller});

  final CatalogoController controller;

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: controller,
      builder: (context, _) {
        final dias = controller.diasActivos;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Subtitulo('Días no hábiles'),
            TablaCatalogo(
              columnas: const [ColumnaTabla('Fecha', flex: 2), ColumnaTabla('Descripción', flex: 3)],
              filaFija: const [Text('Sáb–Dom'), Text('Fin de semana (automático)')],
              filas: [
                for (final d in dias) [Text(fechaCorta(d.fecha)), Text(d.descripcion)],
              ],
              alTocarFila: (i) => _abrirFormulario(context, dias[i]),
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerLeft,
              child: FilledButton(
                key: const Key('dia_agregar'),
                onPressed: () => _abrirFormulario(context, null),
                child: const Text('Agregar día no hábil'),
              ),
            ),
          ],
        );
      },
    );
  }

  Future<void> _abrirFormulario(BuildContext context, DiaNoHabil? dia) async {
    final guardado = await showDialog<String>(
      context: context,
      builder: (_) => _DialogoDia(controller: controller, dia: dia),
    );
    if (guardado != null && context.mounted) {
      avisar(context, guardado);
    }
  }
}

class _DialogoDia extends StatefulWidget {
  const _DialogoDia({required this.controller, this.dia});

  final CatalogoController controller;
  final DiaNoHabil? dia;

  @override
  State<_DialogoDia> createState() => _DialogoDiaState();
}

class _DialogoDiaState extends State<_DialogoDia> {
  final _formulario = GlobalKey<FormState>();
  late final TextEditingController _descripcion = TextEditingController(text: widget.dia?.descripcion ?? '');
  DateTime? _fecha;
  String? _error;
  bool _guardando = false;

  @override
  void initState() {
    super.initState();
    _fecha = widget.dia?.fecha;
  }

  @override
  void dispose() {
    _descripcion.dispose();
    super.dispose();
  }

  Future<void> _elegirFecha() async {
    final hoy = DateTime.now();
    final manana = DateTime(hoy.year, hoy.month, hoy.day + 1);
    final f = await showDatePicker(
      context: context,
      initialDate: _fecha != null && _fecha!.isAfter(manana) ? _fecha! : manana,
      firstDate: manana,
      lastDate: DateTime(hoy.year + 2, 12, 31),
    );
    if (f != null) {
      setState(() => _fecha = f);
    }
  }

  Future<void> _guardar() async {
    if (!_formulario.currentState!.validate()) {
      return;
    }
    if (_fecha == null) {
      setState(() => _error = 'Elija una fecha.');
      return;
    }
    setState(() => _guardando = true);
    final error = await widget.controller.guardarDiaNoHabil(
      DiaNoHabil(id: widget.dia?.id ?? 0, fecha: _fecha!, descripcion: _descripcion.text.trim()),
    );
    if (!mounted) {
      return;
    }
    if (error == null) {
      Navigator.pop(context, 'Día no hábil guardado');
    } else {
      setState(() {
        _guardando = false;
        _error = error;
      });
    }
  }

  Future<void> _darDeBaja() async {
    final ok = await confirmar(context, 'Quitar día no hábil', '¿Volver a habilitar el ${fechaCorta(_fecha!)}?');
    if (!ok || !mounted) {
      return;
    }
    final error = await widget.controller.darDeBajaDiaNoHabil(widget.dia!.id);
    if (!mounted) {
      return;
    }
    if (error == null) {
      Navigator.pop(context, 'Día no hábil quitado');
    } else {
      setState(() => _error = error);
    }
  }

  @override
  Widget build(BuildContext context) {
    final editando = widget.dia != null;
    return AlertDialog(
      title: Text(editando ? 'Editar día no hábil' : 'Agregar día no hábil'),
      content: SizedBox(
        width: 380,
        child: Form(
          key: _formulario,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              OutlinedButton.icon(
                onPressed: _elegirFecha,
                icon: const Icon(Icons.event),
                label: Text(_fecha == null ? 'Elegir fecha' : fechaCorta(_fecha!)),
              ),
              const SizedBox(height: 12),
              TextFormField(
                key: const Key('dia_descripcion'),
                controller: _descripcion,
                decoration: const InputDecoration(labelText: 'Descripción', hintText: 'Día de asueto institucional'),
                validator: validarTexto,
              ),
              MensajeError(_error),
            ],
          ),
        ),
      ),
      actions: [
        if (editando)
          TextButton(
            onPressed: _guardando ? null : _darDeBaja,
            style: TextButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
            child: const Text('Quitar'),
          ),
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
        FilledButton(onPressed: _guardando ? null : _guardar, child: const Text('Guardar')),
      ],
    );
  }
}
