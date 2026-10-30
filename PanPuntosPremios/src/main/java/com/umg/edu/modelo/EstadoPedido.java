package com.umg.edu.modelo;

public enum EstadoPedido {
    PENDIENTE("Pendiente", "warning", "bi-hourglass-split"),
    PAGADO("Pagado", "info", "bi-credit-card"),
    ENTREGADO("Entregado", "success", "bi-bag-check"),
    ANULADO("Anulado", "secondary", "bi-x-circle");

    private final String etiqueta;
    private final String clase;
    private final String icono;

    EstadoPedido(String etiqueta, String clase, String icono) {
        this.etiqueta = etiqueta;
        this.clase = clase;
        this.icono = icono;
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
}
