package com.umg.edu.excepcion;

public class PuntosInsuficientesException extends NegocioException {

    public PuntosInsuficientesException(int saldo, int requeridos) {
        super("Puntos insuficientes: el cliente tiene " + saldo + " pts y la recompensa requiere "
                + requeridos + " pts (faltan " + (requeridos - saldo) + ").");
    }
}
