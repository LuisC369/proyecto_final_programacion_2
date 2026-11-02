package com.umg.edu.controlador;

import com.umg.edu.modelo.EstadoPedido;
import com.umg.edu.servicio.ClienteServicio;
import com.umg.edu.servicio.PedidoServicio;
import com.umg.edu.servicio.ProductoServicio;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {

    private final PedidoServicio pedidoServicio = new PedidoServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();
    private final ClienteServicio clienteServicio = new ClienteServicio();

    @GetMapping("/")
    public String inicio(Model model) {
        Map<EstadoPedido, Integer> conteo = pedidoServicio.contarPorEstado();
        model.addAttribute("ventasHoy", pedidoServicio.ventasDelDia());
        model.addAttribute("pendientes", conteo.getOrDefault(EstadoPedido.PENDIENTE, 0));
        model.addAttribute("porEntregar", conteo.getOrDefault(EstadoPedido.PAGADO, 0));
        model.addAttribute("clientesActivos", clienteServicio.contarActivos());
        model.addAttribute("bajoInventario", productoServicio.listarBajoInventario());
        model.addAttribute("ventasSemana", pedidoServicio.ventasUltimosDias(7));
        model.addAttribute("recientes", pedidoServicio.listarRecientes(6));
        model.addAttribute("topClientes", clienteServicio.topPorPuntos(5));
        return "index";
    }
}
