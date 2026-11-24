import 'package:flutter/material.dart';

import '../../core/app_theme.dart';
import '../../models/reportes/reporte.dart';

/// Componentes del Figma compartidos por las pantallas del personal (enfermeria y administrador).

/// Logo de la CNS en un cuadro blanco redondeado.
class LogoCns extends StatelessWidget {
  const LogoCns({super.key, this.tamano = 72});

  final double tamano;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(tamano * 0.22),
      child: Container(
        width: tamano,
        height: tamano,
        color: ColoresCns.blanco,
        child: Image.asset(
          'assets/logo_cns.jpg',
          fit: BoxFit.cover,
          semanticLabel: 'Logo de la CNS',
          errorBuilder: (_, __, ___) => Icon(Icons.local_hospital, color: ColoresCns.menu, size: tamano * 0.6),
        ),
      ),
    );
  }
}

/// Pildora blanca ("Enfermería · María Flores", "Hoy 13/11/2026 (fija)").
class Pildora extends StatelessWidget {
  const Pildora(this.texto, {super.key, this.color = ColoresCns.blanco, this.icono});

  final String texto;
  final Color color;
  final Widget? icono;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(20)),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icono != null) ...[icono!, const SizedBox(width: 6)],
          Flexible(
            child: Text(texto,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: ColoresCns.texto)),
          ),
        ],
      ),
    );
  }
}

/// "En vivo · se actualiza cada 15 s" con el punto verde.
class PildoraEnVivo extends StatelessWidget {
  const PildoraEnVivo({super.key, this.texto = 'En vivo · se actualiza cada 15 s'});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Pildora(
      texto,
      icono: Container(
        width: 8,
        height: 8,
        decoration: const BoxDecoration(color: ColoresCns.menu, shape: BoxShape.circle),
      ),
    );
  }
}

/// Como se pinta la opcion elegida en [FilaOpciones].
enum EstiloOpciones {
  /// Blanca y la elegida amarilla (chips de filtro).
  amarilla,

  /// Blanca y la elegida verde con texto blanco (pestañas de Supervisión).
  verde,
}

/// Fila de chips del Figma (Médicos / Consultorios / ..., Por estado / Por médico / ...).
class FilaOpciones<T> extends StatelessWidget {
  const FilaOpciones({
    super.key,
    required this.opciones,
    required this.elegida,
    required this.alElegir,
    this.estilo = EstiloOpciones.amarilla,
    this.deshabilitadas = const {},
  });

  final List<(T, String)> opciones;
  final T elegida;
  final ValueChanged<T> alElegir;
  final EstiloOpciones estilo;
  final Set<T> deshabilitadas;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        for (final (valor, texto) in opciones)
          _Opcion(
            texto: texto,
            activa: valor == elegida,
            estilo: estilo,
            alTocar: deshabilitadas.contains(valor) ? null : () => alElegir(valor),
          ),
      ],
    );
  }
}

class _Opcion extends StatelessWidget {
  const _Opcion({required this.texto, required this.activa, required this.estilo, required this.alTocar});

  final String texto;
  final bool activa;
  final EstiloOpciones estilo;
  final VoidCallback? alTocar;

  @override
  Widget build(BuildContext context) {
    final verde = estilo == EstiloOpciones.verde;
    final fondo = !activa ? ColoresCns.blanco : (verde ? ColoresCns.menu : ColoresCns.amarillo);
    final color = alTocar == null ? ColoresCns.textoSuave : (activa && verde ? ColoresCns.blanco : ColoresCns.texto);
    return Semantics(
      selected: activa,
      button: true,
      child: Material(
        color: fondo,
        borderRadius: BorderRadius.circular(20),
        child: InkWell(
          borderRadius: BorderRadius.circular(20),
          onTap: alTocar,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
            child: Text(
              texto,
              style: TextStyle(
                fontSize: 12,
                color: color,
                fontWeight: activa || verde ? FontWeight.w800 : FontWeight.w500,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Color de fondo de cada estado (los mismos del Figma y de la web).
Color colorDeEstado(String valor) {
  switch (valor) {
    case 'ATENDIDA':
      return ColoresCns.atendida;
    case 'ASISTENCIA_CONFIRMADA':
    case 'CONFIRMADA':
    case 'GENERAL':
      return ColoresCns.confirmada;
    case 'PROGRAMADA':
    case 'ASIGNADA':
    case 'ACTIVO':
      return ColoresCns.programada;
    case 'CANCELADA':
    case 'URGENCIA':
    case 'ALTA':
      return ColoresCns.cancelada;
    case 'INTERNA':
    case 'PEDIATRIA':
      return ColoresCns.interna;
    case 'MEDIA':
    case 'EN_ESPERA':
    case 'DESPLAZADA':
      return ColoresCns.media;
    case 'BAJA':
      return ColoresCns.atendida;
    default:
      return ColoresCns.basica;
  }
}

/// Chip de estado (ATENDIDA, PROGRAMADA, ACTIVO, BÁSICA, URGENCIA...).
class ChipEstado extends StatelessWidget {
  const ChipEstado(this.valor, {super.key, this.texto, this.anchoCompleto = false});

  /// Codigo que decide el color.
  final String valor;

  /// Texto visible (por defecto el mismo codigo).
  final String? texto;

  /// En las tablas de enfermeria y supervision el chip ocupa toda la columna.
  final bool anchoCompleto;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: anchoCompleto ? double.infinity : null,
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: colorDeEstado(valor), borderRadius: BorderRadius.circular(20)),
      child: Text(
        texto ?? valor,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800, color: ColoresCns.texto),
      ),
    );
  }
}

/// Columna de [TablaCns].
class ColumnaTabla {
  const ColumnaTabla(this.titulo, {this.flex = 1});

  final String titulo;
  final int flex;
}

/// Tabla del Figma: encabezado verde, filas alternadas y fila seleccionable o resaltada.
/// En ventanas angostas se desplaza a los costados en vez de apretar las columnas.
class TablaCns extends StatelessWidget {
  const TablaCns({
    super.key,
    required this.columnas,
    required this.filas,
    this.alTocarFila,
    this.seleccionada,
    this.resaltadas = const {},
    this.textoVacio = 'Sin registros',
    this.filaFija,
    this.anchoMinimo = 640,
  });

  final List<ColumnaTabla> columnas;
  final List<List<Widget>> filas;
  final ValueChanged<int>? alTocarFila;
  final int? seleccionada;

  /// Filas pintadas como la seleccionada (amarillo claro) sin estar elegidas.
  final Set<int> resaltadas;
  final String textoVacio;

  /// Fila informativa al inicio (no seleccionable), p. ej. "Sáb–Dom".
  final List<Widget>? filaFija;
  final double anchoMinimo;

  @override
  Widget build(BuildContext context) {
    const estiloEncabezado = TextStyle(color: ColoresCns.blanco, fontWeight: FontWeight.w800, fontSize: 12);
    const estiloCelda = TextStyle(color: ColoresCns.texto, fontSize: 12.5);

    Widget fila(List<Widget> celdas, {required Color color, VoidCallback? alTocar, Key? key}) {
      return Material(
        key: key,
        color: color,
        child: InkWell(
          onTap: alTocar,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 11),
            child: DefaultTextStyle.merge(
              style: estiloCelda,
              child: Row(
                children: [
                  for (var i = 0; i < columnas.length; i++)
                    Expanded(
                      flex: columnas[i].flex,
                      child: Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: Align(alignment: Alignment.centerLeft, child: celdas[i]),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      );
    }

    Color colorFila(int i) {
      if (i == seleccionada || resaltadas.contains(i)) {
        return ColoresCns.filaElegida;
      }
      return i.isOdd ? ColoresCns.filaPar : ColoresCns.blanco;
    }

    final tabla = ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Container(
            color: ColoresCns.menu,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                for (final c in columnas) Expanded(flex: c.flex, child: Text(c.titulo, style: estiloEncabezado)),
              ],
            ),
          ),
          if (filaFija != null) fila(filaFija!, color: ColoresCns.blanco),
          for (var i = 0; i < filas.length; i++)
            fila(
              filas[i],
              key: ValueKey('fila_$i'),
              color: filaFija == null ? colorFila(i) : colorFila(i + 1),
              alTocar: alTocarFila == null ? null : () => alTocarFila!(i),
            ),
          if (filas.isEmpty && filaFija == null)
            Container(
              color: ColoresCns.blanco,
              padding: const EdgeInsets.all(24),
              child: Center(child: Text(textoVacio, style: const TextStyle(color: ColoresCns.textoSuave))),
            ),
        ],
      ),
    );

    return LayoutBuilder(builder: (context, r) {
      if (!r.hasBoundedWidth || r.maxWidth >= anchoMinimo) {
        return tabla;
      }
      return SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: SizedBox(width: anchoMinimo, child: tabla),
      );
    });
  }
}

/// Tarjeta blanca redondeada.
class TarjetaBlanca extends StatelessWidget {
  const TarjetaBlanca({super.key, required this.child, this.padding = const EdgeInsets.all(18)});

  final Widget child;
  final EdgeInsets padding;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(color: ColoresCns.blanco, borderRadius: BorderRadius.circular(16)),
      child: child,
    );
  }
}

/// Indicador grande ("18 / Fichas hoy").
class TarjetaIndicador extends StatelessWidget {
  const TarjetaIndicador({super.key, required this.valor, required this.etiqueta});

  final String valor;
  final String etiqueta;

  @override
  Widget build(BuildContext context) {
    final largo = valor.length > 6;
    return TarjetaBlanca(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(valor, style: TextStyle(fontSize: largo ? 20 : 28, fontWeight: FontWeight.w800)),
          const SizedBox(height: 6),
          Text(etiqueta, style: const TextStyle(fontSize: 12, color: ColoresCns.textoSuave)),
        ],
      ),
    );
  }
}

/// Indicadores en una fila (en ventanas angostas, de a dos).
class FilaIndicadores extends StatelessWidget {
  const FilaIndicadores({super.key, required this.indicadores});

  final List<Widget> indicadores;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, r) {
      if (r.maxWidth >= 720) {
        return IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              for (var i = 0; i < indicadores.length; i++) ...[
                if (i > 0) const SizedBox(width: 16),
                Expanded(child: indicadores[i]),
              ],
            ],
          ),
        );
      }
      final ancho = (r.maxWidth - 12) / 2;
      return Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [for (final w in indicadores) SizedBox(width: ancho, child: w)],
      );
    });
  }
}

/// Texto chico al pie de una tabla.
class NotaPie extends StatelessWidget {
  const NotaPie(this.texto, {super.key});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: Text(texto, style: const TextStyle(fontSize: 11.5, color: ColoresCns.nota)),
    );
  }
}

/// Subtitulo en negrita ("Horarios de médicos", "Agenda por médico (solo lectura)").
class Subtitulo extends StatelessWidget {
  const Subtitulo(this.texto, {super.key});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(texto, style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800)),
    );
  }
}

/// Campo de formulario con la etiqueta en negrita arriba (estilo del Figma).
class CampoCns extends StatelessWidget {
  const CampoCns({super.key, required this.etiqueta, required this.child, this.ayuda});

  final String etiqueta;
  final Widget child;
  final String? ayuda;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(etiqueta, style: const TextStyle(fontSize: 12.5, fontWeight: FontWeight.w700)),
          const SizedBox(height: 6),
          child,
          if (ayuda != null) ...[
            const SizedBox(height: 5),
            Text(ayuda!, style: const TextStyle(fontSize: 11, color: ColoresCns.textoSuave)),
          ],
        ],
      ),
    );
  }
}

/// Caja roja con el codigo del rechazo ("QUIROFANO_CON_CIRUGIAS" + explicacion).
class CajaError extends StatelessWidget {
  const CajaError({super.key, required this.codigo, required this.mensaje});

  final String codigo;
  final String mensaje;

  @override
  Widget build(BuildContext context) {
    return Container(
      key: const Key('caja_error'),
      width: double.infinity,
      margin: const EdgeInsets.only(top: 12),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: ColoresCns.fondoError,
        border: Border.all(color: ColoresCns.peligro),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(codigo, style: const TextStyle(color: ColoresCns.peligro, fontWeight: FontWeight.w800, fontSize: 12)),
          const SizedBox(height: 3),
          Text(mensaje, style: const TextStyle(fontSize: 12)),
        ],
      ),
    );
  }
}

/// Boton gris con texto rojo ("Desactivar" del Figma).
class BotonPeligro extends StatelessWidget {
  const BotonPeligro({super.key, required this.texto, required this.alTocar});

  final String texto;
  final VoidCallback? alTocar;

  @override
  Widget build(BuildContext context) {
    return FilledButton(
      onPressed: alTocar,
      style: FilledButton.styleFrom(
        backgroundColor: ColoresCns.entrada,
        foregroundColor: ColoresCns.peligro,
        disabledForegroundColor: ColoresCns.textoSuave,
      ),
      child: Text(texto),
    );
  }
}

/// Grafico de barras horizontales en una tarjeta blanca (Reportes e indicadores).
class GraficoBarras extends StatelessWidget {
  const GraficoBarras({super.key, required this.titulo, required this.filas, required this.maximo});

  final String titulo;
  final List<FilaGrafico> filas;
  final num maximo;

  @override
  Widget build(BuildContext context) {
    return TarjetaBlanca(
      child: LayoutBuilder(builder: (context, r) {
        final angosto = r.maxWidth < 560;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(titulo, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800)),
            const SizedBox(height: 12),
            for (final f in filas)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    SizedBox(
                      width: angosto ? 96 : 150,
                      child: Text(f.etiqueta, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
                    ),
                    Expanded(
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: SizedBox(
                          height: 16,
                          child: Stack(
                            children: [
                              Container(color: ColoresCns.filaPar),
                              FractionallySizedBox(
                                widthFactor: maximo <= 0 ? 0 : (f.valor / maximo).clamp(0, 1).toDouble(),
                                child: Container(color: f.alerta ? ColoresCns.peligro : ColoresCns.menu),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    SizedBox(
                      width: angosto ? 92 : 170,
                      child: Padding(
                        padding: const EdgeInsets.only(left: 10),
                        child: Text(
                          f.texto,
                          style: TextStyle(
                            fontSize: 12,
                            color: f.alerta ? ColoresCns.peligro : ColoresCns.texto,
                            fontWeight: f.alerta ? FontWeight.w800 : FontWeight.w400,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        );
      }),
    );
  }
}

/// Aviso de las pantallas que todavia trabajan con datos de prueba (sin backend).
class AvisoDatosPrueba extends StatelessWidget {
  const AvisoDatosPrueba({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.only(top: 6),
      child: Text(
        'Datos de prueba: esta pantalla todavía no tiene backend.',
        style: TextStyle(fontSize: 11, fontStyle: FontStyle.italic, color: ColoresCns.nota),
      ),
    );
  }
}

/// Indicador de carga centrado.
class Cargando extends StatelessWidget {
  const Cargando({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(32),
      child: Center(child: CircularProgressIndicator(color: ColoresCns.menu)),
    );
  }
}

/// Campo de busqueda blanco (las cajas sobre el fondo verde).
InputDecoration decoracionBlanca(String pista, {Widget? icono}) => InputDecoration(
      hintText: pista,
      fillColor: ColoresCns.blanco,
      prefixIcon: icono,
    );

/// Mensaje corto abajo de la pantalla.
void mostrarAviso(BuildContext context, String texto) {
  ScaffoldMessenger.maybeOf(context)?.showSnackBar(SnackBar(content: Text(texto)));
}
