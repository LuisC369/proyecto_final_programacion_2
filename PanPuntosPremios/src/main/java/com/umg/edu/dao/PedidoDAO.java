package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.DetallePedido;
import com.umg.edu.modelo.EstadoPedido;
import com.umg.edu.modelo.Pedido;
import com.umg.edu.modelo.Producto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoDAO {

    private static final String SELECT = "SELECT pe.id_pedido, pe.fecha, pe.estado, pe.total, pe.puntos_generados, "
            + "c.id_cliente, c.identificador, c.nombre, c.apellido, c.telefono, c.email, c.puntos, c.activo, c.fecha_registro "
            + "FROM pedidos pe LEFT JOIN clientes c ON c.id_cliente = pe.id_cliente";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    public int insertar(Connection conn, Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (id_cliente, estado, total, puntos_generados) VALUES (?, 'PENDIENTE', ?, 0)";
        int idPedido;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (pedido.getCliente() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, pedido.getCliente().getId());
            }
            ps.setBigDecimal(2, pedido.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                idPedido = rs.getInt(1);
            }
        }
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                ps.setInt(1, idPedido);
                ps.setInt(2, detalle.getProducto().getId());
                ps.setInt(3, detalle.getCantidad());
                ps.setBigDecimal(4, detalle.getPrecioUnitario());
                ps.setBigDecimal(5, detalle.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return idPedido;
    }

    public Pedido buscarPorId(int id) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el pedido.", e);
        }
    }

    public Pedido buscarPorId(Connection conn, int id) throws SQLException {
        Pedido pedido;
        try (PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE pe.id_pedido = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                pedido = mapear(rs);
            }
        }
        Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
        String sqlDetalle = "SELECT id_detalle, id_producto, cantidad, precio_unitario FROM detalle_pedido "
                + "WHERE id_pedido = ? ORDER BY id_detalle";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido detalle = new DetallePedido(
                            catalogo.get(rs.getInt("id_producto")),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"));
                    detalle.setId(rs.getInt("id_detalle"));
                    pedido.agregarDetalle(detalle);
                }
            }
        }
        pedido.setPago(pagoDAO.buscarPorPedido(conn, id));
        return pedido;
    }

    public EstadoPedido bloquear(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT estado FROM pedidos WHERE id_pedido = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? EstadoPedido.valueOf(rs.getString("estado")) : null;
            }
        }
    }

    public void actualizarEstado(Connection conn, int id, EstadoPedido estado, int puntosGenerados) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pedidos SET estado = ?, puntos_generados = ? WHERE id_pedido = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, puntosGenerados);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public List<Pedido> listar(EstadoPedido estado) {
        String sql = SELECT + (estado != null ? " WHERE pe.estado = ?" : "") + " ORDER BY pe.fecha DESC, pe.id_pedido DESC";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (estado != null) {
                ps.setString(1, estado.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar los pedidos.", e);
        }
        return pedidos;
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE pe.id_cliente = ? ORDER BY pe.fecha DESC")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el historial de pedidos.", e);
        }
        return pedidos;
    }

    public List<Pedido> listarRecientes(int limite) {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY pe.fecha DESC, pe.id_pedido DESC LIMIT ?")) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar los pedidos recientes.", e);
        }
        return pedidos;
    }

    public Map<EstadoPedido, Integer> contarPorEstado() {
        Map<EstadoPedido, Integer> conteo = new EnumMap<>(EstadoPedido.class);
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("SELECT estado, COUNT(*) AS total FROM pedidos GROUP BY estado");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(EstadoPedido.valueOf(rs.getString("estado")), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo contar los pedidos.", e);
        }
        return conteo;
    }

    public Map<LocalDate, BigDecimal> ventasPorDia(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, BigDecimal> ventas = new LinkedHashMap<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            ventas.put(dia, BigDecimal.ZERO);
        }
        String sql = "SELECT DATE(fecha) AS dia, SUM(monto) AS total FROM pagos WHERE fecha >= ? AND fecha < ? GROUP BY DATE(fecha)";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.put(rs.getObject("dia", LocalDate.class), rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar las ventas.", e);
        }
        return ventas;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id_pedido"));
        pedido.setFecha(rs.getObject("fecha", LocalDateTime.class));
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
        pedido.setTotal(rs.getBigDecimal("total"));
        pedido.setPuntosGenerados(rs.getInt("puntos_generados"));
        rs.getInt("id_cliente");
        if (!rs.wasNull()) {
            pedido.setCliente(ClienteDAO.mapear(rs));
        }
        return pedido;
    }
}
