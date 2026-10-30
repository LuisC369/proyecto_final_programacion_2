package com.umg.edu.modelo;

public enum MetodoPago {
    EFECTIVO("Efectivo", "bi-cash-coin"),
    TARJETA("Tarjeta", "bi-credit-card-2-front");

    private final String etiqueta;
    private final String icono;

    MetodoPago(String etiqueta, String icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getIcono() {
        return icono;
    }
}
