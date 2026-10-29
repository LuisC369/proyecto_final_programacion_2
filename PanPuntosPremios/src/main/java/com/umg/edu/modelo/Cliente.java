package com.umg.edu.modelo;

import java.time.LocalDateTime;

public class Cliente extends Persona {

    private String identificador;
    private int puntos;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    public Cliente() {
    }

    public Cliente(int id, String identificador, String nombre, String apellido, String telefono,
            String email, int puntos, boolean activo, LocalDateTime fechaRegistro) {
        super(id, nombre, apellido, telefono, email);
        this.identificador = identificador;
        this.puntos = puntos;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String getDescripcion() {
        return getNombreCompleto() + " (" + identificador + ")";
    }

    public String getNivel() {
        if (puntos >= 50) {
            return "Oro";
        }
        if (puntos >= 30) {
            return "Plata";
        }
        return "Bronce";
    }

    public boolean tieneSaldo(int puntosRequeridos) {
        return puntos >= puntosRequeridos;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
