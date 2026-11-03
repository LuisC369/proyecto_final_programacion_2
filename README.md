# Pan, Puntos y Premios

Sistema de pedidos y fidelización para un restaurante de sándwiches.
Proyecto Final — Programación II, Universidad Mariano Gálvez de Guatemala.

## Descripción

Aplicación web en Java bajo el patrón MVC con persistencia en MySQL, que administra
clientes, productos, inventario, pedidos y pagos (efectivo y tarjeta), e implementa un
programa de puntos con acumulación y canje de recompensas. Incluye reportes en PDF y una
aplicación Android para consultar el saldo de puntos.

## Integrantes

| Nombre | Carné | Usuario GitHub |
|---|---|---|
| Luis Pablo de Jesus Lopez Carrera | 7690-26-6662 | [@LuisC369](https://github.com/LuisC369) |
| Bryan Alejandro Osoy Monterroso| 7690-25-15671 | [@bosoym-coder](https://github.com/bosoym-coder) | 
| Eliezer Isaí Vicente Vicente | 7690-24-7527 | [@isai-tech](https://github.com/isai-tech) |

## Tecnologías

- Java + Spring Boot (MVC) + Thymeleaf
- Bootstrap 5
- MySQL + JDBC (PreparedStatement)
- Android
## Requisitos
- JDK 17 o superior
- Apache NetBeans
- MySQL 8 con usuario `root` y contraseña `admin`
- Flutter
- Android Studio con un emulador de Android
## Base de datos
- Ejecutar en MySQL el script `base_datos/pan_puntos_premios.sql`.
## Aplicación web
- Abrir la carpeta `PanPuntosPremios` en NetBeans.
- Dar clic derecho sobre el proyecto y seleccionar Run.
- Entrar a http://localhost:8080
## Aplicación Android
- La aplicación web debe estar corriendo.
- Encender un emulador de Android.
- En una terminal, dentro de la carpeta `AppFlutter`, ejecutar:
```
flutter run
```
- Ingresar un identificador, por ejemplo `30214567`, y seleccionar Consultar.
## Diagramas
Los diagramas de clases y entidad-relación están en `docs/diagramas`.