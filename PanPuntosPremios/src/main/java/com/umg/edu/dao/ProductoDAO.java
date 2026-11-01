package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.Acompanamiento;
import com.umg.edu.modelo.Bebida;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.Menu;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.Sandwich;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductoDAO implements CrudDAO<Producto> {

    private static final String CONSULTA_CATALOGO = "SELECT p.id_producto, p.codigo, p.nombre, p.categoria, "
            + "p.precio, p.existencia, p.activo, m.id_sandwich, m.id_bebida, m.id_acompanamiento "
            + "FROM productos p LEFT JOIN menu_componentes m ON m.id_menu = p.id_producto "
            + "ORDER BY p.categoria, p.codigo";

    public Map<Integer, Producto> cargarCatalogo(Connection conn) throws SQLException {
        Map<Integer, Producto> catalogo = new LinkedHashMap<>();
        Map<Menu, int[]> componentes = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(CONSULTA_CATALOGO);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto producto = mapear(rs);
                catalogo.put(producto.getId(), producto);
                if (producto instanceof Menu menu) {
                    componentes.put(menu, new int[]{
                        rs.getInt("id_sandwich"), rs.getInt("id_bebida"), rs.getInt("id_acompanamiento")});
                }
            }
        }
        componentes.forEach((menu, ids) -> {
            if (catalogo.get(ids[0]) instanceof Sandwich sandwich) {
                menu.setSandwich(sandwich);
            }
            if (catalogo.get(ids[1]) instanceof Bebida bebida) {
                menu.setBebida(bebida);
            }
            if (catalogo.get(ids[2]) instanceof Acompanamiento acompanamiento) {
                menu.setAcompanamiento(acompanamiento);
            }
        });
        return catalogo;
    }

    public Map<Integer, Producto> cargarCatalogo() {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return cargarCatalogo(conn);
        } catch (SQLException e) {
            throw new DatosException("No se pudo cargar el catálogo de productos.", e);
        }
    }

    @Override
    public List<Producto> listarTodos() {
        return new ArrayList<>(cargarCatalogo().values());
    }

    @Override
    public Producto buscarPorId(int id) {
        return cargarCatalogo().get(id);
    }

    public boolean existeCodigo(String codigo, int excluirId) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ? AND id_producto <> ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo validar el código del producto.", e);
        }
    }

    @Override
    public int insertar(Producto producto) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                int id = insertar(conn, producto);
                conn.commit();
                return id;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar el producto.", e);
        }
    }

    public int insertar(Connection conn, Producto producto) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, categoria, precio, existencia, activo) VALUES (?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria().name());
            ps.setBigDecimal(4, producto.getPrecio());
            ps.setInt(5, producto.isMenu() ? 0 : producto.getExistencia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                producto.setId(rs.getInt(1));
            }
        }
        if (producto instanceof Menu menu) {
            String sqlMenu = "INSERT INTO menu_componentes (id_menu, id_sandwich, id_bebida, id_acompanamiento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlMenu)) {
                ps.setInt(1, menu.getId());
                ps.setInt(2, menu.getSandwich().getId());
                ps.setInt(3, menu.getBebida().getId());
                ps.setInt(4, menu.getAcompanamiento().getId());
                ps.executeUpdate();
            }
        }
        return producto.getId();
    }

    @Override
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, precio = ? WHERE id_producto = ?";
        try (Connection conn = ConexionDB.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, producto.getCodigo());
                ps.setString(2, producto.getNombre());
                ps.setBigDecimal(3, producto.getPrecio());
                ps.setInt(4, producto.getId());
                int filas = ps.executeUpdate();
                if (producto instanceof Menu menu) {
                    String sqlMenu = "UPDATE menu_componentes SET id_sandwich = ?, id_bebida = ?, id_acompanamiento = ? WHERE id_menu = ?";
                    try (PreparedStatement psMenu = conn.prepareStatement(sqlMenu)) {
                        psMenu.setInt(1, menu.getSandwich().getId());
                        psMenu.setInt(2, menu.getBebida().getId());
                        psMenu.setInt(3, menu.getAcompanamiento().getId());
                        psMenu.setInt(4, menu.getId());
                        psMenu.executeUpdate();
                    }
                }
                conn.commit();
                return filas == 1;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar el producto.", e);
        }
    }

    public boolean cambiarEstado(int id, boolean activo) {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("UPDATE productos SET activo = ? WHERE id_producto = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo cambiar el estado del producto.", e);
        }
    }

    public Map<Integer, Integer> bloquearExistencias(Connection conn, Collection<Integer> ids) throws SQLException {
        Map<Integer, Integer> existencias = new HashMap<>();
        if (ids.isEmpty()) {
            return existencias;
        }
        String marcadores = String.join(", ", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id_producto, existencia FROM productos WHERE id_producto IN (" + marcadores + ") FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int indice = 1;
            for (Integer id : ids) {
                ps.setInt(indice++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existencias.put(rs.getInt("id_producto"), rs.getInt("existencia"));
                }
            }
        }
        return existencias;
    }

    public void ajustarExistencia(Connection conn, int idProducto, int cantidad) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE productos SET existencia = existencia + ? WHERE id_producto = ?")) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto producto = Producto.crear(Categoria.valueOf(rs.getString("categoria")));
        producto.setId(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setExistencia(rs.getInt("existencia"));
        producto.setActivo(rs.getBoolean("activo"));
        return producto;
    }
}
