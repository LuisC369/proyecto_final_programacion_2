package com.umg.edu.modelo;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Producto {

    public static final int LIMITE_BAJO_INVENTARIO = 5;

    private int id;
    private String codigo;
    private String nombre;
    private BigDecimal precio = BigDecimal.ZERO;
    private int existencia;
    private boolean activo = true;

    public abstract Categoria getCategoria();

    public abstract int getPuntosPorUnidad();

    public static Producto crear(Categoria categoria) {
        return switch (categoria) {
            case SANDWICH -> new Sandwich();
            case BEBIDA -> new Bebida();
            case ACOMPANAMIENTO -> new Acompanamiento();
            case MENU -> new Menu();
        };
    }

    public int getDisponibles() {
        return existencia;
    }

    public Map<Integer, Integer> requerimientos(int cantidad) {
        Map<Integer, Integer> requerimientos = new LinkedHashMap<>();
        requerimientos.put(id, cantidad);
        return requerimientos;
    }

    public boolean isActivoParaVenta() {
        return activo;
    }

    public boolean isVendible() {
        return isActivoParaVenta() && getDisponibles() > 0;
    }

    public boolean isAgotado() {
        return getDisponibles() <= 0;
    }

    public boolean isBajoInventario() {
        return getDisponibles() <= LIMITE_BAJO_INVENTARIO;
    }

    public boolean isMenu() {
        return getCategoria() == Categoria.MENU;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
