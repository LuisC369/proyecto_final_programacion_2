package com.umg.edu.modelo;

public class Bebida extends Producto {

    @Override
    public Categoria getCategoria() {
        return Categoria.BEBIDA;
    }

    @Override
    public int getPuntosPorUnidad() {
        return 0;
    }
}
