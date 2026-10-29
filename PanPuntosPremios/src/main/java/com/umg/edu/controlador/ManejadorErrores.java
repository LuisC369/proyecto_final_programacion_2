package com.umg.edu.controlador;

import com.umg.edu.excepcion.DatosException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.umg.edu.controlador")
public class ManejadorErrores {

    @ExceptionHandler(DatosException.class)
    public String errorDeDatos(DatosException e, Model model) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        model.addAttribute("titulo", "Problema con la base de datos");
        model.addAttribute("mensaje", e.getMessage());
        model.addAttribute("detalle", causa.getMessage());
        return "error";
    }
}
