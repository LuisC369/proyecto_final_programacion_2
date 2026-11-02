package com.umg.edu.modelo;

import com.umg.edu.excepcion.PagoRechazadoException;
import com.umg.edu.util.Moneda;
import java.math.BigDecimal;

public class PagoEfectivo extends Pago {

    private BigDecimal montoRecibido;
    private BigDecimal cambio = BigDecimal.ZERO;

    public PagoEfectivo() {
    }

    public PagoEfectivo(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    @Override
    public MetodoPago getMetodo() {
        return MetodoPago.EFECTIVO;
    }

    @Override
    public void procesar() throws PagoRechazadoException {
        if (montoRecibido == null || montoRecibido.compareTo(getMonto()) < 0) {
            throw new PagoRechazadoException("Monto insuficiente: se recibió " + Moneda.formato(montoRecibido)
                    + " y el total es " + Moneda.formato(getMonto()) + ".", null);
        }
        cambio = montoRecibido.subtract(getMonto());
    }

    @Override
    public String getDetalle() {
        return "Recibido " + Moneda.formato(montoRecibido) + " · Cambio " + Moneda.formato(cambio);
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(BigDecimal cambio) {
        this.cambio = cambio;
    }
}
