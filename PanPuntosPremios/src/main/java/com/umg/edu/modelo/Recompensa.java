package com.umg.edu.modelo;

public class Recompensa {

    private int id;
    private String nombre;
    private int puntosRequeridos;
    private int idProducto;
    private Producto producto;
    private boolean activo = true;

    public boolean isDisponible() {
        return activo && producto != null && producto.getDisponibles() > 0;
    }

    public boolean puedeCanjear(Cliente cliente) {
        return motivoRechazo(cliente) == null;
    }

    public String motivoRechazo(Cliente cliente) {
        if (cliente == null) {
            return "El cliente no existe";
        }
        if (!cliente.isActivo()) {
            return "Cliente desactivado";
        }
        if (!activo) {
            return "Recompensa inactiva";
        }
        if (producto == null || producto.getDisponibles() <= 0) {
            return "Sin unidades disponibles";
        }
        if (!cliente.tieneSaldo(puntosRequeridos)) {
            return "Faltan " + (puntosRequeridos - cliente.getPuntos()) + " pts";
        }
        return null;
    }

    public int porcentajeAvance(Cliente cliente) {
        if (cliente == null || puntosRequeridos <= 0) {
            return 0;
        }
        return Math.min(100, cliente.getPuntos() * 100 / puntosRequeridos);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuntosRequeridos() {
        return puntosRequeridos;
    }

    public void setPuntosRequeridos(int puntosRequeridos) {
        this.puntosRequeridos = puntosRequeridos;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.idProducto = producto.getId();
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
