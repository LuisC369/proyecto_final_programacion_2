package com.umg.edu.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.umg.edu.modelo.reporte.PuntosClienteFila;
import java.time.LocalDate;
import java.util.List;

public class ReportePuntosClientes extends ReportePDF {

    private final List<PuntosClienteFila> clientes;

    public ReportePuntosClientes(List<PuntosClienteFila> clientes) {
        super(LocalDate.now(), LocalDate.now());
        this.clientes = clientes;
    }

    @Override
    public String getTitulo() {
        return "Puntos acumulados por cliente";
    }

    @Override
    public String getNombreArchivo() {
        return "puntos_por_cliente.pdf";
    }

    @Override
    protected boolean usaPeriodo() {
        return false;
    }

    @Override
    protected void escribirContenido(Document documento) throws DocumentException {
        PdfPTable tabla = crearTabla(new float[]{1.6f, 3.2f, 1.2f, 1.4f, 1.4f, 1.2f},
                "Identificador", "Cliente", "Estado", "Acumulados", "Canjeados", "Saldo");
        for (PuntosClienteFila fila : clientes) {
            celda(tabla, fila.identificador());
            celda(tabla, fila.cliente());
            celda(tabla, fila.activo() ? "Activo" : "Inactivo");
            celdaNumero(tabla, String.valueOf(fila.acumulados()));
            celdaNumero(tabla, String.valueOf(fila.canjeados()));
            celdaNumero(tabla, String.valueOf(fila.saldo()));
            terminarFila();
        }
        if (clientes.isEmpty()) {
            filaVacia(tabla, "No hay clientes registrados.");
        }
        documento.add(tabla);
        espacio(documento);
        resumen(documento, "Puntos acumulados (histórico)", String.valueOf(clientes.stream().mapToInt(PuntosClienteFila::acumulados).sum()));
        resumen(documento, "Puntos canjeados (histórico)", String.valueOf(clientes.stream().mapToInt(PuntosClienteFila::canjeados).sum()));
        resumen(documento, "Saldo total en circulación", String.valueOf(clientes.stream().mapToInt(PuntosClienteFila::saldo).sum()));
    }
}
