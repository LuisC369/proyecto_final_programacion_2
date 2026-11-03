import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../modelos/recompensa.dart';
import '../modelos/saldo_cliente.dart';
import '../servicios/cliente_api.dart';

class PantallaSaldo extends StatefulWidget {
  const PantallaSaldo({super.key});

  @override
  State<PantallaSaldo> createState() => _PantallaSaldoState();
}

class _PantallaSaldoState extends State<PantallaSaldo> {
  final ClienteApi _api = const ClienteApi();
  final TextEditingController _identificador = TextEditingController();

  bool _cargando = false;
  String? _error;
  SaldoCliente? _saldo;

  @override
  void dispose() {
    _identificador.dispose();
    super.dispose();
  }

  Future<void> _consultar() async {
    final identificador = _identificador.text.trim();
    if (identificador.isEmpty) {
      setState(() {
        _saldo = null;
        _error = 'Ingrese su identificador.';
      });
      return;
    }

    FocusScope.of(context).unfocus();
    setState(() {
      _cargando = true;
      _saldo = null;
      _error = null;
    });

    SaldoCliente? saldo;
    String? error;
    try {
      saldo = await _api.consultarSaldo(identificador);
    } on ClienteNoEncontrado catch (e) {
      error = e.mensaje;
    } on FormatException {
      error = 'El servidor envió una respuesta que no se pudo leer.';
    } catch (_) {
      error = 'No se pudo conectar con el servidor. Verifique que esté encendido.';
    }

    if (!mounted) {
      return;
    }
    setState(() {
      _cargando = false;
      _saldo = saldo;
      _error = error;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colores = Theme.of(context).colorScheme;
    final saldo = _saldo;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: colores.primary,
        foregroundColor: colores.onPrimary,
        title: const Text('Pan, Puntos y Premios'),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _tarjeta(_formulario(colores)),
            if (saldo != null) ...[
              const SizedBox(height: 16),
              _tarjeta(_resumen(saldo, colores)),
              const SizedBox(height: 16),
              _tarjeta(_recompensas(saldo)),
            ],
          ],
        ),
      ),
    );
  }

  Widget _tarjeta(Widget contenido) {
    return Card(
      color: Colors.white,
      elevation: 0,
      child: Padding(padding: const EdgeInsets.all(16), child: contenido),
    );
  }

  Widget _formulario(ColorScheme colores) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          key: const Key('campoIdentificador'),
          controller: _identificador,
          keyboardType: TextInputType.number,
          textInputAction: TextInputAction.search,
          maxLength: 20,
          inputFormatters: [FilteringTextInputFormatter.digitsOnly],
          onSubmitted: (_) => _consultar(),
          decoration: const InputDecoration(
            labelText: 'Identificador (DPI / NIT)',
            border: OutlineInputBorder(),
            counterText: '',
          ),
        ),
        const SizedBox(height: 12),
        FilledButton(
          key: const Key('botonConsultar'),
          onPressed: _cargando ? null : _consultar,
          style: FilledButton.styleFrom(backgroundColor: colores.secondary),
          child: const Text('Consultar'),
        ),
        if (_cargando) ...[
          const SizedBox(height: 12),
          const Center(child: CircularProgressIndicator()),
        ],
        if (_error != null) ...[
          const SizedBox(height: 12),
          Text(_error!, style: TextStyle(color: colores.error)),
        ],
      ],
    );
  }

  Widget _resumen(SaldoCliente saldo, ColorScheme colores) {
    return Column(
      children: [
        Text(
          saldo.nombre,
          textAlign: TextAlign.center,
          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 8),
        const Text('Saldo de puntos'),
        Text(
          '${saldo.puntos}',
          style: TextStyle(
            fontSize: 52,
            fontWeight: FontWeight.bold,
            color: colores.secondary,
          ),
        ),
        Text(
          'Nivel ${saldo.nivel}',
          style: TextStyle(fontWeight: FontWeight.bold, color: colores.primary),
        ),
        Text(
          saldo.activo ? 'Activo' : 'Desactivado',
          style: TextStyle(color: saldo.activo ? Colors.green[800] : colores.error),
        ),
      ],
    );
  }

  Widget _recompensas(SaldoCliente saldo) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Recompensas',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        for (final recompensa in saldo.recompensas)
          ListTile(
            contentPadding: EdgeInsets.zero,
            title: Text(
              recompensa.nombre,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            subtitle: Text('${recompensa.puntosRequeridos} pts'),
            trailing: _estadoRecompensa(saldo, recompensa),
          ),
      ],
    );
  }

  Widget _estadoRecompensa(SaldoCliente saldo, Recompensa recompensa) {
    if (recompensa.alcanzable) {
      return Text('Puede canjear', style: TextStyle(color: Colors.green[800]));
    }
    final faltantes = saldo.puntosFaltantes(recompensa);
    return Text(faltantes > 0 ? 'Faltan $faltantes pts' : 'No disponible');
  }
}
