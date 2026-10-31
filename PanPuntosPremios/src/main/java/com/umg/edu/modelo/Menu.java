package com.umg.edu.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Menu extends Producto {

    public static final int PUNTOS = 8;

    private Sandwich sandwich;
    private Bebida bebida;
    private Acompanamiento acompanamiento;

    @Override
    public Categoria getCategoria() {
        return Categoria.MENU;
    }

    @Override
    public int getPuntosPorUnidad() {
        return PUNTOS;
    }

    public List<Producto> getComponentes() {
        List<Producto> componentes = new ArrayList<>();
        if (sandwich != null) {
            componentes.add(sandwich);
        }
        if (bebida != null) {
            componentes.add(bebida);
        }
        if (acompanamiento != null) {
            componentes.add(acompanamiento);
        }
        return componentes;
    }

    public boolean isCompleto() {
        return sandwich != null && bebida != null && acompanamiento != null;
    }

    @Override
    public int getDisponibles() {
        if (!isCompleto()) {
            return 0;
        }
        return getComponentes().stream().mapToInt(Producto::getExistencia).min().orElse(0);
    }

    @Override
    public Map<Integer, Integer> requerimientos(int cantidad) {
        Map<Integer, Integer> requerimientos = new LinkedHashMap<>();
        for (Producto componente : getComponentes()) {
            requerimientos.merge(componente.getId(), cantidad, Integer::sum);
        }
        return requerimientos;
    }

    @Override
    public boolean isActivoParaVenta() {
        return isActivo() && isCompleto() && getComponentes().stream().allMatch(Producto::isActivo);
    }

    public String getDescripcionComponentes() {
        return getComponentes().stream().map(Producto::getNombre).collect(Collectors.joining(" + "));
    }

    public Sandwich getSandwich() {
        return sandwich;
    }

    public void setSandwich(Sandwich sandwich) {
        this.sandwich = sandwich;
    }

    public Bebida getBebida() {
        return bebida;
    }

    public void setBebida(Bebida bebida) {
        this.bebida = bebida;
    }

    public Acompanamiento getAcompanamiento() {
        return acompanamiento;
    }

    public void setAcompanamiento(Acompanamiento acompanamiento) {
        this.acompanamiento = acompanamiento;
    }
}
