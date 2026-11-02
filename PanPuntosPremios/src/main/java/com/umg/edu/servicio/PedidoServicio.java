package com.umg.edu.servicio;

import com.umg.edu.config.Transaccion;
import com.umg.edu.dao.ClienteDAO;
import com.umg.edu.dao.MovimientoInventarioDAO;
import com.umg.edu.dao.MovimientoPuntosDAO;
import com.umg.edu.dao.PagoDAO;
import com.umg.edu.dao.PedidoDAO;
import com.umg.edu.dao.ProductoDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Cliente;
import com.umg.edu.modelo.DetallePedido;
import com.umg.edu.modelo.EstadoPedido;
import com.umg.edu.modelo.Pago;
import com.umg.edu.modelo.Pedido;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.TipoMovimiento;
import com.umg.edu.modelo.TipoMovimientoPuntos;
import com.umg.edu.modelo.reporte.VentaDia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidoServicio {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();
    private final MovimientoPuntosDAO puntosDAO = new MovimientoPuntosDAO();

    public List<Pedido> listar(EstadoPedido estado) {
        return pedidoDAO.listar(estado);
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        return pedidoDAO.listarPorCliente(idCliente);
    }

    public List<Pedido> listarRecientes(int limite) {
        return pedidoDAO.listarRecientes(limite);
    }

    public Map<EstadoPedido, Integer> contarPorEstado() {
        return pedidoDAO.contarPorEstado();
    }

    public BigDecimal ventasDelDia() {
        LocalDate hoy = LocalDate.now();
        return pedidoDAO.ventasPorDia(hoy, hoy).getOrDefault(hoy, BigDecimal.ZERO);
    }

    public List<VentaDia> ventasUltimosDias(int dias) {
        LocalDate hoy = LocalDate.now();
        Map<LocalDate, BigDecimal> ventas = pedidoDAO.ventasPorDia(hoy.minusDays(dias - 1L), hoy);
        BigDecimal maximo = ventas.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEE d", Locale.forLanguageTag("es"));
        List<VentaDia> resultado = new ArrayList<>();
        ventas.forEach((dia, monto) -> {
            int porcentaje = maximo.signum() == 0 ? 0
                    : monto.multiply(BigDecimal.valueOf(100)).divide(maximo, 0, java.math.RoundingMode.HALF_UP).intValue();
            resultado.add(new VentaDia(dia.format(formato), monto, porcentaje));
        });
        return resultado;
    }

    public Pedido obtener(int id) throws NegocioException {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            throw new NegocioException("No existe el pedido #" + id + ".");
        }
        return pedido;
    }

    public Pedido crear(Integer idCliente, Map<Integer, Integer> productos) throws NegocioException {
        if (productos == null || productos.isEmpty()) {
            throw new NegocioException("El pedido debe incluir al menos un producto.");
        }
        return Transaccion.ejecutar(conn -> {
            Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
            Pedido pedido = new Pedido();
            if (idCliente != null) {
                Cliente cliente = clienteDAO.buscarPorId(conn, idCliente, false);
                if (cliente == null) {
                    throw new NegocioException("El cliente seleccionado no existe.");
                }
                if (!cliente.isActivo()) {
                    throw new NegocioException("El cliente " + cliente.getNombreCompleto() + " está desactivado y no puede realizar pedidos.");
                }
                pedido.setCliente(cliente);
            }
            for (Map.Entry<Integer, Integer> item : productos.entrySet()) {
                Producto producto = catalogo.get(item.getKey());
                if (producto == null) {
                    throw new NegocioException("Uno de los productos seleccionados no existe.");
                }
                if (item.getValue() == null || item.getValue() <= 0) {
                    throw new NegocioException("La cantidad de " + producto.getNombre() + " debe ser mayor que cero.");
                }
                if (!producto.isActivoParaVenta()) {
                    throw new NegocioException("El producto " + producto.getNombre() + " está desactivado y no se puede vender.");
                }
                pedido.agregarDetalle(producto, item.getValue());
            }
            Map<Integer, Integer> existencias = new HashMap<>();
            catalogo.forEach((id, producto) -> existencias.put(id, producto.getExistencia()));
            ValidadorInventario.verificar(pedido.requerimientosInventario(), existencias, catalogo);

            pedido.setId(pedidoDAO.insertar(conn, pedido));
            return pedido;
        });
    }

    public Pedido cobrar(int idPedido, Pago pago) throws NegocioException {
        return Transaccion.ejecutar(conn -> {
            if (pedidoDAO.bloquear(conn, idPedido) == null) {
                throw new NegocioException("No existe el pedido #" + idPedido + ".");
            }
            Pedido pedido = pedidoDAO.buscarPorId(conn, idPedido);
            if (!pedido.isPendiente()) {
                throw new NegocioException("Solo se puede cobrar un pedido en estado PENDIENTE.");
            }
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (!detalle.getProducto().isActivoParaVenta()) {
                    throw new NegocioException(detalle.getProducto().getNombre() + " fue desactivado y ya no se puede vender.");
                }
            }

            Map<Integer, Integer> requerimientos = pedido.requerimientosInventario();
            Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
            Map<Integer, Integer> existencias = productoDAO.bloquearExistencias(conn, requerimientos.keySet());
            ValidadorInventario.verificar(requerimientos, existencias, catalogo);

            pago.setIdPedido(idPedido);
            pago.setMonto(pedido.getTotal());
            pago.procesar();

            for (Map.Entry<Integer, Integer> requerimiento : requerimientos.entrySet()) {
                productoDAO.ajustarExistencia(conn, requerimiento.getKey(), -requerimiento.getValue());
                movimientoDAO.registrar(conn, requerimiento.getKey(), TipoMovimiento.VENTA,
                        requerimiento.getValue(), "Pedido #" + idPedido);
            }

            pedido.registrarPago(pago);
            pagoDAO.insertar(conn, pago);
            pedidoDAO.actualizarEstado(conn, idPedido, pedido.getEstado(), pedido.getPuntosGenerados());

            if (pedido.getCliente() != null && pedido.getPuntosGenerados() > 0) {
                clienteDAO.sumarPuntos(conn, pedido.getCliente().getId(), pedido.getPuntosGenerados());
                puntosDAO.registrar(conn, pedido.getCliente().getId(), TipoMovimientoPuntos.ACUMULACION,
                        pedido.getPuntosGenerados(), idPedido, null,
                        "Pedido #" + idPedido + ": " + pedido.getCantidadSandwiches() + " sándwiches x 2 + "
                        + pedido.getCantidadMenus() + " menús x 8");
            }
            return pedido;
        });
    }

    public void entregar(int idPedido) throws NegocioException {
        Transaccion.ejecutar(conn -> {
            if (pedidoDAO.bloquear(conn, idPedido) == null) {
                throw new NegocioException("No existe el pedido #" + idPedido + ".");
            }
            Pedido pedido = pedidoDAO.buscarPorId(conn, idPedido);
            pedido.entregar();
            pedidoDAO.actualizarEstado(conn, idPedido, pedido.getEstado(), pedido.getPuntosGenerados());
            return null;
        });
    }

    public void anular(int idPedido) throws NegocioException {
        Transaccion.ejecutar(conn -> {
            if (pedidoDAO.bloquear(conn, idPedido) == null) {
                throw new NegocioException("No existe el pedido #" + idPedido + ".");
            }
            Pedido pedido = pedidoDAO.buscarPorId(conn, idPedido);
            pedido.anular();
            pedidoDAO.actualizarEstado(conn, idPedido, pedido.getEstado(), 0);
            return null;
        });
    }
}
