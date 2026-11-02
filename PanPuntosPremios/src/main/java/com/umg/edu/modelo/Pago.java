package com.umg.edu.modelo;

import com.umg.edu.excepcion.PagoRechazadoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Pago {

    private int id;
    private int idPedido;
    private BigDecimal monto = BigDecimal.ZERO;
    private LocalDateTime fecha = LocalDateTime.now();

    public abstract MetodoPago getMetodo();

    public abstract void procesar() throws PagoRechazadoException;

    public abstract String getDetalle();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
