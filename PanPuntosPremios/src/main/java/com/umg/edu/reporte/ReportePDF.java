package com.umg.edu.reporte;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ReportePDF {

    protected static final Color CAFE = new Color(107, 62, 38);
    protected static final Color MOSTAZA = new Color(232, 163, 61);
    protected static final Color CREMA = new Color(255, 247, 236);
    protected static final Color GRIS = new Color(120, 110, 100);
    protected static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font FUENTE_MARCA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, MOSTAZA);
    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, CAFE);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS);
    private static final Font FUENTE_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FUENTE_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font FUENTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, CAFE);

    private final LocalDate desde;
    private final LocalDate hasta;
    private int filaActual;

    protected ReportePDF(LocalDate desde, LocalDate hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public abstract String getTitulo();

    public abstract String getNombreArchivo();

    protected abstract void escribirContenido(Document documento) throws DocumentException;

    protected boolean usaPeriodo() {
        return true;
    }

    public byte[] generar() {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            escribirEncabezado(documento);
            escribirContenido(documento);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el reporte " + getTitulo() + ".", e);
        } finally {
            documento.close();
        }
        return salida.toByteArray();
    }

    private void escribirEncabezado(Document documento) throws DocumentException {
        documento.add(new Paragraph("PAN, PUNTOS Y PREMIOS", FUENTE_MARCA));
        Paragraph titulo = new Paragraph(getTitulo(), FUENTE_TITULO);
        titulo.setSpacingBefore(4);
        documento.add(titulo);
        StringBuilder subtitulo = new StringBuilder();
        if (usaPeriodo()) {
            subtitulo.append("Período: ").append(desde.format(FECHA)).append(" al ").append(hasta.format(FECHA)).append("   ·   ");
        }
        subtitulo.append("Generado: ").append(LocalDateTime.now().format(FECHA_HORA));
        documento.add(new Paragraph(subtitulo.toString(), FUENTE_SUBTITULO));
        LineSeparator linea = new LineSeparator(1.5f, 100, MOSTAZA, Element.ALIGN_CENTER, -6);
        documento.add(new Chunk(linea));
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(6);
        documento.add(espacio);
    }

    protected PdfPTable crearTabla(float[] anchos, String... encabezados) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO));
            celda.setBackgroundColor(CAFE);
            celda.setPadding(6);
            celda.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(celda);
        }
        filaActual = 0;
        return tabla;
    }

    protected void celda(PdfPTable tabla, String texto) {
        tabla.addCell(nuevaCelda(texto, Element.ALIGN_LEFT));
    }

    protected void celdaNumero(PdfPTable tabla, String texto) {
        tabla.addCell(nuevaCelda(texto, Element.ALIGN_RIGHT));
    }

    protected void terminarFila() {
        filaActual++;
    }

    protected void filaVacia(PdfPTable tabla, String mensaje) {
        PdfPCell celda = new PdfPCell(new Phrase(mensaje, FUENTE_SUBTITULO));
        celda.setColspan(tabla.getNumberOfColumns());
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(12);
        celda.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(celda);
    }

    protected void resumen(Document documento, String etiqueta, String valor) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{3, 1});
        tabla.setWidthPercentage(55);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_CELDA));
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, FUENTE_TOTAL));
        for (PdfPCell celda : new PdfPCell[]{celdaEtiqueta, celdaValor}) {
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(new Color(235, 225, 210));
            celda.setPadding(5);
        }
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
        documento.add(tabla);
    }

    protected void espacio(Document documento) throws DocumentException {
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(4);
        documento.add(espacio);
    }

    private PdfPCell nuevaCelda(String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, FUENTE_CELDA));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(new Color(235, 225, 210));
        if (filaActual % 2 == 1) {
            celda.setBackgroundColor(CREMA);
        }
        return celda;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }
}

