import 'package:flutter/material.dart';

import '../../controllers/catalogo_controller.dart';
import '../../core/app_theme.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/catalogo/medico.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// CU-02 · Catalogo de medicos (Figma "ADMIN CATALOGO MEDICOS v2"): tabla con el tipo
/// (BÁSICA / INTERNA) y panel "Nuevo / editar médico" con las 13 especialidades agrupadas.
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
        '¿Dar de baja a ${m.nombreCompleto}? Dejará de recibir citas nuevas y sus horarios se desactivan (no se borra).');
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
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: Container(
            margin: const EdgeInsets.only(bottom: 10),
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(color: ColoresCns.blanco, borderRadius: BorderRadius.circular(20)),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<Especialidad?>(
                key: const Key('medico_filtro'),
                value: _c.filtroEspecialidad,
                isDense: true,
                style: const TextStyle(fontSize: 12, color: ColoresCns.texto),
                hint: const Text('Todas las especialidades'),
                onChanged: _c.filtrarPorEspecialidad,
                items: [
                  const DropdownMenuItem(value: null, child: Text('Todas las especialidades')),
                  for (final e in Especialidad.values) DropdownMenuItem(value: e, child: Text(e.nombre)),
                ],
              ),
            ),
          ),
        ),
        TablaCatalogo(
          columnas: const [
            ColumnaTabla('Médico', flex: 3),
            ColumnaTabla('Especialidad', flex: 3),
            ColumnaTabla('Tipo', flex: 2),
            ColumnaTabla('Duración', flex: 2),
            ColumnaTabla('Cupo/día', flex: 2),
            ColumnaTabla('Estado', flex: 2),
          ],
          filas: [
            for (final m in lista)
              [
                Text(m.nombreCompleto),
                Text(m.especialidad.nombre),
                ChipEstado(m.especialidad.esBasica ? 'BASICA' : 'INTERNA', texto: m.especialidad.categoria.etiqueta),
                Text('${m.duracionCitaMin} min'),
                Text('${m.cupoMaximoDia}'),
                EstadoChip(activo: m.activo),
              ],
          ],
          seleccionada: i < 0 ? null : i,
          alTocarFila: (fila) => _seleccionar(lista[fila]),
          textoVacio: 'No hay médicos registrados',
        ),
        const NotaPie('Las 3 básicas las pide el paciente; las 10 internas solo se asignan por ficha interna. '
            'La duración debe estar en el rango de la especialidad y el cupo entre $cupoMinimo y $cupoMaximo.'),
      ],
    );
  }

  Widget _panel() {
    final editando = _seleccionado != null;
    return PanelCatalogo(
      titulo: 'Nuevo / editar médico',
      subtitulo: editando ? 'Editando: ${_seleccionado!.nombreCompleto}' : null,
      child: Form(
        key: _formulario,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            CampoCns(
              etiqueta: 'Nombres',
              child: TextFormField(
                key: const Key('medico_nombres'),
                controller: _nombres,
                decoration: const InputDecoration(hintText: 'Ej. Raúl'),
                validator: validarTexto,
              ),
            ),
            CampoCns(
              etiqueta: 'Apellidos',
              child: TextFormField(
                key: const Key('medico_apellidos'),
                controller: _apellidos,
                decoration: const InputDecoration(hintText: 'Ej. Vargas'),
                validator: validarTexto,
              ),
            ),
            CampoCns(
              etiqueta: 'Especialidad',
              child: DropdownButtonFormField<Especialidad>(
                key: ValueKey('medico_especialidad_$_version'),
                isExpanded: true,
                initialValue: _especialidad,
                items: _opcionesEspecialidad(),
                selectedItemBuilder: (_) => [
                  for (final e in _ordenOpciones)
                    e == null ? const SizedBox.shrink() : Text(e.nombre, style: const TextStyle(fontSize: 13)),
                ],
                onChanged: (e) => setState(() => _especialidad = e ?? _especialidad),
              ),
            ),
            CampoCns(
              etiqueta: 'Duración de la ficha (min)',
              ayuda: 'Rango de ${_especialidad.nombre}: ${_especialidad.duracionMin}–${_especialidad.duracionMax} min.',
              child: TextFormField(
                key: const Key('medico_duracion'),
                controller: _duracion,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(hintText: 'Ej. 40'),
                validator: validarEntero,
              ),
            ),
            CampoCns(
              etiqueta: 'Cupo por día',
              ayuda: 'Entre $cupoMinimo y $cupoMaximo fichas.',
              child: TextFormField(
                key: const Key('medico_cupo'),
                controller: _cupo,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(hintText: 'Ej. 7'),
                validator: validarEntero,
              ),
            ),
            MensajeError(_error),
            FilledButton(
              key: const Key('medico_guardar'),
              onPressed: _guardando ? null : _guardar,
              child: Text(_guardando ? 'Guardando…' : 'Guardar'),
            ),
            const SizedBox(height: 10),
            BotonPeligro(
              key: const Key('medico_baja'),
              texto: 'Dar de baja',
              alTocar: editando && _seleccionado!.activo ? _darDeBaja : null,
            ),
            if (editando) TextButton(onPressed: () => _seleccionar(null), child: const Text('Nuevo médico')),
            const SizedBox(height: 6),
            const Text('No se aplica si afecta citas programadas.',
                style: TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
          ],
        ),
      ),
    );
  }

  /// Orden del desplegable: encabezado BÁSICAS, las 3 basicas, encabezado INTERNAS, las 10 internas.
  /// `null` marca un encabezado.
  static final List<Especialidad?> _ordenOpciones = [
    null,
    ...Especialidad.basicas,
    null,
    ...Especialidad.internas,
  ];

  List<DropdownMenuItem<Especialidad>> _opcionesEspecialidad() {
    var grupo = 0;
    return [
      for (final e in _ordenOpciones)
        if (e == null)
          DropdownMenuItem<Especialidad>(
            enabled: false,
            child: Text(grupo++ == 0 ? 'BÁSICAS' : 'INTERNAS',
                style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w800, color: ColoresCns.menu)),
          )
        else
          DropdownMenuItem<Especialidad>(
            value: e,
            child: Row(
              children: [
                Expanded(child: Text(e.nombre, style: const TextStyle(fontSize: 13))),
                Text(e.rango, style: const TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
              ],
            ),
          ),
    ];
  }
}
