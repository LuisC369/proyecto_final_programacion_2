package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.ProductoForm;
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
@RequestMapping("/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio = new ProductoServicio();

    @GetMapping
    public String listar(@RequestParam(required = false) Categoria categoria, Model model) {
        model.addAttribute("productos", productoServicio.listar(categoria));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("categoriaActual", categoria);
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Categoria categoria, Model model) {
        ProductoForm form = new ProductoForm();
        form.setCategoria(categoria == null ? Categoria.SANDWICH : categoria);
        model.addAttribute("producto", form);
        cargarOpciones(model);
        return "productos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("producto", ProductoForm.desde(productoServicio.obtener(id)));
            cargarOpciones(model);
            return "productos/formulario";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("producto") ProductoForm form, Model model, RedirectAttributes flash) {
        try {
            productoServicio.guardar(form);
            flash.addFlashAttribute("mensaje", "Producto " + form.getNombre().trim() + " guardado correctamente.");
            return "redirect:/productos";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            cargarOpciones(model);
            return "productos/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable int id, @RequestParam boolean activo, RedirectAttributes flash) {
        try {
            productoServicio.cambiarEstado(id, activo);
            flash.addFlashAttribute("mensaje", activo ? "Producto activado." : "Producto desactivado: ya no se podrá vender.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("sandwiches", productoServicio.listarActivos(Categoria.SANDWICH));
        model.addAttribute("bebidas", productoServicio.listarActivos(Categoria.BEBIDA));
        model.addAttribute("acompanamientos", productoServicio.listarActivos(Categoria.ACOMPANAMIENTO));
    }
}
