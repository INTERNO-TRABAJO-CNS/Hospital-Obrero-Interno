import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../models/catalogo/horario.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// CU-02 · "Horarios de medicos" (Figma "ADMIN CATALOGO HORARIOS Y DIAS NO HABILES").
/// Cada fila agrupa los dias con la misma franja (ej. Lun–Vie 08:00–12:00).
class CatalogoHorariosView extends StatelessWidget {
  const CatalogoHorariosView({super.key, required this.controller});

  final CatalogoController controller;

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: controller,
      builder: (context, _) {
        final grupos = controller.gruposHorario;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Subtitulo('Horarios de médicos'),
            TablaCatalogo(
              columnas: const [
                ColumnaTabla('Médico', flex: 3),
                ColumnaTabla('Día', flex: 2),
                ColumnaTabla('Inicio', flex: 2),
                ColumnaTabla('Fin', flex: 2),
                ColumnaTabla('Cons.', flex: 3),
              ],
              filas: [
                for (final g in grupos)
                  [
                    Text(controller.nombreMedico(g.medicoId)),
                    Text(g.diasTexto),
                    Text(horaATexto(g.inicio)),
                    Text(horaATexto(g.fin)),
                    Text(controller.nombreConsultorio(g.consultorioId)),
                  ],
              ],
              alTocarFila: (i) => _abrirFormulario(context, grupos[i]),
              textoVacio: 'No hay horarios registrados',
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerLeft,
              child: FilledButton(
                key: const Key('horario_agregar'),
                onPressed: () => _abrirFormulario(context, null),
                child: const Text('Agregar horario'),
              ),
            ),
          ],
        );
      },
    );
  }

  Future<void> _abrirFormulario(BuildContext context, GrupoHorario? grupo) async {
    final guardado = await showDialog<String>(
      context: context,
      builder: (_) => _DialogoHorario(controller: controller, grupo: grupo),
    );
    if (guardado != null && context.mounted) {
      avisar(context, guardado);
    }
  }
}

class _DialogoHorario extends StatefulWidget {
  const _DialogoHorario({required this.controller, this.grupo});

  final CatalogoController controller;
  final GrupoHorario? grupo;

  @override
  State<_DialogoHorario> createState() => _DialogoHorarioState();
}

class _DialogoHorarioState extends State<_DialogoHorario> {
  int? _medicoId;
  int? _consultorioId;
  late Set<DiaSemana> _dias;
  TimeOfDay _inicio = const TimeOfDay(hour: 8, minute: 0);
  TimeOfDay _fin = const TimeOfDay(hour: 12, minute: 0);
  String? _error;
  bool _guardando = false;

  CatalogoController get _c => widget.controller;

  @override
  void initState() {
    super.initState();
    final g = widget.grupo;
    _medicoId = g?.medicoId;
    _consultorioId = g?.consultorioId;
    _dias = g?.dias ?? {DiaSemana.lunes, DiaSemana.martes, DiaSemana.miercoles, DiaSemana.jueves, DiaSemana.viernes};
    if (g != null) {
      _inicio = g.inicio;
      _fin = g.fin;
    }
  }

  Future<void> _elegirHora(bool esInicio) async {
    final hora = await showTimePicker(context: context, initialTime: esInicio ? _inicio : _fin);
    if (hora != null) {
      setState(() => esInicio ? _inicio = hora : _fin = hora);
    }
  }

  Future<void> _guardar() async {
    // Validacion de formato (las reglas las revisa el backend).
    if (_medicoId == null || _consultorioId == null || _dias.isEmpty) {
      setState(() => _error = 'Elija médico, consultorio y al menos un día.');
      return;
    }
    if (minutosDe(_inicio) >= minutosDe(_fin)) {
      setState(() => _error = 'La hora de inicio debe ser anterior a la de fin.');
      return;
    }
    setState(() => _guardando = true);
    final error = await _c.guardarGrupoHorario(
      anterior: widget.grupo,
      medicoId: _medicoId!,
      consultorioId: _consultorioId!,
      diasElegidos: _dias,
      inicio: _inicio,
      fin: _fin,
    );
    if (!mounted) {
      return;
    }
    if (error == null) {
      Navigator.pop(context, 'Horario guardado');
    } else {
      setState(() {
        _guardando = false;
        _error = error;
      });
    }
  }

  Future<void> _darDeBaja() async {
    final ok = await confirmar(context, 'Dar de baja', '¿Dar de baja este horario (${widget.grupo!.diasTexto})?');
    if (!ok || !mounted) {
      return;
    }
    final error = await _c.darDeBajaGrupoHorario(widget.grupo!);
    if (!mounted) {
      return;
    }
    if (error == null) {
      Navigator.pop(context, 'Horario dado de baja');
    } else {
      setState(() => _error = error);
    }
  }

  @override
  Widget build(BuildContext context) {
    final editando = widget.grupo != null;
    // En edicion se muestra tambien el medico/consultorio aunque ya este dado de baja.
    final medicos = [
      for (final m in _c.medicos)
        if (m.activo || m.id == _medicoId) m
    ];
    final consultorios = [
      for (final c in _c.consultorios)
        if (c.activo || c.id == _consultorioId) c
    ];
    return AlertDialog(
      title: Text(editando ? 'Editar horario' : 'Agregar horario'),
      content: SizedBox(
        width: 420,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              DropdownButtonFormField<int>(
                isExpanded: true,
                initialValue: _medicoId,
                decoration: const InputDecoration(labelText: 'Médico'),
                items: [
                  for (final m in medicos)
                    DropdownMenuItem(value: m.id, child: Text('${m.nombreCompleto} · ${m.especialidad.nombre}')),
                ],
                onChanged: (v) => setState(() => _medicoId = v),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(
                isExpanded: true,
                initialValue: _consultorioId,
                decoration: const InputDecoration(labelText: 'Consultorio'),
                items: [for (final c in consultorios) DropdownMenuItem(value: c.id, child: Text(c.nombre))],
                onChanged: (v) => setState(() => _consultorioId = v),
              ),
              const SizedBox(height: 16),
              Text('Días', style: Theme.of(context).textTheme.labelLarge),
              const SizedBox(height: 8),
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: [
                  for (final d in DiaSemana.values)
                    FilterChip(
                      label: Text(d.corto),
                      selected: _dias.contains(d),
                      onSelected: (s) => setState(() => s ? _dias.add(d) : _dias.remove(d)),
                    ),
                ],
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => _elegirHora(true),
                      icon: const Icon(Icons.schedule),
                      label: Text('Inicio ${horaATexto(_inicio)}'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => _elegirHora(false),
                      icon: const Icon(Icons.schedule),
                      label: Text('Fin ${horaATexto(_fin)}'),
                    ),
                  ),
                ],
              ),
              MensajeError(_error),
              const SizedBox(height: 8),
              Text('No se aplica si afecta citas programadas.', style: Theme.of(context).textTheme.bodySmall),
            ],
          ),
        ),
      ),
      actions: [
        if (editando)
          TextButton(
            onPressed: _guardando ? null : _darDeBaja,
            style: TextButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
            child: const Text('Dar de baja'),
          ),
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
        FilledButton(onPressed: _guardando ? null : _guardar, child: const Text('Guardar')),
      ],
    );
  }
}
