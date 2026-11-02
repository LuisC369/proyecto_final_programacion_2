package com.umg.edu.controlador;

import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.EstadoPedido;
import com.umg.edu.modelo.MetodoPago;
import com.umg.edu.modelo.Pago;
import com.umg.edu.modelo.PagoEfectivo;
import com.umg.edu.modelo.PagoTarjeta;
import com.umg.edu.modelo.Pedido;
import com.umg.edu.servicio.ClienteServicio;
import com.umg.edu.servicio.PedidoServicio;
import com.umg.edu.servicio.ProductoServicio;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
public class PedidoControlador {

    private final PedidoServicio pedidoServicio = new PedidoServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();
    private final ClienteServicio clienteServicio = new ClienteServicio();

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", pedidoServicio.listar(estado));
        model.addAttribute("conteo", pedidoServicio.contarPorEstado());
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoActual", estado);
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Integer cliente, Model model) {
        model.addAttribute("productos", productoServicio.listarActivos(null));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("clientes", clienteServicio.listarActivos());
        model.addAttribute("clienteSeleccionado", cliente);
        return "pedidos/nuevo";
    }

    @PostMapping
    public String crear(@RequestParam(required = false) Integer idCliente,
            @RequestParam(name = "productoId", required = false) List<Integer> productoIds,
            @RequestParam(name = "cantidad", required = false) List<Integer> cantidades,
            RedirectAttributes flash) {
        Map<Integer, Integer> productos = new LinkedHashMap<>();
        if (productoIds != null && cantidades != null) {
            for (int i = 0; i < productoIds.size() && i < cantidades.size(); i++) {
                if (cantidades.get(i) != null && cantidades.get(i) > 0) {
                    productos.merge(productoIds.get(i), cantidades.get(i), Integer::sum);
                }
            }
        }
        try {
            Pedido pedido = pedidoServicio.crear(idCliente, productos);
            flash.addFlashAttribute("mensaje", "Pedido #" + pedido.getId() + " creado en estado PENDIENTE. Ya puede cobrarse.");
            return "redirect:/pedidos/" + pedido.getId();
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/nuevo" + (idCliente != null ? "?cliente=" + idCliente : "");
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("pedido", pedidoServicio.obtener(id));
            return "pedidos/detalle";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos";
        }
    }

    @GetMapping("/{id}/cobrar")
    public String formularioCobro(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            Pedido pedido = pedidoServicio.obtener(id);
            if (!pedido.isPendiente()) {
                flash.addFlashAttribute("error", "El pedido #" + id + " ya no está pendiente de cobro.");
                return "redirect:/pedidos/" + id;
            }
            model.addAttribute("pedido", pedido);
            return "pedidos/cobrar";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos";
        }
    }

    @PostMapping("/{id}/cobrar")
    public String cobrar(@PathVariable int id,
            @RequestParam MetodoPago metodo,
            @RequestParam(required = false) BigDecimal montoRecibido,
            @RequestParam(required = false) String numeroTarjeta,
            RedirectAttributes flash) {
        Pago pago = metodo == MetodoPago.EFECTIVO ? new PagoEfectivo(montoRecibido) : new PagoTarjeta(numeroTarjeta);
        try {
            Pedido pedido = pedidoServicio.cobrar(id, pago);
            String puntos = pedido.getPuntosGenerados() > 0
                    ? " Se acreditaron " + pedido.getPuntosGenerados() + " puntos a " + pedido.getNombreCliente() + "."
                    : "";
            flash.addFlashAttribute("mensaje", "Pago aprobado." + puntos);
            return "redirect:/pedidos/" + id + "/comprobante";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage() + " No se registró la venta.");
            return "redirect:/pedidos/" + id + "/cobrar";
        }
    }

    @PostMapping("/{id}/entregar")
    public String entregar(@PathVariable int id, RedirectAttributes flash) {
        try {
            pedidoServicio.entregar(id);
            flash.addFlashAttribute("mensaje", "Pedido #" + id + " marcado como ENTREGADO.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable int id, RedirectAttributes flash) {
        try {
            pedidoServicio.anular(id);
            flash.addFlashAttribute("mensaje", "Pedido #" + id + " anulado. No se afectó inventario ni puntos.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(@PathVariable int id, Model model, RedirectAttributes flash) {
        try {
            Pedido pedido = pedidoServicio.obtener(id);
            if (!pedido.isCobrado()) {
                flash.addFlashAttribute("error", "El pedido #" + id + " aún no ha sido pagado.");
                return "redirect:/pedidos/" + id;
            }
            model.addAttribute("pedido", pedido);
            return "pedidos/comprobante";
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos";
        }
    }
}
