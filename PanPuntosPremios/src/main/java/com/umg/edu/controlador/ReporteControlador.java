package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.reporte.ReportePDF;
import com.umg.edu.servicio.ReporteServicio;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio = new ReporteServicio();

    @GetMapping
    public String inicio(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("desde", hoy.withDayOfMonth(1));
        model.addAttribute("hasta", hoy);
        return "reportes/index";
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<byte[]> generar(@PathVariable String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1) : desde;
        if (inicio.isAfter(fin)) {
            LocalDate temporal = inicio;
            inicio = fin;
            fin = temporal;
        }
        try {
            ReportePDF reporte = reporteServicio.crear(tipo, inicio, fin);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + reporte.getNombreArchivo() + "\"")
                    .body(reporte.generar());
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
