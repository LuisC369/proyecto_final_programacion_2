import 'dart:convert';

import 'package:http/http.dart' as http;

import '../modelos/saldo_cliente.dart';

class ClienteNoEncontrado implements Exception {
  const ClienteNoEncontrado(this.mensaje);

  final String mensaje;
}

class ClienteApi {
  const ClienteApi();

  static const String urlBase = 'http://10.0.2.2:8080';
  static const Duration tiempoEspera = Duration(seconds: 5);

  Future<SaldoCliente> consultarSaldo(String identificador) async {
    final url = Uri.parse(
      '$urlBase/api/clientes/${Uri.encodeComponent(identificador)}/puntos',
    );
    final respuesta = await http.get(url).timeout(tiempoEspera);
    final cuerpo = utf8.decode(respuesta.bodyBytes);

    if (respuesta.statusCode == 404) {
      final error = jsonDecode(cuerpo) as Map<String, dynamic>;
      throw ClienteNoEncontrado(error['mensaje'] as String);
    }
    if (respuesta.statusCode != 200) {
      throw http.ClientException('Código ${respuesta.statusCode}', url);
    }
    return SaldoCliente.desdeJson(jsonDecode(cuerpo) as Map<String, dynamic>);
  }
}
