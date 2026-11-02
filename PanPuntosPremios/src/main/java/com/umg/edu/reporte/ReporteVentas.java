package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.MetodoPago;
import com.umg.edu.modelo.reporte.VentaFila;
import com.umg.edu.util.Moneda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReporteVentas extends ReportePDF {

    private final List<VentaFila> ventas;

    public ReporteVentas(LocalDate desde, LocalDate hasta, List<VentaFila> ventas) {
        super(desde, hasta);
        this.ventas = ventas;
    }

    @Override
    public String getTitulo() {
        return "Ventas por período";
    }

    @Override
    public String getNombreArchivo() {
        return "ventas_por_periodo.pdf";
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{1.2f, 2f, 3f, 1.5f, 1.6f}, "Pedido", "Fecha", "Cliente", "Método", "Monto");
        Map<MetodoPago, BigDecimal> totales = new EnumMap<>(MetodoPago.class);
        Map<MetodoPago, Integer> cantidades = new EnumMap<>(MetodoPago.class);
        for (VentaFila venta : ventas) {
            celda(tabla, "#" + venta.idPedido());
            celda(tabla, venta.fecha().format(FECHA_HORA));
            celda(tabla, venta.cliente());
            celda(tabla, venta.metodo().getEtiqueta());
            celdaNumero(tabla, Moneda.formato(venta.monto()));
            terminarFila();
            totales.merge(venta.metodo(), venta.monto(), BigDecimal::add);
            cantidades.merge(venta.metodo(), 1, Integer::sum);
        }
        if (ventas.isEmpty()) {
            filaVacia(tabla, "No hay ventas registradas en el período.");
        }
        documento.add(tabla);
        espacio(documento);

        BigDecimal total = BigDecimal.ZERO;
        for (MetodoPago metodo : MetodoPago.values()) {
            BigDecimal monto = totales.getOrDefault(metodo, BigDecimal.ZERO);
            resumen(documento, metodo.getEtiqueta() + " (" + cantidades.getOrDefault(metodo, 0) + " pagos)", Moneda.formato(monto));
            total = total.add(monto);
        }
        resumen(documento, "Total vendido (" + ventas.size() + " pedidos)", Moneda.formato(total));
    }
}
