package com.umg.edu.servicio;

import com.umg.edu.dao.CanjeDAO;
import com.umg.edu.dao.ProductoDAO;
import com.umg.edu.dao.ReporteDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Menu;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.reporte.ProductoVendidoFila;
import com.umg.edu.reporte.ReporteBajoInventario;
import com.umg.edu.reporte.ReporteCanjes;
import com.umg.edu.reporte.ReporteMasVendidos;
import com.umg.edu.reporte.ReportePDF;
import com.umg.edu.reporte.ReportePuntosClientes;
import com.umg.edu.reporte.ReporteVentas;
import com.umg.edu.reporte.ReporteVentasMenus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteServicio {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CanjeDAO canjeDAO = new CanjeDAO();
    private final ProductoServicio productoServicio = new ProductoServicio();

    public ReportePDF crear(String tipo, LocalDate desde, LocalDate hasta) throws NegocioException {
        return switch (tipo) {
            case "ventas" -> new ReporteVentas(desde, hasta, reporteDAO.ventasPorPeriodo(desde, hasta));
            case "mas-vendidos" -> new ReporteMasVendidos(desde, hasta, productosMasVendidos(desde, hasta));
            case "inventario" -> new ReporteBajoInventario(productoServicio.listarBajoInventario());
            case "puntos" -> new ReportePuntosClientes(reporteDAO.puntosPorCliente());
            case "canjes" -> new ReporteCanjes(desde, hasta, canjeDAO.listar(desde, hasta));
            case "menus" -> new ReporteVentasMenus(desde, hasta, reporteDAO.ventasMenus(desde, hasta));
            default -> throw new NegocioException("El reporte solicitado no existe.");
        };
    }

    public List<ProductoVendidoFila> productosMasVendidos(LocalDate desde, LocalDate hasta) {
        Map<Integer, Integer> vendidos = reporteDAO.cantidadesVendidas(desde, hasta);
        Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo();
        Map<Integer, int[]> acumulado = new LinkedHashMap<>();
        vendidos.forEach((idProducto, cantidad) -> {
            Producto producto = catalogo.get(idProducto);
            acumulado.computeIfAbsent(idProducto, id -> new int[2])[0] += cantidad;
            if (producto instanceof Menu menu) {
                for (Producto componente : menu.getComponentes()) {
                    acumulado.computeIfAbsent(componente.getId(), id -> new int[2])[1] += cantidad;
                }
            }
        });
        List<ProductoVendidoFila> filas = new ArrayList<>();
        acumulado.forEach((idProducto, cantidades) -> {
            Producto producto = catalogo.get(idProducto);
            filas.add(new ProductoVendidoFila(producto.getCodigo(), producto.getNombre(),
                    producto.getCategoria().getEtiqueta(), cantidades[0], cantidades[1]));
        });
        filas.sort(Comparator.comparingInt(ProductoVendidoFila::total).reversed()
                .thenComparing(ProductoVendidoFila::nombre));
        return filas;
    }
}
