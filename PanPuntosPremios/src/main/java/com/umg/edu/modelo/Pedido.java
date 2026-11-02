package com.umg.edu.modelo;

import com.umg.edu.excepcion.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Pedido {

    private int id;
    private Cliente cliente;
    private LocalDateTime fecha;
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private final List<DetallePedido> detalles = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private int puntosGenerados;
    private Pago pago;

    public void agregarDetalle(Producto producto, int cantidad) {
        agregarDetalle(new DetallePedido(producto, cantidad, producto.getPrecio()));
    }

    public void agregarDetalle(DetallePedido detalle) {
        Optional<DetallePedido> existente = detalles.stream()
                .filter(d -> d.getProducto().getId() == detalle.getProducto().getId())
                .findFirst();
        if (existente.isPresent()) {
            existente.get().aumentar(detalle.getCantidad());
        } else {
            detalles.add(detalle);
        }
        total = detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCantidadSandwiches() {
        return contar(Categoria.SANDWICH);
    }

    public int getCantidadMenus() {
        return contar(Categoria.MENU);
    }

    public int getCantidadArticulos() {
        return detalles.stream().mapToInt(DetallePedido::getCantidad).sum();
    }

    private int contar(Categoria categoria) {
        return detalles.stream()
                .filter(d -> d.getProducto().getCategoria() == categoria)
                .mapToInt(DetallePedido::getCantidad)
                .sum();
    }

    public int calcularPuntos() {
        return detalles.stream().mapToInt(DetallePedido::getPuntos).sum();
    }

    public Map<Integer, Integer> requerimientosInventario() {
        Map<Integer, Integer> requerimientos = new LinkedHashMap<>();
        for (DetallePedido detalle : detalles) {
            detalle.getProducto().requerimientos(detalle.getCantidad())
                    .forEach((idProducto, cantidad) -> requerimientos.merge(idProducto, cantidad, Integer::sum));
        }
        return requerimientos;
    }

    public void registrarPago(Pago pagoAprobado) throws NegocioException {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new NegocioException("Solo se puede cobrar un pedido en estado PENDIENTE.");
        }
        this.pago = pagoAprobado;
        this.puntosGenerados = cliente == null ? 0 : calcularPuntos();
        this.estado = EstadoPedido.PAGADO;
    }

    public void entregar() throws NegocioException {
        if (estado != EstadoPedido.PAGADO) {
            throw new NegocioException("Solo un pedido PAGADO puede marcarse como ENTREGADO.");
        }
        estado = EstadoPedido.ENTREGADO;
    }

    public void anular() throws NegocioException {
        switch (estado) {
            case PAGADO, ENTREGADO ->
                throw new NegocioException("Un pedido ya pagado no puede anularse desde el módulo de ventas.");
            case ANULADO ->
                throw new NegocioException("El pedido ya se encuentra anulado.");
            default ->
                estado = EstadoPedido.ANULADO;
        }
    }

    public boolean isPendiente() {
        return estado == EstadoPedido.PENDIENTE;
    }

    public boolean isPagado() {
        return estado == EstadoPedido.PAGADO;
    }

    public boolean isCobrado() {
        return estado == EstadoPedido.PAGADO || estado == EstadoPedido.ENTREGADO;
    }

    public String getNombreCliente() {
        return cliente == null ? "Consumidor final" : cliente.getNombreCompleto();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
