package com.umg.edu.excepcion;

import java.util.List;

public class InventarioInsuficienteException extends NegocioException {

    private final List<String> faltantes;

    public InventarioInsuficienteException(List<String> faltantes) {
        super("Inventario insuficiente. " + String.join("; ", faltantes) + ".");
        this.faltantes = List.copyOf(faltantes);
    }

    public List<String> getFaltantes() {
        return faltantes;
    }
}
