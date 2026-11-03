import 'package:flutter/material.dart';

import 'pantallas/pantalla_saldo.dart';

void main() {
  runApp(const PanPuntosApp());
}

class PanPuntosApp extends StatelessWidget {
  const PanPuntosApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Pan, Puntos y Premios',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF5C3317),
          primary: const Color(0xFF5C3317),
          secondary: const Color(0xFFC97A2B),
        ),
        scaffoldBackgroundColor: const Color(0xFFFFF6EC),
        useMaterial3: true,
      ),
      home: const PantallaSaldo(),
    );
  }
}
