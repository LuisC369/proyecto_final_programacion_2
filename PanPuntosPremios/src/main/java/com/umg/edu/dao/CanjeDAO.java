package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.Canje;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.Recompensa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CanjeDAO {

    private static final String SELECT = "SELECT ca.id_canje, ca.puntos_usados, ca.fecha, "
            + "c.id_cliente, c.identificador, c.nombre, c.apellido, c.telefono, c.email, c.puntos, c.activo, c.fecha_registro, "
            + "r.id_recompensa, r.nombre AS nombre_recompensa, r.puntos_requeridos, r.activo AS recompensa_activa, "
            + "p.id_producto, p.codigo AS codigo_producto, p.nombre AS nombre_producto, p.categoria "
            + "FROM canjes ca "
            + "JOIN clientes c ON c.id_cliente = ca.id_cliente "
            + "JOIN recompensas r ON r.id_recompensa = ca.id_recompensa "
            + "JOIN productos p ON p.id_producto = r.id_producto";

    public int insertar(Connection conn, Canje canje) throws SQLException {
        String sql = "INSERT INTO canjes (id_cliente, id_recompensa, puntos_usados, fecha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, canje.getCliente().getId());
            ps.setInt(2, canje.getRecompensa().getId());
            ps.setInt(3, canje.getPuntosUsados());
            ps.setObject(4, canje.getFecha());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                canje.setId(rs.getInt(1));
                return canje.getId();
            }
        }
    }

    public List<Canje> listar(LocalDate desde, LocalDate hasta) {
        String sql = SELECT + " WHERE ca.fecha >= ? AND ca.fecha < ? ORDER BY ca.fecha DESC";
        List<Canje> canjes = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    canjes.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el historial de canjes.", e);
        }
        return canjes;
    }

    public List<Canje> listarTodos() {
        return listarConFiltro(SELECT + " ORDER BY ca.fecha DESC", null);
    }

    public List<Canje> listarPorCliente(int idCliente) {
        return listarConFiltro(SELECT + " WHERE ca.id_cliente = ? ORDER BY ca.fecha DESC", idCliente);
    }

    private List<Canje> listarConFiltro(String sql, Integer parametro) {
        List<Canje> canjes = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    canjes.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar los canjes.", e);
        }
        return canjes;
    }

    private Canje mapear(ResultSet rs) throws SQLException {
        Producto producto = Producto.crear(Categoria.valueOf(rs.getString("categoria")));
        producto.setId(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo_producto"));
        producto.setNombre(rs.getString("nombre_producto"));

        Recompensa recompensa = new Recompensa();
        recompensa.setId(rs.getInt("id_recompensa"));
        recompensa.setNombre(rs.getString("nombre_recompensa"));
        recompensa.setPuntosRequeridos(rs.getInt("puntos_requeridos"));
        recompensa.setActivo(rs.getBoolean("recompensa_activa"));
        recompensa.setProducto(producto);

        Canje canje = new Canje();
        canje.setId(rs.getInt("id_canje"));
        canje.setCliente(ClienteDAO.mapear(rs));
        canje.setRecompensa(recompensa);
        canje.setPuntosUsados(rs.getInt("puntos_usados"));
        canje.setFecha(rs.getObject("fecha", LocalDateTime.class));
        return canje;
    }
}
