package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.Producto;
import java.time.LocalDate;
import java.util.List;

public class ReporteBajoInventario extends ReportePDF {

    private final List<Producto> productos;

    public ReporteBajoInventario(List<Producto> productos) {
        super(LocalDate.now(), LocalDate.now());
        this.productos = productos;
    }

    @Override
    public String getTitulo() {
        return "Productos agotados o con " + Producto.LIMITE_BAJO_INVENTARIO + " unidades o menos";
    }

    @Override
    public String getNombreArchivo() {
        return "inventario_bajo.pdf";
    }

    @Override
    protected boolean usaPeriodo() {
        return false;
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{1.2f, 3.5f, 2f, 1.3f, 1.5f, 1.3f},
                "Código", "Producto", "Categoría", "Existencia", "Situación", "Estado");
        for (Producto producto : productos) {
            celda(tabla, producto.getCodigo());
            celda(tabla, producto.getNombre());
            celda(tabla, producto.getCategoria().getEtiqueta());
            celdaNumero(tabla, String.valueOf(producto.getDisponibles()));
            celda(tabla, producto.isAgotado() ? "AGOTADO" : "Existencia baja");
            celda(tabla, producto.isActivo() ? "Activo" : "Inactivo");
            terminarFila();
        }
        if (productos.isEmpty()) {
            filaVacia(tabla, "Todos los productos tienen existencia suficiente.");
        }
        documento.add(tabla);
        espacio(documento);
        resumen(documento, "Productos agotados", String.valueOf(productos.stream().filter(Producto::isAgotado).count()));
        resumen(documento, "Productos con existencia baja", String.valueOf(productos.stream().filter(p -> !p.isAgotado()).count()));
    }
}
