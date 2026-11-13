import 'package:flutter/material.dart';

import '../../controllers/quirofanos_controller.dart';
import '../../core/api_client.dart';
import '../../core/app_theme.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/catalogo/horario.dart';
import '../../models/quirofano/quirofano.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// Figma "ADMIN CATALOGO QUIROFANOS": tabla y panel "Nuevo / editar quirófano". Los rechazos se
/// muestran en la caja roja con su codigo (p. ej. QUIROFANO_CON_CIRUGIAS).
class CatalogoQuirofanosView extends StatefulWidget {
  const CatalogoQuirofanosView({super.key, required this.controller});

  final QuirofanosController controller;

  @override
  State<CatalogoQuirofanosView> createState() => _CatalogoQuirofanosViewState();
}

class _CatalogoQuirofanosViewState extends State<CatalogoQuirofanosView> {
  final _nombre = TextEditingController();
  final _limpieza = TextEditingController(text: '30');
  Especialidad _especialidad = Especialidad.gastroenterologia;
  TimeOfDay _apertura = const TimeOfDay(hour: 7, minute: 0);
  TimeOfDay _cierre = const TimeOfDay(hour: 19, minute: 0);
  Quirofano? _seleccionado;
  ApiException? _error;
  bool _guardando = false;
  int _version = 0;

  QuirofanosController get _c => widget.controller;

  @override
  void dispose() {
    _nombre.dispose();
    _limpieza.dispose();
    super.dispose();
  }

  void _seleccionar(Quirofano? q) {
    setState(() {
      _seleccionado = q;
      _version++;
      _error = null;
      _nombre.text = q?.nombre ?? '';
      _limpieza.text = '${q?.limpiezaMin ?? 30}';
      _especialidad = q?.especialidad ?? Especialidad.gastroenterologia;
      _apertura = q?.apertura ?? const TimeOfDay(hour: 7, minute: 0);
      _cierre = q?.cierre ?? const TimeOfDay(hour: 19, minute: 0);
    });
  }

  Future<void> _elegirHora(bool esApertura) async {
    final hora = await showTimePicker(context: context, initialTime: esApertura ? _apertura : _cierre);
    if (hora != null) {
      setState(() => esApertura ? _apertura = hora : _cierre = hora);
    }
  }

  Future<void> _guardar() async {
    final limpieza = int.tryParse(_limpieza.text.trim());
    if (limpieza == null) {
      setState(() => _error = const ApiException('DATOS_INVALIDOS', 'Ingrese los minutos de limpieza.'));
      return;
    }
    final q = Quirofano(
      id: _seleccionado?.id ?? 0,
      nombre: _nombre.text.trim(),
      especialidad: _especialidad,
      apertura: _apertura,
      cierre: _cierre,
      limpiezaMin: limpieza,
      activo: _seleccionado?.activo ?? true,
    );
    setState(() => _guardando = true);
    final error = await _c.guardar(q);
    if (!mounted) {
      return;
    }
    setState(() {
      _guardando = false;
      _error = error;
    });
    if (error == null) {
      avisar(context, q.id == 0 ? 'Quirófano registrado' : 'Quirófano actualizado');
      _seleccionar(null);
    }
  }

  Future<void> _desactivar() async {
    final q = _seleccionado!;
    final ok = await confirmar(context, 'Desactivar quirófano', '¿Desactivar el quirófano ${q.nombre}? No se borra.');
    if (!ok || !mounted) {
      return;
    }
    final error = await _c.desactivar(q.id);
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    if (error == null) {
      avisar(context, 'Quirófano desactivado');
      _seleccionar(null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        if (_c.cargando && _c.quirofanos.isEmpty) {
          return const Cargando();
        }
        final lista = _c.quirofanos;
        final i = lista.indexWhere((q) => q.id == _seleccionado?.id);
        return DosColumnas(
          principal: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (_c.errorCarga != null) MensajeError(_c.errorCarga!.mensaje),
              TablaCatalogo(
                columnas: const [
                  ColumnaTabla('Quirófano', flex: 2),
                  ColumnaTabla('Especialidad', flex: 3),
                  ColumnaTabla('Horario', flex: 3),
                  ColumnaTabla('Limpieza', flex: 2),
                  ColumnaTabla('Cirugías prog.', flex: 2),
                  ColumnaTabla('Estado', flex: 2),
                ],
                filas: [
                  for (final q in lista)
                    [
                      Text(q.nombre),
                      Text(q.especialidad.nombre),
                      Text(q.horario),
                      Text('${q.limpiezaMin} min'),
                      Text('${q.cirugiasProgramadas}'),
                      EstadoChip(activo: q.activo),
                    ],
                ],
                seleccionada: i < 0 ? null : i,
                alTocarFila: (fila) => _seleccionar(lista[fila]),
                textoVacio: 'No hay quirófanos registrados',
              ),
              const NotaPie('Cada quirófano es de una sola especialidad (no se comparten). Horario por defecto '
                  '07:00–19:00; las urgencias pueden operarse fuera de horario. Cardiología, Nefrología, Neumología '
                  'y Endocrinología operan solo si hay tiempo (pendiente PV-20).'),
              const AvisoDatosPrueba(),
            ],
          ),
          lateral: _panel(),
        );
      },
    );
  }

  Widget _panel() {
    final editando = _seleccionado != null;
    return PanelCatalogo(
      titulo: 'Nuevo / editar quirófano',
      subtitulo: editando ? 'Editando: ${_seleccionado!.nombre}' : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CampoCns(
            etiqueta: 'Nombre',
            child: TextField(
              key: const Key('quirofano_nombre'),
              controller: _nombre,
              decoration: const InputDecoration(hintText: 'Ej. G-3'),
            ),
          ),
          CampoCns(
            etiqueta: 'Especialidad',
            ayuda: 'Solo especialidades que operan: Gastroenterología, Hematología y Dermatología.',
            child: DropdownButtonFormField<Especialidad>(
              key: ValueKey('quirofano_especialidad_$_version'),
              isExpanded: true,
              initialValue: _especialidad,
              items: [
                for (final e in Especialidad.queOperan) DropdownMenuItem(value: e, child: Text(e.nombre)),
              ],
              onChanged: (e) => setState(() => _especialidad = e ?? _especialidad),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: CampoCns(
                  etiqueta: 'Apertura',
                  child: _CampoHora(
                      key: const Key('quirofano_apertura'), hora: _apertura, alTocar: () => _elegirHora(true)),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: CampoCns(
                  etiqueta: 'Cierre',
                  child:
                      _CampoHora(key: const Key('quirofano_cierre'), hora: _cierre, alTocar: () => _elegirHora(false)),
                ),
              ),
            ],
          ),
          CampoCns(
            etiqueta: 'Limpieza después de cada cirugía (min)',
            ayuda: 'Entre $limpiezaMinima y $limpiezaMaxima min.',
            child: TextField(
              key: const Key('quirofano_limpieza'),
              controller: _limpieza,
              keyboardType: TextInputType.number,
            ),
          ),
          FilledButton(
            key: const Key('quirofano_guardar'),
            onPressed: _guardando ? null : _guardar,
            child: Text(_guardando ? 'Guardando…' : 'Guardar'),
          ),
          const SizedBox(height: 10),
          BotonPeligro(
            key: const Key('quirofano_desactivar'),
            texto: 'Desactivar',
            alTocar: editando && _seleccionado!.activo ? _desactivar : null,
          ),
          if (editando) TextButton(onPressed: () => _seleccionar(null), child: const Text('Nuevo quirófano')),
          if (_error != null) CajaError(codigo: _error!.codigo, mensaje: _error!.mensaje),
        ],
      ),
    );
  }
}

/// Caja gris con la hora; al tocarla se elige otra.
class _CampoHora extends StatelessWidget {
  const _CampoHora({super.key, required this.hora, required this.alTocar});

  final TimeOfDay hora;
  final VoidCallback alTocar;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: ColoresCns.entrada,
      borderRadius: BorderRadius.circular(8),
      child: InkWell(
        borderRadius: BorderRadius.circular(8),
        onTap: alTocar,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
          child: Text(horaATexto(hora), style: const TextStyle(fontSize: 13)),
        ),
      ),
    );
  }
}
