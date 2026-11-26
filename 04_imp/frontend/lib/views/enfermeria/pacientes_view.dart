import 'package:flutter/material.dart';

import '../../controllers/pacientes_enfermeria_controller.dart';
import '../../core/api_client.dart';
import '../../core/app_theme.dart';
import '../../models/auth/paciente.dart';
import '../../models/comun/fechas.dart';
import '../../services/paciente_service.dart';
import '../comun/cns_widgets.dart';

/// CU-03 (S3-12) para enfermeria / admision: buscar al paciente por numero de afiliado (y CI). Si existe se
/// abre su ficha; si no, se registra con la afiliacion del titular, sin crear duplicados. Mismo estilo que las
/// otras pantallas de enfermeria del Figma (cajas blancas sobre fondo verde, tabla verde y tarjetas).
class PacientesView extends StatefulWidget {
  const PacientesView({super.key, required this.servicio, this.reloj});

  final PacienteService servicio;
  final DateTime Function()? reloj;

  @override
  State<PacientesView> createState() => _PacientesViewState();
}

class _PacientesViewState extends State<PacientesView> {
  late final PacientesEnfermeriaController _c = PacientesEnfermeriaController(widget.servicio, reloj: widget.reloj);
  final _afiliado = TextEditingController();
  final _ci = TextEditingController();

  // Formulario de registro.
  bool _registrando = false;
  final _fAfiliado = TextEditingController();
  final _fCi = TextEditingController();
  final _fNombres = TextEditingController();
  final _fApellidos = TextEditingController();
  DateTime? _fNacimiento;
  String? _errorFormulario;

  /// "Paciente registrado" / "Ya estaba registrado".
  String? _mensaje;

  @override
  void dispose() {
    _c.dispose();
    for (final t in [_afiliado, _ci, _fAfiliado, _fCi, _fNombres, _fApellidos]) {
      t.dispose();
    }
    super.dispose();
  }

  Future<void> _buscar() async {
    setState(() {
      _registrando = false;
      _mensaje = null;
    });
    await _c.buscar(_afiliado.text, ci: _ci.text);
  }

  void _abrirFormulario() {
    setState(() {
      _registrando = true;
      _mensaje = null;
      _errorFormulario = null;
      _fAfiliado.text = _c.afiliadoBuscado;
      _fCi.text = _c.ciBuscado;
      _fNombres.clear();
      _fApellidos.clear();
      _fNacimiento = null;
    });
  }

  Future<void> _elegirNacimiento() async {
    final hoy = widget.reloj?.call() ?? DateTime.now();
    final fecha = await showDatePicker(
      context: context,
      firstDate: DateTime(1900),
      lastDate: DateTime(hoy.year, hoy.month, hoy.day),
      initialDate: _fNacimiento ?? DateTime(hoy.year - 30, 1, 1),
    );
    if (fecha != null) {
      setState(() => _fNacimiento = fecha);
    }
  }

  Future<void> _registrar() async {
    final problema = _c.validar(
      afiliado: _fAfiliado.text,
      ci: _fCi.text,
      nombres: _fNombres.text,
      apellidos: _fApellidos.text,
      nacimiento: _fNacimiento,
    );
    if (problema != null) {
      setState(() => _errorFormulario = problema);
      return;
    }
    try {
      final r = await _c.registrar(RegistroBeneficiario(
        numeroAfiliadoTitular: _fAfiliado.text,
        ci: _fCi.text,
        nombres: _fNombres.text,
        apellidos: _fApellidos.text,
        fechaNacimiento: _fNacimiento!,
      ));
      if (!mounted) {
        return;
      }
      setState(() {
        _registrando = false;
        _afiliado.text = r.paciente.numeroAfiliado;
        _ci.text = r.paciente.ci;
        _mensaje = r.yaExistia
            ? 'El paciente ya estaba registrado: se muestra su ficha y no se creó otro registro.'
            : 'Paciente registrado. Ya se le pueden gestionar citas.';
      });
    } on ApiException catch (e) {
      if (mounted) {
        setState(() => _errorFormulario = e.mensaje);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _barra(),
          const SizedBox(height: 16),
          if (_c.error != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
            ),
          if (_mensaje != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Container(
                key: const Key('pacientes_mensaje'),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(color: ColoresCns.filaElegida, borderRadius: BorderRadius.circular(10)),
                child: Text(_mensaje!, style: const TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
          if (_registrando) _formulario() else _resultados(),
          const NotaPie('Busque siempre antes de registrar: el sistema no permite dos pacientes con la misma '
              'afiliación y CI. El titular crea su propia cuenta desde la app.'),
          if (widget.servicio is FakePacienteService) const AvisoDatosPrueba(),
        ],
      ),
    );
  }

  Widget _barra() {
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        SizedBox(
          width: 220,
          child: TextField(
            key: const Key('pacientes_afiliado'),
            controller: _afiliado,
            onSubmitted: (_) => _buscar(),
            decoration: decoracionBlanca('N° de afiliado', icono: const Icon(Icons.badge_outlined, size: 18)),
          ),
        ),
        SizedBox(
          width: 170,
          child: TextField(
            key: const Key('pacientes_ci'),
            controller: _ci,
            onSubmitted: (_) => _buscar(),
            decoration: decoracionBlanca('CI (opcional)'),
          ),
        ),
        FilledButton(
          key: const Key('pacientes_buscar'),
          onPressed: _c.cargando ? null : _buscar,
          child: Text(_c.cargando ? 'Buscando…' : 'Buscar'),
        ),
      ],
    );
  }

  Widget _resultados() {
    if (!_c.buscado) {
      return const TarjetaBlanca(
        child: Text('Ingrese el número de afiliado del paciente (y su CI si es beneficiario) y pulse Buscar.'),
      );
    }
    if (_c.cargando && _c.resultados.isEmpty) {
      return const Cargando();
    }
    if (_c.resultados.isEmpty) {
      if (_c.error != null) {
        return const SizedBox.shrink();
      }
      return TarjetaBlanca(
        key: const Key('pacientes_sin_resultado'),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'No hay ningún paciente registrado con la afiliación ${_c.afiliadoBuscado}'
              '${_c.ciBuscado.isEmpty ? '' : ' y CI ${_c.ciBuscado}'}.',
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 12),
            FilledButton.icon(
              key: const Key('pacientes_registrar'),
              onPressed: _abrirFormulario,
              icon: const Icon(Icons.person_add_alt_1, size: 18),
              label: const Text('Registrar paciente'),
            ),
          ],
        ),
      );
    }

    final tabla = Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TablaCns(
          columnas: const [
            ColumnaTabla('Afiliado', flex: 2),
            ColumnaTabla('CI', flex: 2),
            ColumnaTabla('Nombre', flex: 4),
            ColumnaTabla('Tipo', flex: 3),
            ColumnaTabla('Edad'),
          ],
          filas: [
            for (final p in _c.resultados)
              [
                Text(p.numeroAfiliado),
                Text(p.ci),
                Text(p.nombreCompleto),
                ChipEstado(p.titular ? 'ACTIVO' : 'INTERNA', texto: p.titular ? 'TITULAR' : 'BENEFICIARIO'),
                Text('${_c.edadDe(p)}'),
              ],
          ],
          seleccionada: _c.elegido == null ? null : _c.resultados.indexOf(_c.elegido!),
          alTocarFila: (i) => _c.elegir(_c.resultados[i]),
          anchoMinimo: 560,
        ),
        const SizedBox(height: 10),
        Align(
          alignment: Alignment.centerLeft,
          child: OutlinedButton.icon(
            key: const Key('pacientes_registrar_otro'),
            style: OutlinedButton.styleFrom(backgroundColor: ColoresCns.blanco),
            onPressed: _abrirFormulario,
            icon: const Icon(Icons.person_add_alt_1, size: 18),
            label: const Text('Registrar otro beneficiario de esta afiliación'),
          ),
        ),
      ],
    );
    final elegido = _c.elegido;
    final ficha = elegido == null
        ? const TarjetaBlanca(child: Text('Toque un paciente para ver su ficha.'))
        : _Ficha(paciente: elegido, edad: _c.edadDe(elegido));
    return LayoutBuilder(builder: (context, r) {
      if (r.maxWidth >= 900) {
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [Expanded(child: tabla), const SizedBox(width: 18), SizedBox(width: 320, child: ficha)],
        );
      }
      return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch, children: [tabla, const SizedBox(height: 14), ficha]);
    });
  }

  Widget _formulario() {
    Widget campo(String etiqueta, TextEditingController t, String clave, {String? ayuda}) => CampoCns(
          etiqueta: etiqueta,
          ayuda: ayuda,
          child: TextField(key: Key(clave), controller: t, decoration: decoracionBlanca(etiqueta)),
        );
    return Align(
      alignment: Alignment.topLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 560),
        child: TarjetaBlanca(
          key: const Key('pacientes_formulario'),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Subtitulo('Registrar paciente'),
              campo('Afiliación del titular', _fAfiliado, 'form_afiliado',
                  ayuda: 'Los beneficiarios usan la afiliación de su titular.'),
              campo('CI', _fCi, 'form_ci'),
              campo('Nombres', _fNombres, 'form_nombres'),
              campo('Apellidos', _fApellidos, 'form_apellidos'),
              CampoCns(
                etiqueta: 'Fecha de nacimiento',
                child: Material(
                  color: ColoresCns.entrada,
                  borderRadius: BorderRadius.circular(8),
                  child: InkWell(
                    key: const Key('form_nacimiento'),
                    borderRadius: BorderRadius.circular(8),
                    onTap: _elegirNacimiento,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 13),
                      child: Text(
                        _fNacimiento == null ? 'DD / MM / AAAA' : fechaNumerica(_fNacimiento!),
                        style: TextStyle(color: _fNacimiento == null ? ColoresCns.textoSuave : ColoresCns.texto),
                      ),
                    ),
                  ),
                ),
              ),
              if (_errorFormulario != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Text(_errorFormulario!,
                      key: const Key('form_error'), style: const TextStyle(color: ColoresCns.peligro)),
                ),
              Row(
                children: [
                  Expanded(
                    child: FilledButton(
                      onPressed: _c.registrando ? null : () => setState(() => _registrando = false),
                      style: FilledButton.styleFrom(
                          backgroundColor: ColoresCns.entrada, foregroundColor: ColoresCns.texto),
                      child: const Text('Cancelar'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FilledButton(
                      key: const Key('form_registrar'),
                      onPressed: _c.registrando ? null : _registrar,
                      child: Text(_c.registrando ? 'Registrando…' : 'Registrar'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Ficha del paciente encontrado o recien registrado.
class _Ficha extends StatelessWidget {
  const _Ficha({required this.paciente, required this.edad});

  final Paciente paciente;
  final int edad;

  @override
  Widget build(BuildContext context) {
    Widget dato(String etiqueta, String valor) => Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Row(
            children: [
              Text(etiqueta, style: const TextStyle(color: ColoresCns.textoSuave, fontSize: 13)),
              const SizedBox(width: 12),
              Expanded(
                child: Text(valor,
                    textAlign: TextAlign.right, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13.5)),
              ),
            ],
          ),
        );
    return TarjetaBlanca(
      key: Key('ficha_paciente_${paciente.pacienteId}'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Ficha del paciente',
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: ColoresCns.textoSuave)),
          const SizedBox(height: 8),
          Text(paciente.nombreCompleto, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          dato('Afiliación', paciente.numeroAfiliado),
          dato('CI', paciente.ci),
          dato('Tipo', paciente.titular ? 'Titular' : 'Beneficiario'),
          dato('Nacimiento', fechaNumerica(paciente.fechaNacimiento)),
          dato('Edad', '$edad años'),
        ],
      ),
    );
  }
}
