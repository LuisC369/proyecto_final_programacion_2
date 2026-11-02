package com.umg.edu.modelo.reporte;

import com.umg.edu.modelo.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaFila(int idPedido, LocalDateTime fecha, String cliente, MetodoPago metodo, BigDecimal monto) {
}
