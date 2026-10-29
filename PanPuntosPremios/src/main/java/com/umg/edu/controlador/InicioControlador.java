package com.umg.edu.controlador;

import com.umg.edu.servicio.ClienteServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {

    private final ClienteServicio clienteServicio = new ClienteServicio();

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("clientesActivos", clienteServicio.contarActivos());
        model.addAttribute("topClientes", clienteServicio.topPorPuntos(5));
        return "index";
    }
}
