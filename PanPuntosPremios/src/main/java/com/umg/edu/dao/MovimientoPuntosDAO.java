package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.MovimientoPuntos;
import com.umg.edu.modelo.TipoMovimientoPuntos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoPuntosDAO {

    public void registrar(Connection conn, int idCliente, TipoMovimientoPuntos tipo, int puntos,
            Integer idPedido, Integer idCanje, String descripcion) throws SQLException {
        String sql = "INSERT INTO movimientos_puntos (id_cliente, tipo, puntos, id_pedido, id_canje, descripcion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, tipo.name());
            ps.setInt(3, puntos);
            if (idPedido == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, idPedido);
            }
            if (idCanje == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, idCanje);
            }
            ps.setString(6, descripcion);
            ps.executeUpdate();
        }
    }

    public List<MovimientoPuntos> listarPorCliente(int idCliente) {
        String sql = "SELECT id_movimiento, id_cliente, tipo, puntos, fecha, id_pedido, descripcion "
                + "FROM movimientos_puntos WHERE id_cliente = ? ORDER BY fecha DESC, id_movimiento DESC";
        List<MovimientoPuntos> movimientos = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoPuntos movimiento = new MovimientoPuntos();
                    movimiento.setId(rs.getInt("id_movimiento"));
                    movimiento.setIdCliente(rs.getInt("id_cliente"));
                    movimiento.setTipo(TipoMovimientoPuntos.valueOf(rs.getString("tipo")));
                    movimiento.setPuntos(rs.getInt("puntos"));
                    movimiento.setFecha(rs.getObject("fecha", LocalDateTime.class));
                    int idPedido = rs.getInt("id_pedido");
                    movimiento.setIdPedido(rs.wasNull() ? null : idPedido);
                    movimiento.setDescripcion(rs.getString("descripcion"));
                    movimientos.add(movimiento);
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar los movimientos de puntos.", e);
        }
        return movimientos;
    }
}
