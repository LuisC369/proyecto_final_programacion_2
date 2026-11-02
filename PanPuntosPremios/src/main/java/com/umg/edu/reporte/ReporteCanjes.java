package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.Canje;
import java.time.LocalDate;
import java.util.List;

public class ReporteCanjes extends ReportePDF {

    private final List<Canje> canjes;

    public ReporteCanjes(LocalDate desde, LocalDate hasta, List<Canje> canjes) {
        super(desde, hasta);
        this.canjes = canjes;
    }

    @Override
    public String getTitulo() {
        return "Historial de canjes";
    }

    @Override
    public String getNombreArchivo() {
        return "historial_canjes.pdf";
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{0.9f, 2f, 3f, 2.4f, 2.6f, 1f},
                "Canje", "Fecha", "Cliente", "Recompensa", "Producto entregado", "Puntos");
        for (Canje canje : canjes) {
            celda(tabla, "#" + canje.getId());
            celda(tabla, canje.getFecha().format(FECHA_HORA));
            celda(tabla, canje.getCliente().getDescripcion());
            celda(tabla, canje.getRecompensa().getNombre());
            celda(tabla, canje.getRecompensa().getProducto().getNombre());
            celdaNumero(tabla, String.valueOf(canje.getPuntosUsados()));
            terminarFila();
        }
        if (canjes.isEmpty()) {
            filaVacia(tabla, "No hay canjes registrados en el período.");
        }
        documento.add(tabla);
        espacio(documento);
        resumen(documento, "Canjes realizados", String.valueOf(canjes.size()));
        resumen(documento, "Puntos canjeados", String.valueOf(canjes.stream().mapToInt(Canje::getPuntosUsados).sum()));
    }
}
