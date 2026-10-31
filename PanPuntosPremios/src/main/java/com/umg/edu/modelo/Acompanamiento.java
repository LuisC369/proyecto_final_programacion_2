package com.umg.edu.modelo;

public class Acompanamiento extends Producto {

    @Override
    public Categoria getCategoria() {
        return Categoria.ACOMPANAMIENTO;
    }

    @Override
    public int getPuntosPorUnidad() {
        return 0;
    }
}
