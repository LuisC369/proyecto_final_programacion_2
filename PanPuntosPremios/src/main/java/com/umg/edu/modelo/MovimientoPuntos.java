package com.umg.edu.modelo;

import java.time.LocalDateTime;

public class MovimientoPuntos {

    private int id;
    private int idCliente;
    private TipoMovimientoPuntos tipo;
    private int puntos;
    private LocalDateTime fecha;
    private Integer idPedido;
    private String descripcion;

    public String getPuntosConSigno() {
        return puntos > 0 ? "+" + puntos : String.valueOf(puntos);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public TipoMovimientoPuntos getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoPuntos tipo) {
        this.tipo = tipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
