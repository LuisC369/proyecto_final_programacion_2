package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.Recompensa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecompensaDAO implements CrudDAO<Recompensa> {

    private static final String SELECT = "SELECT id_recompensa, nombre, puntos_requeridos, id_producto, activo FROM recompensas";

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public List<Recompensa> listarTodos() {
        List<Recompensa> recompensas = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion()) {
            Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
            try (PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY puntos_requeridos");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recompensas.add(mapear(rs, catalogo));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar las recompensas.", e);
        }
        return recompensas;
    }

    @Override
    public Recompensa buscarPorId(int id) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar la recompensa.", e);
        }
    }

    public Recompensa buscarPorId(Connection conn, int id) throws SQLException {
        Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
        try (PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE id_recompensa = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs, catalogo) : null;
            }
        }
    }

    @Override
    public int insertar(Recompensa recompensa) {
        String sql = "INSERT INTO recompensas (nombre, puntos_requeridos, id_producto, activo) VALUES (?, ?, ?, 1)";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, recompensa.getNombre());
            ps.setInt(2, recompensa.getPuntosRequeridos());
            ps.setInt(3, recompensa.getIdProducto());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar la recompensa.", e);
        }
    }

    @Override
    public boolean actualizar(Recompensa recompensa) {
        String sql = "UPDATE recompensas SET nombre = ?, puntos_requeridos = ?, id_producto = ? WHERE id_recompensa = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recompensa.getNombre());
            ps.setInt(2, recompensa.getPuntosRequeridos());
            ps.setInt(3, recompensa.getIdProducto());
            ps.setInt(4, recompensa.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar la recompensa.", e);
        }
    }

    public boolean cambiarEstado(int id, boolean activo) {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("UPDATE recompensas SET activo = ? WHERE id_recompensa = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo cambiar el estado de la recompensa.", e);
        }
    }

    private Recompensa mapear(ResultSet rs, Map<Integer, Producto> catalogo) throws SQLException {
        Recompensa recompensa = new Recompensa();
        recompensa.setId(rs.getInt("id_recompensa"));
        recompensa.setNombre(rs.getString("nombre"));
        recompensa.setPuntosRequeridos(rs.getInt("puntos_requeridos"));
        recompensa.setIdProducto(rs.getInt("id_producto"));
        recompensa.setProducto(catalogo.get(rs.getInt("id_producto")));
        recompensa.setActivo(rs.getBoolean("activo"));
        return recompensa;
    }
}
