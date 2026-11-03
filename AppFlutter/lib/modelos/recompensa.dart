class Recompensa {
  const Recompensa({
    required this.nombre,
    required this.puntosRequeridos,
    required this.alcanzable,
  });

  final String nombre;
  final int puntosRequeridos;
  final bool alcanzable;

  factory Recompensa.desdeJson(Map<String, dynamic> json) {
    return Recompensa(
      nombre: json['nombre'] as String,
      puntosRequeridos: json['puntosRequeridos'] as int,
      alcanzable: json['alcanzable'] as bool,
    );
  }
}
