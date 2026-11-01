package com.umg.edu.servicio;

import com.umg.edu.dao.ReporteDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.reporte.ReportePDF;
import com.umg.edu.reporte.ReportePuntosClientes;
import java.time.LocalDate;

public class ReporteServicio {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public ReportePDF crear(String tipo, LocalDate desde, LocalDate hasta) throws NegocioException {
        return switch (tipo) {
            case "puntos" -> new ReportePuntosClientes(reporteDAO.puntosPorCliente());
            default -> throw new NegocioException("El reporte solicitado no existe.");
        };
    }
}
