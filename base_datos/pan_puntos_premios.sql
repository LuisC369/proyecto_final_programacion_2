DROP DATABASE IF EXISTS pan_puntos_premios;
CREATE DATABASE pan_puntos_premios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pan_puntos_premios;

CREATE TABLE clientes (
    id_cliente      INT AUTO_INCREMENT PRIMARY KEY,
    identificador   VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(60)  NOT NULL,
    apellido        VARCHAR(60)  NOT NULL,
    telefono        VARCHAR(15),
    email           VARCHAR(100),
    puntos          INT          NOT NULL DEFAULT 0,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clientes_identificador UNIQUE (identificador),
    CONSTRAINT chk_clientes_puntos CHECK (puntos >= 0)
);

CREATE TABLE productos (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    codigo       VARCHAR(10)   NOT NULL,
    nombre       VARCHAR(80)   NOT NULL,
    categoria    ENUM('SANDWICH', 'BEBIDA', 'ACOMPANAMIENTO', 'MENU') NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    existencia   INT           NOT NULL DEFAULT 0,
    activo       TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT uq_productos_codigo UNIQUE (codigo),
    CONSTRAINT chk_productos_precio CHECK (precio >= 0),
    CONSTRAINT chk_productos_existencia CHECK (existencia >= 0)
);

CREATE TABLE menu_componentes (
    id_menu            INT PRIMARY KEY,
    id_sandwich        INT NOT NULL,
    id_bebida          INT NOT NULL,
    id_acompanamiento  INT NOT NULL,
    CONSTRAINT fk_menu_producto FOREIGN KEY (id_menu) REFERENCES productos (id_producto),
    CONSTRAINT fk_menu_sandwich FOREIGN KEY (id_sandwich) REFERENCES productos (id_producto),
    CONSTRAINT fk_menu_bebida FOREIGN KEY (id_bebida) REFERENCES productos (id_producto),
    CONSTRAINT fk_menu_acompanamiento FOREIGN KEY (id_acompanamiento) REFERENCES productos (id_producto)
);

CREATE TABLE movimientos_inventario (
    id_movimiento  INT AUTO_INCREMENT PRIMARY KEY,
    id_producto    INT          NOT NULL,
    tipo           ENUM('ENTRADA', 'VENTA', 'CANJE') NOT NULL,
    cantidad       INT          NOT NULL,
    fecha          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia     VARCHAR(100),
    CONSTRAINT fk_movinv_producto FOREIGN KEY (id_producto) REFERENCES productos (id_producto),
    CONSTRAINT chk_movinv_cantidad CHECK (cantidad > 0)
);

CREATE TABLE pedidos (
    id_pedido         INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente        INT,
    fecha             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado            ENUM('PENDIENTE', 'PAGADO', 'ENTREGADO', 'ANULADO') NOT NULL DEFAULT 'PENDIENTE',
    total             DECIMAL(10,2) NOT NULL DEFAULT 0,
    puntos_generados  INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente)
);

CREATE TABLE detalle_pedido (
    id_detalle       INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido        INT           NOT NULL,
    id_producto      INT           NOT NULL,
    cantidad         INT           NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    subtotal         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos (id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0)
);

CREATE TABLE pagos (
    id_pago          INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido        INT           NOT NULL,
    metodo           ENUM('EFECTIVO', 'TARJETA') NOT NULL,
    monto            DECIMAL(10,2) NOT NULL,
    monto_recibido   DECIMAL(10,2),
    cambio           DECIMAL(10,2),
    referencia       VARCHAR(30),
    ultimos_digitos  CHAR(4),
    fecha            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pagos_pedido UNIQUE (id_pedido),
    CONSTRAINT fk_pagos_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido)
);

CREATE TABLE recompensas (
    id_recompensa      INT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(60) NOT NULL,
    puntos_requeridos  INT         NOT NULL,
    id_producto        INT         NOT NULL,
    activo             TINYINT(1)  NOT NULL DEFAULT 1,
    CONSTRAINT fk_recompensas_producto FOREIGN KEY (id_producto) REFERENCES productos (id_producto),
    CONSTRAINT chk_recompensas_puntos CHECK (puntos_requeridos > 0)
);

CREATE TABLE canjes (
    id_canje       INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente     INT      NOT NULL,
    id_recompensa  INT      NOT NULL,
    puntos_usados  INT      NOT NULL,
    fecha          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_canjes_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente),
    CONSTRAINT fk_canjes_recompensa FOREIGN KEY (id_recompensa) REFERENCES recompensas (id_recompensa)
);

CREATE TABLE movimientos_puntos (
    id_movimiento  INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente     INT          NOT NULL,
    tipo           ENUM('ACUMULACION', 'CANJE') NOT NULL,
    puntos         INT          NOT NULL,
    fecha          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_pedido      INT,
    id_canje       INT,
    descripcion    VARCHAR(150),
    CONSTRAINT fk_movpts_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente),
    CONSTRAINT fk_movpts_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido),
    CONSTRAINT fk_movpts_canje FOREIGN KEY (id_canje) REFERENCES canjes (id_canje)
);

INSERT INTO clientes (id_cliente, identificador, nombre, apellido, telefono, email, puntos, activo, fecha_registro) VALUES
(1, '30214567', 'Ana',    'López', '55123456', 'ana.lopez@correo.com',    10, 1, '2026-10-10 09:00:00'),
(2, '45678912', 'Carlos', 'Pérez', '55234567', 'carlos.perez@correo.com',  0, 1, '2026-10-10 09:15:00');

INSERT INTO productos (id_producto, codigo, nombre, categoria, precio, existencia, activo) VALUES
(1, 'S001', 'Sándwich de Pollo',         'SANDWICH',       35.00, 18, 1),
(2, 'S002', 'Sándwich de Jamón y Queso', 'SANDWICH',       30.00, 15, 1),
(3, 'B001', 'Coca-Cola 355 ml',          'BEBIDA',         12.00, 22, 1),
(4, 'B002', 'Agua pura 500 ml',          'BEBIDA',          8.00, 12, 1),
(5, 'A001', 'Papas fritas',              'ACOMPANAMIENTO', 15.00, 19, 1),
(6, 'A002', 'Galleta de chocolate',      'ACOMPANAMIENTO', 10.00,  4, 1),
(7, 'M001', 'Menú Pollo Clásico',        'MENU',           55.00,  0, 1);

INSERT INTO menu_componentes (id_menu, id_sandwich, id_bebida, id_acompanamiento) VALUES
(7, 1, 3, 5);

INSERT INTO recompensas (id_recompensa, nombre, puntos_requeridos, id_producto, activo) VALUES
(1, 'Bebida gratuita',          10, 3, 1),
(2, 'Acompañamiento gratuito',  15, 5, 1),
(3, 'Sándwich gratuito',        30, 2, 1),
(4, 'Menú gratuito',            50, 7, 1);

INSERT INTO pedidos (id_pedido, id_cliente, fecha, estado, total, puntos_generados) VALUES
(1, 1, '2026-10-20 13:05:00', 'ENTREGADO', 47.00, 2),
(2, 1, '2026-10-22 19:10:00', 'PAGADO',    55.00, 8);

INSERT INTO detalle_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 1, 35.00, 35.00),
(2, 1, 3, 1, 12.00, 12.00),
(3, 2, 7, 1, 55.00, 55.00);

INSERT INTO pagos (id_pago, id_pedido, metodo, monto, monto_recibido, cambio, referencia, ultimos_digitos, fecha) VALUES
(1, 1, 'EFECTIVO', 47.00, 50.00, 3.00, NULL,           NULL,   '2026-10-20 13:07:00'),
(2, 2, 'TARJETA',  55.00, NULL,  NULL, 'AUT-11592038', '4242', '2026-10-22 19:12:00');

INSERT INTO movimientos_puntos (id_cliente, tipo, puntos, fecha, id_pedido, id_canje, descripcion) VALUES
(1, 'ACUMULACION', 2, '2026-10-20 13:07:00', 1, NULL, 'Pedido #1: 1 sándwiches x 2 + 0 memus x 8'),
(1, 'ACUMULACION', 8, '2026-10-22 19:12:00', 2, NULL, 'Pedido #2: 0 sándwiches x 2 + 1 memus x 8');

INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, fecha, referencia) VALUES
(1, 'ENTRADA', 20, '2026-10-15 08:00:00', 'Existencia inicial'),
(2, 'ENTRADA', 15, '2026-10-15 08:00:00', 'Existencia inicial'),
(3, 'ENTRADA', 24, '2026-10-15 08:00:00', 'Existencia inicial'),
(4, 'ENTRADA', 12, '2026-10-15 08:00:00', 'Existencia inicial'),
(5, 'ENTRADA', 20, '2026-10-15 08:00:00', 'Existencia inicial'),
(6, 'ENTRADA',  4, '2026-10-15 08:00:00', 'Existencia inicial'),
(1, 'VENTA',    1, '2026-10-20 13:07:00', 'Pedido #1'),
(3, 'VENTA',    1, '2026-10-20 13:07:00', 'Pedido #1'),
(1, 'VENTA',    1, '2026-10-22 19:12:00', 'Pedido #2'),
(3, 'VENTA',    1, '2026-10-22 19:12:00', 'Pedido #2'),
(5, 'VENTA',    1, '2026-10-22 19:12:00', 'Pedido #2');
