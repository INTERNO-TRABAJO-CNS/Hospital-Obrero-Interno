import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/catalogo/medico.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Catalogo de medicos (Figma COMPUTER "ADMIN CATALOGO MEDICOS"):
/// tabla a la izquierda y panel "Nuevo / editar medico" a la derecha.
class CatalogoMedicosView extends StatefulWidget {
  const CatalogoMedicosView({super.key, required this.controller});

  final CatalogoController controller;

  @override
  State<CatalogoMedicosView> createState() => _CatalogoMedicosViewState();
}

class _CatalogoMedicosViewState extends State<CatalogoMedicosView> {
  final _formulario = GlobalKey<FormState>();
  final _nombres = TextEditingController();
  final _apellidos = TextEditingController();
  final _duracion = TextEditingController();
  final _cupo = TextEditingController();
  Especialidad _especialidad = Especialidad.consultorioFamiliar;

  Medico? _seleccionado;

  /// Cambia en cada seleccion para que el desplegable tome el valor nuevo.
  int _version = 0;
  String? _error;
  bool _guardando = false;

  CatalogoController get _c => widget.controller;

  @override
  void dispose() {
    _nombres.dispose();
    _apellidos.dispose();
    _duracion.dispose();
    _cupo.dispose();
    super.dispose();
  }

  void _seleccionar(Medico? m) {
    setState(() {
      _seleccionado = m;
      _version++;
      _error = null;
      _nombres.text = m?.nombres ?? '';
      _apellidos.text = m?.apellidos ?? '';
      _especialidad = m?.especialidad ?? Especialidad.consultorioFamiliar;
      _duracion.text = m == null ? '' : '${m.duracionCitaMin}';
      _cupo.text = m == null ? '' : '${m.cupoMaximoDia}';
    });
  }

  Future<void> _guardar() async {
    if (!_formulario.currentState!.validate()) {
      return;
    }
    final medico = Medico(
      id: _seleccionado?.id ?? 0,
      nombres: _nombres.text.trim(),
      apellidos: _apellidos.text.trim(),
      especialidad: _especialidad,
      duracionCitaMin: int.parse(_duracion.text.trim()),
      cupoMaximoDia: int.parse(_cupo.text.trim()),
      activo: _seleccionado?.activo ?? true,
    );
    setState(() => _guardando = true);
    final error = await _c.guardarMedico(medico);
    if (!mounted) {
      return;
    }
    setState(() {
      _guardando = false;
      _error = error;
    });
    if (error == null) {
      avisar(context, medico.id == 0 ? 'Médico registrado' : 'Médico actualizado');
      _seleccionar(null);
    }
  }

  Future<void> _darDeBaja() async {
    final m = _seleccionado!;
    final ok = await confirmar(context, 'Dar de baja',
        '¿Dar de baja a ${m.nombreCompleto}? Dejará de recibir citas nuevas (no se borra).');
    if (!ok || !mounted) {
      return;
    }
    final error = await _c.darDeBajaMedico(m.id);
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    if (error == null) {
      avisar(context, 'Médico dado de baja');
      _seleccionar(null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) => DosColumnas(principal: _tabla(), lateral: _panel()),
    );
  }

  Widget _tabla() {
    final lista = _c.medicosFiltrados;
    final i = lista.indexWhere((m) => m.id == _seleccionado?.id);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: DropdownButton<Especialidad?>(
            value: _c.filtroEspecialidad,
            hint: const Text('Todas las especialidades'),
            onChanged: _c.filtrarPorEspecialidad,
            items: [
              const DropdownMenuItem(value: null, child: Text('Todas las especialidades')),
              for (final e in Especialidad.values) DropdownMenuItem(value: e, child: Text(e.nombre)),
            ],
          ),
        ),
        const SizedBox(height: 8),
        TablaCatalogo(
          columnas: const [
            ColumnaTabla('Médico', flex: 3),
            ColumnaTabla('Especialidad', flex: 3),
            ColumnaTabla('Duración', flex: 2),
            ColumnaTabla('Cupo/día', flex: 2),
            ColumnaTabla('Estado', flex: 3),
          ],
          filas: [
            for (final m in lista)
              [
                Text(m.nombreCompleto),
                Text(m.especialidad.nombre),
                Text('${m.duracionCitaMin} min'),
                Text('${m.cupoMaximoDia}'),
                EstadoChip(activo: m.activo),
              ],
          ],
          seleccionada: i < 0 ? null : i,
          alTocarFila: (fila) => _seleccionar(lista[fila]),
          textoVacio: 'No hay médicos registrados',
        ),
      ],
    );
  }

  Widget _panel() {
    final editando = _seleccionado != null;
    return PanelCatalogo(
      titulo: editando ? 'Editar médico' : 'Nuevo médico',
      child: Form(
        key: _formulario,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextFormField(
              key: const Key('medico_nombres'),
              controller: _nombres,
              decoration: const InputDecoration(labelText: 'Nombres', hintText: 'Dr. ...'),
              validator: validarTexto,
            ),
            const SizedBox(height: 12),
            TextFormField(
              key: const Key('medico_apellidos'),
              controller: _apellidos,
              decoration: const InputDecoration(labelText: 'Apellidos'),
              validator: validarTexto,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<Especialidad>(
              isExpanded: true,
              key: ValueKey('medico_especialidad_$_version'),
              initialValue: _especialidad,
              decoration: const InputDecoration(labelText: 'Especialidad'),
              items: [
                for (final e in Especialidad.values) DropdownMenuItem(value: e, child: Text(e.nombre)),
              ],
              onChanged: (e) => setState(() => _especialidad = e ?? _especialidad),
            ),
            const SizedBox(height: 12),
            TextFormField(
              key: const Key('medico_duracion'),
              controller: _duracion,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'Duración de ficha (min)',
                helperText: 'General 30–45 · Odonto 45–60 · Pediatría 30–45 (esta: ${_especialidad.rango})',
                helperMaxLines: 2,
              ),
              validator: validarEntero,
            ),
            const SizedBox(height: 12),
            TextFormField(
              key: const Key('medico_cupo'),
              controller: _cupo,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Cupo diario ($cupoMinimo a $cupoMaximo)'),
              validator: validarEntero,
            ),
            MensajeError(_error),
            const SizedBox(height: 16),
            FilledButton(
              key: const Key('medico_guardar'),
              onPressed: _guardando ? null : _guardar,
              child: Text(_guardando ? 'Guardando…' : 'Guardar'),
            ),
            const SizedBox(height: 8),
            OutlinedButton(
              key: const Key('medico_baja'),
              onPressed: editando && _seleccionado!.activo ? _darDeBaja : null,
              style: OutlinedButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
              child: const Text('Dar de baja'),
            ),
            if (editando)
              TextButton(onPressed: () => _seleccionar(null), child: const Text('Nuevo médico')),
            const SizedBox(height: 8),
            Text('No se aplica si afecta citas programadas.', style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}
