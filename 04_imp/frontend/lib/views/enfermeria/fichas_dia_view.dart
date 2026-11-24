import 'package:flutter/material.dart';

import '../../controllers/enfermeria_controller.dart';
import '../../core/app_theme.dart';
import '../../models/comun/fechas.dart';
import '../../models/enfermeria/ficha_dia.dart';
import '../../services/enfermeria_service.dart';
import '../comun/cns_widgets.dart';
import 'ticket_cita_dialog.dart';

/// Figma "ENFERMERIA FICHAS DEL DIA": todas las citas comunes de hoy, con busqueda por
/// paciente, medico, estado o consultorio. Tocar una ficha abre su ticket para imprimir.
class FichasDiaView extends StatefulWidget {
  const FichasDiaView({super.key, required this.servicio, required this.usuario, this.reloj});

  final EnfermeriaService servicio;
  final String usuario;
  final DateTime Function()? reloj;

  @override
  State<FichasDiaView> createState() => _FichasDiaViewState();
}

class _FichasDiaViewState extends State<FichasDiaView> {
  late final EnfermeriaController _c =
      EnfermeriaController(widget.servicio, usuario: widget.usuario, reloj: widget.reloj);
  final _busqueda = TextEditingController();

  @override
  void initState() {
    super.initState();
    _c.cargar();
  }

  @override
  void dispose() {
    _c.dispose();
    _busqueda.dispose();
    super.dispose();
  }

  void _abrir(FichaDelDia f) {
    if (f.estado == EstadoCita.cancelada) {
      mostrarAviso(context, 'La ficha ${f.numeroFicha} de ${f.medico} está cancelada: no se imprime ticket.');
      return;
    }
    mostrarTicketCita(context, f);
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _c,
      builder: (context, _) {
        final lista = _c.filtradas;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _barra(),
            const SizedBox(height: 16),
            if (_c.error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(_c.error!, style: const TextStyle(color: ColoresCns.peligro)),
              ),
            if (_c.cargando && _c.fichas.isEmpty)
              const Cargando()
            else
              TablaCns(
                columnas: const [
                  ColumnaTabla('Ficha'),
                  ColumnaTabla('Hora'),
                  ColumnaTabla('Paciente', flex: 3),
                  ColumnaTabla('Especialidad', flex: 3),
                  ColumnaTabla('Médico', flex: 2),
                  ColumnaTabla('Consultorio', flex: 2),
                  ColumnaTabla('Estado', flex: 4),
                ],
                filas: [
                  for (final f in lista)
                    [
                      Text('${f.numeroFicha}'),
                      Text(horaTexto(f.inicio)),
                      Text(f.paciente),
                      Text(f.especialidad.nombre),
                      Text(f.medico),
                      Text(f.consultorio),
                      ChipEstado(f.estado.codigo, anchoCompleto: true),
                    ],
                ],
                alTocarFila: (i) => _abrir(lista[i]),
                textoVacio: _c.texto.isEmpty ? 'No hay fichas para hoy' : 'Ninguna ficha coincide con la búsqueda',
                anchoMinimo: 760,
              ),
            const NotaPie('Solo se muestran las fichas del día actual. Consulta de solo lectura: toca una ficha '
                'para imprimir su ticket.'),
            const AvisoDatosPrueba(),
          ],
        );
      },
    );
  }

  Widget _barra() {
    final chips = FilaOpciones<CampoBusqueda>(
      opciones: [for (final c in CampoBusqueda.values) (c, c.titulo)],
      elegida: _c.campo,
      alElegir: _c.elegirCampo,
    );
    final campo = TextField(
      key: const Key('buscar_fichas'),
      controller: _busqueda,
      onChanged: _c.buscar,
      decoration: decoracionBlanca(_c.campo.pista, icono: const Icon(Icons.search, size: 18)),
    );
    final fecha = Pildora('Hoy ${fechaNumerica(_c.ahora)} (fija)', color: ColoresCns.entrada);
    return LayoutBuilder(builder: (context, r) {
      if (r.maxWidth >= 900) {
        return Row(
          children: [
            chips,
            const SizedBox(width: 12),
            Expanded(child: campo),
            const SizedBox(width: 12),
            fecha,
          ],
        );
      }
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [chips, const SizedBox(height: 10), campo, const SizedBox(height: 10), fecha],
      );
    });
  }
}
