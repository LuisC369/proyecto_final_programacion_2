package com.umg.edu.modelo;

import java.math.BigDecimal;

public class DetallePedido {

    private int id;
    private final Producto producto;
    private int cantidad;
    private final BigDecimal precioUnitario;

    public DetallePedido(Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getPuntos() {
        return producto.getPuntosPorUnidad() * cantidad;
    }

    public void aumentar(int cantidadAdicional) {
        cantidad += cantidadAdicional;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}
