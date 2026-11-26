import 'package:flutter/material.dart';

import '../../controllers/solicitud_controller.dart';
import '../../models/auth/paciente.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/comun/fechas.dart';
import '../../models/medico/ficha_interna.dart' show edadPediatria;
import 'resultado_solicitud_view.dart';

/// CU-04 (RF-04, RU-01, RN-01): el paciente elige para quien es la cita (el titular o uno de sus beneficiarios,
/// RN-05), la especialidad y el dia. No elige medico, hora ni consultorio: los asigna el sistema.
class SolicitarCitaView extends StatefulWidget {
  const SolicitarCitaView({
    super.key,
    required this.controller,
    this.numeroAfiliado,
    this.alVolver,
  });

  final SolicitudController controller;

  /// Afiliacion del titular, para listar a sus beneficiarios.
  final String? numeroAfiliado;

  final VoidCallback? alVolver;

  @override
  State<SolicitarCitaView> createState() => _SolicitarCitaViewState();
}

class _SolicitarCitaViewState extends State<SolicitarCitaView> {
  Especialidad? especialidadSeleccionada;
  DateTime? diaSeleccionado;
  int? pacienteSeleccionado;

  SolicitudController get _c => widget.controller;

  @override
  void initState() {
    super.initState();
    _c.cargarOpciones(widget.numeroAfiliado);
  }

  Paciente? get _paciente {
    for (final p in _c.grupo) {
      if (p.pacienteId == pacienteSeleccionado) {
        return p;
      }
    }
    return null;
  }

  bool _especialidadPermitida(Especialidad e) =>
      e != Especialidad.pediatria || SolicitudController.puedePediatria(_paciente, diaSeleccionado);

  void _elegirPaciente(int pacienteId) {
    setState(() {
      pacienteSeleccionado = pacienteId;
      final elegida = especialidadSeleccionada;
      if (elegida != null && !_especialidadPermitida(elegida)) {
        especialidadSeleccionada = null;
      }
    });
  }

  void _elegirDia(DateTime dia) {
    setState(() {
      diaSeleccionado = dia;
      final elegida = especialidadSeleccionada;
      if (elegida != null && !_especialidadPermitida(elegida)) {
        especialidadSeleccionada = null;
      }
    });
  }

  void _aviso(String texto) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(texto)));
  }

  Future<void> _continuar() async {
    final especialidad = especialidadSeleccionada;
    final dia = diaSeleccionado;
    if (especialidad == null) {
      _aviso('Seleccione una especialidad.');
      return;
    }
    if (dia == null) {
      _aviso('Seleccione el día de atención.');
      return;
    }

    final paciente = _paciente;
    final resultado = await _c.solicitar(
      especialidad,
      dia,
      pacienteId: paciente == null || paciente.titular ? null : paciente.pacienteId,
    );

    if (!mounted) return;

    if (resultado == null) {
      _aviso(_c.error ?? 'No se pudo enviar la solicitud.');
      return;
    }

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ResultadoSolicitudView(resultado: resultado),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Solicitar cita'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: widget.alVolver ?? () => Navigator.of(context).pop(),
        ),
      ),
      body: ListenableBuilder(
        listenable: _c,
        builder: (context, _) {
          // Por defecto la cita es para el titular.
          if (pacienteSeleccionado == null && _c.grupo.isNotEmpty) {
            pacienteSeleccionado = _c.grupo.firstWhere((p) => p.titular, orElse: () => _c.grupo.first).pacienteId;
          }
          return Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 850),
              child: ListView(
                padding: const EdgeInsets.all(20),
                children: [
                  Text('Solicite una cita médica', style: Theme.of(context).textTheme.headlineSmall),
                  const SizedBox(height: 8),
                  Text(
                    'Elija la especialidad y el día. El sistema le asignará médico, hora y consultorio; '
                    'si ese día no hay cupo, seguirá buscando en los siguientes días hábiles y le avisará.',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  if (_c.grupo.length > 1) ...[
                    const SizedBox(height: 24),
                    const _Titulo('¿Para quién es la cita?'),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        for (final p in _c.grupo)
                          ChoiceChip(
                            key: Key('paciente_${p.pacienteId}'),
                            label: Text(p.titular ? '${p.nombreCompleto} (titular)' : p.nombreCompleto),
                            selected: p.pacienteId == pacienteSeleccionado,
                            onSelected: (_) => _elegirPaciente(p.pacienteId),
                          ),
                      ],
                    ),
                  ],
                  const SizedBox(height: 24),
                  const _Titulo('Seleccione una especialidad'),
                  for (final especialidad in Especialidad.basicas) _tarjetaEspecialidad(especialidad),
                  const SizedBox(height: 16),
                  const _Titulo('Seleccione el día'),
                  _dias(),
                  const SizedBox(height: 28),
                  SizedBox(
                    height: 52,
                    child: FilledButton(
                      key: const Key('solicitar_continuar'),
                      onPressed: _c.cargando ? null : _continuar,
                      child: _c.cargando
                          ? const SizedBox(height: 24, width: 24, child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text('Continuar', style: TextStyle(fontSize: 16)),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _tarjetaEspecialidad(Especialidad especialidad) {
    final colores = Theme.of(context).colorScheme;
    final seleccionada = especialidadSeleccionada == especialidad;
    final permitida = _especialidadPermitida(especialidad);

    final titulo = switch (especialidad) {
      Especialidad.consultorioFamiliar => 'Atención General',
      Especialidad.odontologia => 'Odontología',
      Especialidad.pediatria => 'Pediatría',
      _ => especialidad.nombre,
    };

    final icono = switch (especialidad) {
      Especialidad.consultorioFamiliar => Icons.medical_services,
      Especialidad.odontologia => Icons.health_and_safety,
      Especialidad.pediatria => Icons.child_care,
      _ => Icons.local_hospital,
    };

    return Card(
      key: Key('especialidad_${especialidad.codigo}'),
      margin: const EdgeInsets.only(bottom: 10),
      color: seleccionada ? colores.primaryContainer : colores.surface,
      child: ListTile(
        enabled: permitida,
        leading: Icon(icono, size: 32, color: permitida ? colores.primary : colores.outline),
        title: Text(titulo, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Text(permitida ? especialidad.rango : 'Solo para menores de $edadPediatria años'),
        trailing: Icon(
          seleccionada ? Icons.radio_button_checked : Icons.radio_button_unchecked,
          color: colores.primary,
        ),
        onTap: () => setState(() => especialidadSeleccionada = especialidad),
      ),
    );
  }

  Widget _dias() {
    if (_c.cargandoOpciones && _c.dias.isEmpty) {
      return const Padding(
        padding: EdgeInsets.all(12),
        child: Center(child: CircularProgressIndicator()),
      );
    }
    if (_c.errorOpciones != null && _c.dias.isEmpty) {
      return Row(
        children: [
          Expanded(
            child: Text(_c.errorOpciones!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
          ),
          TextButton(
            onPressed: () => _c.cargarOpciones(widget.numeroAfiliado),
            child: const Text('Reintentar'),
          ),
        ],
      );
    }
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        for (final dia in _c.dias)
          ChoiceChip(
            key: Key('dia_${dia.year}-${dia.month}-${dia.day}'),
            label: Text(_diaCorto(dia)),
            selected: diaSeleccionado != null && soloFecha(diaSeleccionado!) == soloFecha(dia),
            onSelected: (_) => _elegirDia(dia),
          ),
      ],
    );
  }
}

/// "Lun 23 nov".
String _diaCorto(DateTime f) {
  const dias = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  return '${dias[f.weekday - 1]} ${diaMesCorto(f)}';
}

class _Titulo extends StatelessWidget {
  const _Titulo(this.texto);

  final String texto;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Text(texto, style: Theme.of(context).textTheme.titleMedium),
      );
}
