package com.umg.edu.modelo;

import com.umg.edu.excepcion.PagoRechazadoException;
import java.util.concurrent.ThreadLocalRandom;

public class PagoTarjeta extends Pago {

    private String numeroTarjeta;
    private String ultimosDigitos;
    private String referencia;

    public PagoTarjeta() {
    }

    public PagoTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    @Override
    public MetodoPago getMetodo() {
        return MetodoPago.TARJETA;
    }

    @Override
    public void procesar() throws PagoRechazadoException {
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("[\\s-]", "");
        if (!digitos.matches("\\d{16}")) {
            throw new PagoRechazadoException("El número de tarjeta debe tener 16 dígitos.", null);
        }
        ultimosDigitos = digitos.substring(12);
        if (digitos.endsWith("0000")) {
            throw new PagoRechazadoException("Tarjeta rechazada por el banco: fondos insuficientes.", generarReferencia("RCH"));
        }
        if (!esValidaLuhn(digitos)) {
            throw new PagoRechazadoException("Tarjeta rechazada: el número no es válido.", generarReferencia("RCH"));
        }
        referencia = generarReferencia("AUT");
    }

    private static String generarReferencia(String prefijo) {
        return prefijo + "-" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
    }

    private static boolean esValidaLuhn(String digitos) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int digito = digitos.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    @Override
    public String getDetalle() {
        return "Tarjeta **** " + ultimosDigitos + " · Autorización " + referencia;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
