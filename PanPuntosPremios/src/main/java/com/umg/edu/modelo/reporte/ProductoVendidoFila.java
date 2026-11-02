package com.umg.edu.modelo.reporte;

public record ProductoVendidoFila(String codigo, String nombre, String categoria, int ventaDirecta, int enMenus) {

    public int total() {
        return ventaDirecta + enMenus;
    }
}
