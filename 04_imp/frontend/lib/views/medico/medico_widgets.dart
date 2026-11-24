import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../models/catalogo/especialidad.dart';
import '../../models/comun/fechas_iso.dart';
import '../../models/medico/ficha_interna.dart';

/// Componentes de las pantallas del médico (mismo estilo que el Figma "WEB MEDICO").

/// Abre una pantalla del médico encima de la actual (el botón atrás vuelve).
void irA(BuildContext context, String ruta) => Navigator.of(context).pushNamed(ruta);

/// Abre una pantalla del médico y descarta las anteriores ("Volver a la agenda").
void volverA(BuildContext context, String ruta) => Navigator.of(context).pushNamedAndRemoveUntil(ruta, (_) => false);

/// "Jueves 12 de noviembre · 6 citas hoy" + botón de fecha + "Hoy".
class FilaFecha extends StatelessWidget {
  const FilaFecha(
      {super.key, required this.resumen, required this.fecha, required this.alCambiar, required this.alHoy});

  final String resumen;

  /// "2026-11-12".
  final String fecha;
  final ValueChanged<String> alCambiar;
  final VoidCallback alHoy;

  Future<void> _elegir(BuildContext context) async {
    final actual = fechaDeIso(fecha);
    final elegida = await showDatePicker(
      context: context,
      initialDate: actual,
      firstDate: DateTime(actual.year - 2),
      lastDate: DateTime(actual.year + 2),
      helpText: 'Elegir fecha',
    );
    if (elegida != null) {
      alCambiar(isoDe(elegida));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 10,
      runSpacing: 8,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        Text(resumen,
            key: const Key('resumen_fecha'), style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
        OutlinedButton.icon(
          key: const Key('elegir_fecha'),
          style: OutlinedButton.styleFrom(backgroundColor: ColoresCns.blanco, minimumSize: const Size(0, 38)),
          onPressed: () => _elegir(context),
          icon: const Icon(Icons.calendar_today, size: 16),
          label: Text(fechaCortaIso(fecha)),
        ),
        FilledButton(
          key: const Key('fecha_hoy'),
          style: FilledButton.styleFrom(minimumSize: const Size(0, 38)),
          onPressed: alHoy,
          child: const Text('Hoy'),
        ),
      ],
    );
  }
}

/// Fila "Etiqueta ........ valor" de las tarjetas.
class DatoFila extends StatelessWidget {
  const DatoFila(this.etiqueta, this.valor, {super.key, this.child});

  final String etiqueta;
  final String valor;

  /// Si se indica, se muestra en vez de [valor] (p. ej. un chip).
  final Widget? child;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 150,
            child: Text(etiqueta, style: const TextStyle(fontSize: 12.5, color: ColoresCns.textoSuave)),
          ),
          Expanded(
            child: child == null
                ? Text(valor, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700))
                : Align(alignment: Alignment.centerLeft, child: child),
          ),
        ],
      ),
    );
  }
}

/// Casilla verde del Figma (marcada: fondo verde con un cuadrado amarillo).
class Casilla extends StatelessWidget {
  const Casilla(
      {super.key, required this.valor, required this.texto, required this.alCambiar, this.deshabilitada = false});

  final bool valor;
  final String texto;
  final ValueChanged<bool> alCambiar;
  final bool deshabilitada;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      checked: valor,
      enabled: !deshabilitada,
      child: InkWell(
        onTap: deshabilitada ? null : () => alCambiar(!valor),
        borderRadius: BorderRadius.circular(6),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 6),
          child: Row(
            children: [
              Container(
                width: 20,
                height: 20,
                decoration: BoxDecoration(
                  color: valor ? ColoresCns.menu : ColoresCns.blanco,
                  border: Border.all(color: ColoresCns.menu, width: 2),
                  borderRadius: BorderRadius.circular(4),
                ),
                alignment: Alignment.center,
                child: valor
                    ? Container(
                        width: 10,
                        height: 10,
                        decoration: BoxDecoration(color: ColoresCns.amarillo, borderRadius: BorderRadius.circular(2)))
                    : null,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(texto,
                    style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: deshabilitada ? ColoresCns.textoSuave : ColoresCns.texto)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// ALTA / MEDIA / BAJA en tres botones.
class SelectorGravedad extends StatelessWidget {
  const SelectorGravedad({super.key, required this.valor, required this.alCambiar, this.deshabilitado = false});

  final Gravedad valor;
  final ValueChanged<Gravedad> alCambiar;
  final bool deshabilitado;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        for (final g in Gravedad.values) ...[
          Expanded(
            child: OutlinedButton(
              key: Key('gravedad_${g.codigo}'),
              onPressed: deshabilitado ? null : () => alCambiar(g),
              style: OutlinedButton.styleFrom(
                backgroundColor: g == valor ? ColoresCns.amarillo : ColoresCns.blanco,
                side: BorderSide(color: g == valor ? ColoresCns.menu : const Color(0xFFBBBBBB)),
                minimumSize: const Size(0, 38),
              ),
              child: Text(g.codigo,
                  style: TextStyle(fontSize: 13, fontWeight: g == valor ? FontWeight.w800 : FontWeight.w400)),
            ),
          ),
          if (g != Gravedad.values.last) const SizedBox(width: 8),
        ],
      ],
    );
  }
}

/// Lista de especialidades internas con la duración de su ficha. Terapia Intensiva solo con urgencia.
class SelectorEspecialidadInterna extends StatelessWidget {
  const SelectorEspecialidadInterna({
    super.key,
    required this.valor,
    required this.urgencia,
    required this.alCambiar,
    this.deshabilitado = false,
  });

  final Especialidad? valor;
  final bool urgencia;
  final ValueChanged<Especialidad> alCambiar;
  final bool deshabilitado;

  bool _habilitada(Especialidad e) => e != Especialidad.terapiaIntensiva || urgencia;

  String _detalle(Especialidad e) {
    if (e == Especialidad.terapiaIntensiva) {
      return urgencia ? 'habilitada por urgencia' : 'solo con urgencia';
    }
    return '${e.duracionMin}–${e.duracionMax} min';
  }

  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<Especialidad>(
      // Cambia de key cuando el controlador cambia el valor (p. ej. quita Terapia Intensiva).
      key: ValueKey('selector_especialidad_${valor?.codigo}_$urgencia'),
      initialValue: valor,
      isExpanded: true,
      hint: const Text('Elegir especialidad'),
      onChanged: deshabilitado
          ? null
          : (e) {
              if (e != null) {
                alCambiar(e);
              }
            },
      items: [
        for (final e in Especialidad.internas)
          DropdownMenuItem(
            value: e,
            enabled: _habilitada(e),
            child: Row(
              children: [
                Expanded(
                  child: Text(e.nombre,
                      style: TextStyle(color: _habilitada(e) ? ColoresCns.texto : ColoresCns.textoSuave)),
                ),
                Text(_detalle(e), style: const TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
              ],
            ),
          ),
      ],
    );
  }
}

/// Dos columnas (tarjeta principal y lateral); en pantallas angostas, una debajo de la otra.
class Columnas extends StatelessWidget {
  const Columnas({super.key, required this.principal, this.lateral});

  final Widget principal;
  final Widget? lateral;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, r) {
      final lado = lateral;
      if (lado == null) {
        return principal;
      }
      if (r.maxWidth < 820) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [principal, const SizedBox(height: 16), lado],
        );
      }
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [Expanded(flex: 3, child: principal), const SizedBox(width: 16), Expanded(flex: 2, child: lado)],
      );
    });
  }
}

/// Título de tarjeta.
class TituloTarjeta extends StatelessWidget {
  const TituloTarjeta(this.texto, {super.key, this.grande = false});

  final String texto;
  final bool grande;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(texto, style: TextStyle(fontSize: grande ? 18 : 15, fontWeight: FontWeight.w800)),
    );
  }
}

/// Texto gris chico.
class Ayuda extends StatelessWidget {
  const Ayuda(this.texto, {super.key, this.color = ColoresCns.textoSuave});

  final String texto;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 4, bottom: 4),
      child: Text(texto, style: TextStyle(fontSize: 11.5, color: color)),
    );
  }
}

/// Lista con viñetas.
class ListaPuntos extends StatelessWidget {
  const ListaPuntos(this.puntos, {super.key});

  final List<String> puntos;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        for (final p in puntos)
          Padding(
            padding: const EdgeInsets.only(bottom: 6),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('•  ', style: TextStyle(fontSize: 13)),
                Expanded(child: Text(p, style: const TextStyle(fontSize: 12.5))),
              ],
            ),
          ),
      ],
    );
  }
}

/// Mensaje de error en rojo.
class TextoError extends StatelessWidget {
  const TextoError(this.texto, {super.key});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Text(texto, style: const TextStyle(color: ColoresCns.peligro, fontWeight: FontWeight.w700)),
    );
  }
}

/// Mensaje de éxito en verde.
class TextoOk extends StatelessWidget {
  const TextoOk(this.texto, {super.key});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.symmetric(vertical: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: ColoresCns.filaPar, borderRadius: BorderRadius.circular(10)),
      child: Text(texto, style: const TextStyle(color: ColoresCns.menu, fontWeight: FontWeight.w700)),
    );
  }
}

/// Fila de botones que se acomoda en pantallas angostas.
class Botones extends StatelessWidget {
  const Botones(this.botones, {super.key});

  final List<Widget> botones;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: Wrap(spacing: 10, runSpacing: 10, children: botones),
    );
  }
}

String capitalizada(String s) => s.isEmpty ? s : '${s[0].toUpperCase()}${s.substring(1)}';
