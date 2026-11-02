package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.reporte.VentaMenuFila;
import com.umg.edu.util.Moneda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteVentasMenus extends ReportePDF {

    private final List<VentaMenuFila> menus;

    public ReporteVentasMenus(LocalDate desde, LocalDate hasta, List<VentaMenuFila> menus) {
        super(desde, hasta);
        this.menus = menus;
    }

    @Override
    public String getTitulo() {
        return "Ventas de menús completos";
    }

    @Override
    public String getNombreArchivo() {
        return "ventas_menus.pdf";
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{1.2f, 4f, 1.5f, 2f}, "Código", "Menú", "Cantidad", "Monto");
        for (VentaMenuFila fila : menus) {
            celda(tabla, fila.codigo());
            celda(tabla, fila.nombre());
            celdaNumero(tabla, String.valueOf(fila.cantidad()));
            celdaNumero(tabla, Moneda.formato(fila.monto()));
            terminarFila();
        }
        if (menus.isEmpty()) {
            filaVacia(tabla, "No se vendieron menús en el período.");
        }
        documento.add(tabla);
        espacio(documento);
        resumen(documento, "Menús vendidos", String.valueOf(menus.stream().mapToInt(VentaMenuFila::cantidad).sum()));
        resumen(documento, "Monto total en menús", Moneda.formato(
                menus.stream().map(VentaMenuFila::monto).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }
}
