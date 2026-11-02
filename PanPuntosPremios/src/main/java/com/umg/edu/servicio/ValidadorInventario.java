package com.umg.edu.servicio;

import com.umg.edu.excepcion.InventarioInsuficienteException;
import com.umg.edu.modelo.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ValidadorInventario {

    private ValidadorInventario() {
    }

    public static void verificar(Map<Integer, Integer> requerimientos, Map<Integer, Integer> existencias,
            Map<Integer, Producto> catalogo) throws InventarioInsuficienteException {
        List<String> faltantes = new ArrayList<>();
        requerimientos.forEach((idProducto, requerido) -> {
            int disponible = existencias.getOrDefault(idProducto, 0);
            if (requerido > disponible) {
                Producto producto = catalogo.get(idProducto);
                String nombre = producto == null ? "Producto #" + idProducto : producto.getNombre();
                faltantes.add(nombre + ": se requieren " + requerido + ", disponible " + disponible);
            }
        });
        if (!faltantes.isEmpty()) {
            throw new InventarioInsuficienteException(faltantes);
        }
    }
}
