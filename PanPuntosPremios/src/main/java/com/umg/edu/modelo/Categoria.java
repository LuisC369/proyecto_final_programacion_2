package com.umg.edu.modelo;

public enum Categoria {
    SANDWICH("Sándwich", "Sándwiches", "bi-basket2"),
    BEBIDA("Bebida", "Bebidas", "bi-cup-straw"),
    ACOMPANAMIENTO("Acompañamiento", "Acompañamientos", "bi-egg-fried"),
    MENU("Menú completo", "Menús", "bi-box2-heart");

    private final String etiqueta;
    private final String plural;
    private final String icono;

    Categoria(String etiqueta, String plural, String icono) {
        this.etiqueta = etiqueta;
        this.plural = plural;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getPlural() {
        return plural;
    }

    public String getIcono() {
        return icono;
    }

    public String getClave() {
        return name().toLowerCase();
    }
}
