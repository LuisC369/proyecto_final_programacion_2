package com.umg.edu.modelo;

import java.math.BigDecimal;

public class ProductoForm {

    private int id;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int existencia;
    private Integer idSandwich;
    private Integer idBebida;
    private Integer idAcompanamiento;

    public ProductoForm() {
    }

    public static ProductoForm desde(Producto producto) {
        ProductoForm form = new ProductoForm();
        form.id = producto.getId();
        form.codigo = producto.getCodigo();
        form.nombre = producto.getNombre();
        form.categoria = producto.getCategoria();
        form.precio = producto.getPrecio();
        form.existencia = producto.getExistencia();
        if (producto instanceof Menu menu) {
            form.idSandwich = menu.getSandwich() == null ? null : menu.getSandwich().getId();
            form.idBebida = menu.getBebida() == null ? null : menu.getBebida().getId();
            form.idAcompanamiento = menu.getAcompanamiento() == null ? null : menu.getAcompanamiento().getId();
        }
        return form;
    }

    public boolean isNuevo() {
        return id == 0;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public Integer getIdSandwich() {
        return idSandwich;
    }

    public void setIdSandwich(Integer idSandwich) {
        this.idSandwich = idSandwich;
    }

    public Integer getIdBebida() {
        return idBebida;
    }

    public void setIdBebida(Integer idBebida) {
        this.idBebida = idBebida;
    }

    public Integer getIdAcompanamiento() {
        return idAcompanamiento;
    }

    public void setIdAcompanamiento(Integer idAcompanamiento) {
        this.idAcompanamiento = idAcompanamiento;
    }
}
