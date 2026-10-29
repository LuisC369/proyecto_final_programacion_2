package com.umg.edu.modelo;

public abstract class Persona {

    private int id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;

    protected Persona() {
    }

    protected Persona(int id, String nombre, String apellido, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
    }

    public abstract String getDescripcion();

    public String getNombreCompleto() {
        return (texto(nombre) + " " + texto(apellido)).trim();
    }

    public String getIniciales() {
        StringBuilder iniciales = new StringBuilder();
        if (!texto(nombre).isEmpty()) {
            iniciales.append(Character.toUpperCase(nombre.trim().charAt(0)));
        }
        if (!texto(apellido).isEmpty()) {
            iniciales.append(Character.toUpperCase(apellido.trim().charAt(0)));
        }
        return iniciales.toString();
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
