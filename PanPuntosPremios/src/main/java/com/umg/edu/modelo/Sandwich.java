package com.umg.edu.modelo;

public class Sandwich extends Producto {

    public static final int PUNTOS = 2;

    @Override
    public Categoria getCategoria() {
        return Categoria.SANDWICH;
    }

    @Override
    public int getPuntosPorUnidad() {
        return PUNTOS;
    }
}
