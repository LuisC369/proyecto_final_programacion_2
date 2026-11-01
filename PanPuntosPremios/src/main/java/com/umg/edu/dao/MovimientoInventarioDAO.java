package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.MovimientoInventario;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.TipoMovimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAO {

    public void registrar(Connection conn, int idProducto, TipoMovimiento tipo, int cantidad, String referencia)
            throws SQLException {
        String sql = "INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, referencia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, tipo.name());
            ps.setInt(3, cantidad);
            ps.setString(4, referencia);
            ps.executeUpdate();
        }
    }

    public List<MovimientoInventario> listar(Integer idProducto) {
        String sql = "SELECT mi.id_movimiento, mi.tipo, mi.cantidad, mi.fecha, mi.referencia, "
                + "p.id_producto, p.codigo, p.nombre, p.categoria "
                + "FROM movimientos_inventario mi JOIN productos p ON p.id_producto = mi.id_producto "
                + (idProducto != null ? "WHERE mi.id_producto = ? " : "")
                + "ORDER BY mi.fecha DESC, mi.id_movimiento DESC LIMIT 300";
        List<MovimientoInventario> movimientos = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idProducto != null) {
                ps.setInt(1, idProducto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = Producto.crear(Categoria.valueOf(rs.getString("categoria")));
                    producto.setId(rs.getInt("id_producto"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.setId(rs.getInt("id_movimiento"));
                    movimiento.setProducto(producto);
                    movimiento.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
                    movimiento.setCantidad(rs.getInt("cantidad"));
                    movimiento.setFecha(rs.getObject("fecha", LocalDateTime.class));
                    movimiento.setReferencia(rs.getString("referencia"));
                    movimientos.add(movimiento);
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el historial de inventario.", e);
        }
        return movimientos;
    }
}
