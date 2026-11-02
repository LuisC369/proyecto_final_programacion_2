package com.umg.edu.modelo;

import java.time.LocalDateTime;

public class Canje {

    private int id;
    private Cliente cliente;
    private Recompensa recompensa;
    private int puntosUsados;
    private LocalDateTime fecha;

    public Canje() {
    }

    public Canje(Cliente cliente, Recompensa recompensa) {
        this.cliente = cliente;
        this.recompensa = recompensa;
        this.puntosUsados = recompensa.getPuntosRequeridos();
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Recompensa getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(Recompensa recompensa) {
        this.recompensa = recompensa;
    }

    public int getPuntosUsados() {
        return puntosUsados;
    }

    public void setPuntosUsados(int puntosUsados) {
        this.puntosUsados = puntosUsados;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
