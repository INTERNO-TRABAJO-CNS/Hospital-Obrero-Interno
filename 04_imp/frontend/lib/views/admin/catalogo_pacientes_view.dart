import 'package:flutter/material.dart';

import '../../controllers/pacientes_admin_controller.dart';
import '../../core/app_theme.dart';
import '../../models/comun/fechas.dart';
import '../../models/personal/cuenta.dart';
import '../../models/personal/paciente_admin.dart';
import '../comun/cns_widgets.dart';
import 'catalogo_widgets.dart';

/// Pacientes del administrador: tabla con búsqueda y, en "Gestionar", panel "Nuevo / editar paciente"
/// (mismo formato que el de médicos). Titular: se registra su afiliación y, si se quiere, su cuenta.
/// Beneficiario: usa la afiliación de un titular y no tiene cuenta propia.
class CatalogoPacientesView extends StatefulWidget {
  const CatalogoPacientesView({super.key, required this.controller, this.soloLectura = false});

  final PacientesAdminController controller;

  /// En "Catálogos" solo se consulta la tabla; crear, editar y dar de baja se hace en "Gestionar".
  final bool soloLectura;

  @override
  State<CatalogoPacientesView> createState() => _CatalogoPacientesViewState();
}

class _CatalogoPacientesViewState extends State<CatalogoPacientesView> {
  final _afiliado = TextEditingController();
  final _ci = TextEditingController();
  final _nombres = TextEditingController();
  final _apellidos = TextEditingController();
  final _usuario = TextEditingController();
  final _clave = TextEditingController();
  bool _titular = true;
  DateTime? _nacimiento;

  PacienteAdmin? _seleccionado;
  String? _error;
  bool _guardando = false;

  PacientesAdminController get _c => widget.controller;

  @override
  void dispose() {
    for (final c in [_afiliado, _ci, _nombres, _apellidos, _usuario, _clave]) {
      c.dispose();
    }
    super.dispose();
  }

  void _seleccionar(PacienteAdmin? p) {
    setState(() {
      _seleccionado = p;
      _error = null;
      _afiliado.text = p?.numeroAfiliado ?? '';
      _ci.text = p?.ci ?? '';
      _nombres.text = p?.nombres ?? '';
      _apellidos.text = p?.apellidos ?? '';
      _usuario.clear();
      _clave.clear();
      _titular = p?.titular ?? true;
      _nacimiento = p?.fechaNacimiento;
    });
  }

  Future<void> _elegirFecha() async {
    final hoy = DateTime.now();
    final elegida = await showDatePicker(
      context: context,
      initialDate: _nacimiento ?? DateTime(hoy.year - 30),
      firstDate: DateTime(1900),
      lastDate: hoy,
      helpText: 'Fecha de nacimiento',
    );
    if (elegida != null) {
      setState(() => _nacimiento = elegida);
    }
  }

  Future<void> _guardar() async {
    final datos = DatosPacienteAdmin(
      titular: _titular,
      numeroAfiliado: _afiliado.text,
      ci: _ci.text,
      nombres: _nombres.text,
      apellidos: _apellidos.text,
      fechaNacimiento: _nacimiento,
      usuario: _usuario.text,
      clave: _clave.text,
    );
    final editando = _seleccionado;
    setState(() => _guardando = true);
    final error = await _c.guardar(datos, pacienteId: editando?.pacienteId);
    if (!mounted) {
      return;
    }
    setState(() {
      _guardando = false;
      _error = error;
    });
    if (error == null) {
      avisar(context, editando == null ? 'Paciente registrado' : 'Paciente actualizado');
      _seleccionar(null);
    }
  }

  Future<void> _darDeBaja() async {
    final p = _seleccionado!;
    final ok = await confirmar(
        context,
        'Dar de baja',
        '¿Dar de baja a ${p.nombreCompleto}? Ya no podrá pedir citas${p.titular ? ' ni entrar a la app' : ''} '
            '(no se borra; se puede reactivar).');
    await _accion(ok, () => _c.darDeBaja(p.pacienteId), 'Paciente dado de baja');
  }

  Future<void> _reactivar() async {
    final p = _seleccionado!;
    final ok = await confirmar(context, 'Reactivar', '¿Reactivar a ${p.nombreCompleto}?');
    await _accion(ok, () => _c.reactivar(p.pacienteId), 'Paciente reactivado');
  }

  Future<void> _accion(bool ok, Future<String?> Function() accion, String exito) async {
    if (!ok || !mounted) {
      return;
    }
    final error = await accion();
    if (!mounted) {
      return;
    }
    setState(() => _error = error);
    if (error == null) {
      avisar(context, exito);
      _seleccionar(null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        if (_c.cargando && _c.pacientes.isEmpty) {
          return const Cargando();
        }
        return DosColumnas(principal: _tabla(), lateral: widget.soloLectura ? null : _panel());
      },
    );
  }

  Widget _tabla() {
    final lista = _c.filtrados;
    final i = lista.indexWhere((p) => p.pacienteId == _seleccionado?.pacienteId);
    final hoy = DateTime.now();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (_c.errorCarga != null) MensajeError(_c.errorCarga),
        Align(
          alignment: Alignment.centerRight,
          child: Container(
            width: 300,
            margin: const EdgeInsets.only(bottom: 10),
            child: TextField(
              key: const Key('paciente_buscar'),
              onChanged: _c.buscar,
              style: const TextStyle(fontSize: 13),
              decoration: InputDecoration(
                hintText: 'Buscar por nombre, CI o afiliación',
                prefixIcon: const Icon(Icons.search, size: 18),
                isDense: true,
                filled: true,
                fillColor: ColoresCns.blanco,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(20), borderSide: BorderSide.none),
              ),
            ),
          ),
        ),
        TablaCatalogo(
          columnas: const [
            ColumnaTabla('Paciente', flex: 3),
            ColumnaTabla('CI', flex: 2),
            ColumnaTabla('Afiliación', flex: 2),
            ColumnaTabla('Edad', flex: 1),
            ColumnaTabla('Tipo', flex: 2),
            ColumnaTabla('Cuenta', flex: 2),
            ColumnaTabla('Estado', flex: 2),
          ],
          filas: [
            for (final p in lista)
              [
                Text(p.nombreCompleto),
                Text(p.ci),
                Text(p.numeroAfiliado),
                Text('${p.edad(hoy)}'),
                ChipEstado(p.titular ? 'BASICA' : 'INTERNA', texto: p.titular ? 'TITULAR' : 'BENEFICIARIO'),
                Text(p.usuario ?? '—'),
                EstadoChip(activo: p.activo),
              ],
          ],
          seleccionada: i < 0 ? null : i,
          alTocarFila: widget.soloLectura ? null : (fila) => _seleccionar(lista[fila]),
          textoVacio: _c.busqueda.isEmpty ? 'No hay pacientes registrados' : 'Ningún paciente coincide con la búsqueda',
        ),
        NotaPie('${_c.pacientes.length} pacientes registrados. Los beneficiarios usan la afiliación de su titular y '
            'entran a la app con la cuenta del titular.'),
      ],
    );
  }

  Widget _panel() {
    final editando = _seleccionado;
    final nuevo = editando == null;
    final fecha = _nacimiento;
    return PanelCatalogo(
      titulo: 'Nuevo / editar paciente',
      subtitulo: nuevo ? null : 'Editando: ${editando.nombreCompleto}',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CampoCns(
            etiqueta: 'Tipo',
            child: SegmentedButton<bool>(
              key: const Key('paciente_tipo'),
              segments: const [
                ButtonSegment(value: true, label: Text('Titular')),
                ButtonSegment(value: false, label: Text('Beneficiario')),
              ],
              selected: {_titular},
              showSelectedIcon: false,
              onSelectionChanged: nuevo ? (s) => setState(() => _titular = s.first) : null,
            ),
          ),
          CampoCns(
            etiqueta: _titular ? 'N.º de afiliado' : 'N.º de afiliado del titular',
            ayuda: nuevo ? null : 'La afiliación y el CI no se cambian.',
            child: TextField(
              key: const Key('paciente_afiliado'),
              controller: _afiliado,
              enabled: nuevo,
              decoration: const InputDecoration(hintText: 'Ej. AF-10001'),
            ),
          ),
          CampoCns(
            etiqueta: 'CI',
            child: TextField(
              key: const Key('paciente_ci'),
              controller: _ci,
              enabled: nuevo,
              decoration: const InputDecoration(hintText: 'Ej. 4512301'),
            ),
          ),
          CampoCns(
            etiqueta: 'Nombres',
            child: TextField(
              key: const Key('paciente_nombres'),
              controller: _nombres,
              decoration: const InputDecoration(hintText: 'Ej. Juan Carlos'),
            ),
          ),
          CampoCns(
            etiqueta: 'Apellidos',
            child: TextField(
              key: const Key('paciente_apellidos'),
              controller: _apellidos,
              decoration: const InputDecoration(hintText: 'Ej. Choque Mamani'),
            ),
          ),
          CampoCns(
            etiqueta: 'Fecha de nacimiento',
            child: OutlinedButton.icon(
              key: const Key('paciente_fecha'),
              onPressed: _elegirFecha,
              icon: const Icon(Icons.cake_outlined, size: 18),
              label: Text(fecha == null ? 'Elegir fecha' : fechaNumerica(fecha)),
            ),
          ),
          if (nuevo && _titular) ...[
            CampoCns(
              etiqueta: 'Usuario (opcional)',
              ayuda: 'Si lo deja vacío, el titular crea su cuenta con "Registrarse".',
              child: TextField(
                key: const Key('paciente_usuario'),
                controller: _usuario,
                maxLength: usuarioMaximo,
                decoration: const InputDecoration(hintText: 'Ej. jchoque', counterText: ''),
              ),
            ),
            CampoCns(
              etiqueta: 'Contraseña inicial',
              ayuda: 'Mínimo $claveMinima caracteres. Entréguela en persona.',
              child: TextField(
                key: const Key('paciente_clave'),
                controller: _clave,
                obscureText: true,
                decoration: const InputDecoration(hintText: 'Contraseña'),
              ),
            ),
          ],
          if (!nuevo && editando.usuario != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text('Cuenta: ${editando.usuario}',
                  style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
            ),
          MensajeError(_error),
          FilledButton(
            key: const Key('paciente_guardar'),
            onPressed: _guardando ? null : _guardar,
            child: Text(_guardando ? 'Guardando…' : 'Guardar'),
          ),
          const SizedBox(height: 10),
          if (!nuevo && !editando.activo)
            OutlinedButton(
              key: const Key('paciente_reactivar'),
              onPressed: _reactivar,
              child: const Text('Reactivar'),
            )
          else
            BotonPeligro(
              key: const Key('paciente_baja'),
              texto: 'Dar de baja',
              alTocar: nuevo ? null : _darDeBaja,
            ),
          if (!nuevo) TextButton(onPressed: () => _seleccionar(null), child: const Text('Nuevo paciente')),
          const SizedBox(height: 6),
          const Text('No se aplica si tiene citas programadas.',
              style: TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
        ],
      ),
    );
  }
}
