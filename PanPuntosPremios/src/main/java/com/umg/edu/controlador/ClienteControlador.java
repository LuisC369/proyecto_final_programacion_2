package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Cliente;
import com.umg.edu.servicio.ClienteServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio = new ClienteServicio();

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clientes", clienteServicio.buscar(q));
        model.addAttribute("q", q);
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("cliente", clienteServicio.obtener(id));
            return "clientes/formulario";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente, Model model, RedirectAttributes flash) {
        boolean nuevo = cliente.getId() == 0;
        try {
            clienteServicio.guardar(cliente);
            flash.addFlashAttribute("mensaje", nuevo
                    ? "Cliente registrado con saldo inicial de 0 puntos."
                    : "Datos del cliente actualizados.");
            return "redirect:/clientes";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "clientes/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable int id, @RequestParam boolean activo, RedirectAttributes flash) {
        try {
            clienteServicio.cambiarEstado(id, activo);
            flash.addFlashAttribute("mensaje", activo ? "Cliente activado." : "Cliente desactivado.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes flash) {
        try {
            clienteServicio.eliminar(id);
            flash.addFlashAttribute("mensaje", "Cliente eliminado.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }
}
