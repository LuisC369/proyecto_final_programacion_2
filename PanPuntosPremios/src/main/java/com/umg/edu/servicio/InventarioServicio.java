package com.umg.edu.servicio;

import com.umg.edu.config.Transaccion;
import com.umg.edu.dao.MovimientoInventarioDAO;
import com.umg.edu.dao.ProductoDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.MovimientoInventario;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.TipoMovimiento;
import java.util.List;

public class InventarioServicio {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();

    public List<MovimientoInventario> historial(Integer idProducto) {
        return movimientoDAO.listar(idProducto);
    }

    public Producto abastecer(int idProducto, Integer cantidad, String referencia) throws NegocioException {
        if (cantidad == null || cantidad <= 0) {
            throw new NegocioException("La cantidad a ingresar debe ser mayor que cero.");
        }
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            throw new NegocioException("Seleccione un producto existente.");
        }
        if (producto.isMenu()) {
            throw new NegocioException("Los menús no tienen existencia propia: abastezca sus componentes.");
        }
        String detalle = referencia == null || referencia.isBlank() ? "Abastecimiento" : referencia.trim();
        Transaccion.ejecutar(conn -> {
            productoDAO.ajustarExistencia(conn, idProducto, cantidad);
            movimientoDAO.registrar(conn, idProducto, TipoMovimiento.ENTRADA, cantidad, detalle);
            return null;
        });
        producto.setExistencia(producto.getExistencia() + cantidad);
        return producto;
    }
}
