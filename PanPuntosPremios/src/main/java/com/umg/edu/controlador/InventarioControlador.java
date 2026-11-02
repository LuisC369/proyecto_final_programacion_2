package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Producto;
import com.umg.edu.servicio.InventarioServicio;
import com.umg.edu.servicio.ProductoServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
public class InventarioControlador {

    private final InventarioServicio inventarioServicio = new InventarioServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();

    @GetMapping
    public String inicio(@RequestParam(required = false) Integer producto, Model model) {
        model.addAttribute("productos", productoServicio.listar(null).stream().filter(p -> !p.isMenu()).toList());
        model.addAttribute("bajoInventario", productoServicio.listarBajoInventario());
        model.addAttribute("movimientos", inventarioServicio.historial(producto));
        model.addAttribute("productoSeleccionado", producto);
        return "inventario/index";
    }

    @PostMapping("/abastecer")
    public String abastecer(@RequestParam(defaultValue = "0") int idProducto,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) String referencia,
            RedirectAttributes flash) {
        try {
            Producto producto = inventarioServicio.abastecer(idProducto, cantidad, referencia);
            flash.addFlashAttribute("mensaje", "Se ingresaron " + cantidad + " unidades de " + producto.getNombre()
                    + ". Nueva existencia: " + producto.getExistencia() + ".");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventario";
    }
}
