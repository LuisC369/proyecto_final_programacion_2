package com.umg.edu.modelo;

public enum TipoMovimiento {
    ENTRADA("Entrada", "success", "bi-box-arrow-in-down", true),
    VENTA("Venta", "primary", "bi-cart-check", false),
    CANJE("Canje", "warning", "bi-gift", false);

    private final String etiqueta;
    private final String clase;
    private final String icono;
    private final boolean suma;

    TipoMovimiento(String etiqueta, String clase, String icono, boolean suma) {
        this.etiqueta = etiqueta;
        this.clase = clase;
        this.icono = icono;
        this.suma = suma;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getClase() {
        return clase;
    }

    public String getIcono() {
        return icono;
    }

    public boolean isSuma() {
        return suma;
    }
}
