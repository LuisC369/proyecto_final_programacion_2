package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Canje;
import com.umg.edu.modelo.Recompensa;
import com.umg.edu.servicio.FidelizacionServicio;
import com.umg.edu.servicio.ProductoServicio;
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
@RequestMapping("/recompensas")
public class RecompensaControlador {

    private final FidelizacionServicio fidelizacionServicio = new FidelizacionServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recompensas", fidelizacionServicio.listarRecompensas());
        return "recompensas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("recompensa", new Recompensa());
        model.addAttribute("productos", productoServicio.listar(null));
        return "recompensas/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("recompensa", fidelizacionServicio.obtenerRecompensa(id));
            model.addAttribute("productos", productoServicio.listar(null));
            return "recompensas/formulario";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/recompensas";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Recompensa recompensa, Model model, RedirectAttributes flash) {
        try {
            fidelizacionServicio.guardarRecompensa(recompensa);
            flash.addFlashAttribute("mensaje", "Recompensa guardada en el catálogo.");
            return "redirect:/recompensas";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productos", productoServicio.listar(null));
            return "recompensas/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable int id, @RequestParam boolean activo, RedirectAttributes flash) {
        try {
            fidelizacionServicio.cambiarEstadoRecompensa(id, activo);
            flash.addFlashAttribute("mensaje", activo ? "Recompensa activada." : "Recompensa desactivada.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recompensas";
    }

    @PostMapping("/canjear")
    public String canjear(@RequestParam int idCliente, @RequestParam int idRecompensa, RedirectAttributes flash) {
        try {
            Canje canje = fidelizacionServicio.canjear(idCliente, idRecompensa);
            flash.addFlashAttribute("mensaje", "¡Canje realizado! Entregue: " + canje.getRecompensa().getProducto().getNombre()
                    + ". Se descontaron " + canje.getPuntosUsados() + " puntos; saldo actual: "
                    + canje.getCliente().getPuntos() + " pts.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", "Canje rechazado. " + e.getMessage());
        }
        return "redirect:/clientes/" + idCliente;
    }

    @GetMapping("/canjes")
    public String canjes(Model model) {
        model.addAttribute("canjes", fidelizacionServicio.historialCanjes());
        return "recompensas/canjes";
    }
}
