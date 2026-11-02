package com.umg.edu.modelo;

public enum TipoMovimientoPuntos {
    ACUMULACION("Acumulación", "bi-plus-circle"),
    CANJE("Canje", "bi-gift");

    private final String etiqueta;
    private final String icono;

    TipoMovimientoPuntos(String etiqueta, String icono) {
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
