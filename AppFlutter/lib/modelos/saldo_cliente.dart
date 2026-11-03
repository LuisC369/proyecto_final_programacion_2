import 'recompensa.dart';

class SaldoCliente {
  const SaldoCliente({
    required this.nombre,
    required this.puntos,
    required this.nivel,
    required this.activo,
    required this.recompensas,
  });

  final String nombre;
  final int puntos;
  final String nivel;
  final bool activo;
  final List<Recompensa> recompensas;

  factory SaldoCliente.desdeJson(Map<String, dynamic> json) {
    final lista = json['recompensas'] as List<dynamic>;
    return SaldoCliente(
      nombre: json['nombre'] as String,
      puntos: json['puntos'] as int,
      nivel: json['nivel'] as String,
      activo: json['activo'] as bool,
      recompensas: lista
          .map((item) => Recompensa.desdeJson(item as Map<String, dynamic>))
          .toList(),
    );
  }

  int puntosFaltantes(Recompensa recompensa) {
    final faltantes = recompensa.puntosRequeridos - puntos;
    return faltantes > 0 ? faltantes : 0;
  }
}
