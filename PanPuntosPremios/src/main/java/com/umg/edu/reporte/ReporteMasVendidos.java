package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.reporte.ProductoVendidoFila;
import java.time.LocalDate;
import java.util.List;

public class ReporteMasVendidos extends ReportePDF {

    private final List<ProductoVendidoFila> productos;

    public ReporteMasVendidos(LocalDate desde, LocalDate hasta, List<ProductoVendidoFila> productos) {
        super(desde, hasta);
        this.productos = productos;
    }

    @Override
    public String getTitulo() {
        return "Productos más vendidos";
    }

    @Override
    public String getNombreArchivo() {
        return "productos_mas_vendidos.pdf";
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{0.6f, 1.1f, 3.2f, 2f, 1.3f, 1.3f, 1.1f},
                "#", "Código", "Producto", "Categoría", "Venta directa", "En menús", "Total");
        int posicion = 1;
        for (ProductoVendidoFila fila : productos) {
            celda(tabla, String.valueOf(posicion++));
            celda(tabla, fila.codigo());
            celda(tabla, fila.nombre());
            celda(tabla, fila.categoria());
            celdaNumero(tabla, String.valueOf(fila.ventaDirecta()));
            celdaNumero(tabla, String.valueOf(fila.enMenus()));
            celdaNumero(tabla, String.valueOf(fila.total()));
            terminarFila();
        }
        if (productos.isEmpty()) {
            filaVacia(tabla, "No hay ventas registradas en el período.");
        }
        documento.add(tabla);
        espacio(documento);
        resumen(documento, "Unidades vendidas (incluye componentes de menú)",
                String.valueOf(productos.stream().filter(f -> !"Menú completo".equals(f.categoria()))
                        .mapToInt(ProductoVendidoFila::total).sum()));
    }
}
