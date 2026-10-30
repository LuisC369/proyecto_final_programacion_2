package com.umg.edu.excepcion;

public class PagoRechazadoException extends NegocioException {

    private final String referencia;

    public PagoRechazadoException(String mensaje, String referencia) {
        super(referencia == null ? mensaje : mensaje + " Referencia: " + referencia + ".");
        this.referencia = referencia;
    }

    public String getReferencia() {
        return referencia;
    }
}
