package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements CrudDAO<Cliente> {

    private static final String SELECT = "SELECT id_cliente, identificador, nombre, apellido, telefono, email, "
            + "puntos, activo, fecha_registro FROM clientes";

    @Override
    public int insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes (identificador, nombre, apellido, telefono, email, puntos, activo) "
                + "VALUES (?, ?, ?, ?, ?, 0, 1)";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getIdentificador());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getApellido());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar el cliente.", e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET identificador = ?, nombre = ?, apellido = ?, telefono = ?, email = ? "
                + "WHERE id_cliente = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getIdentificador());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getApellido());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getEmail());
            ps.setInt(6, cliente.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar el cliente.", e);
        }
    }

    @Override
    public Cliente buscarPorId(int id) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return buscarPorId(conn, id, false);
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el cliente.", e);
        }
    }

    public Cliente buscarPorId(Connection conn, int id, boolean bloquear) throws SQLException {
        String sql = SELECT + " WHERE id_cliente = ?" + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Cliente buscarPorIdentificador(String identificador) {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE identificador = ?")) {
            ps.setString(1, identificador);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar el cliente.", e);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        return buscar("");
    }

    public List<Cliente> buscar(String texto) {
        String sql = SELECT + " WHERE identificador LIKE ? OR CONCAT(nombre, ' ', apellido) LIKE ? "
                + "ORDER BY activo DESC, nombre, apellido";
        List<Cliente> clientes = new ArrayList<>();
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar los clientes.", e);
        }
        return clientes;
    }

    public List<Cliente> listarActivos() {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE activo = 1 ORDER BY nombre, apellido");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar los clientes.", e);
        }
        return clientes;
    }

    public List<Cliente> topPorPuntos(int limite) {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE activo = 1 ORDER BY puntos DESC, nombre LIMIT ?")) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo listar los clientes.", e);
        }
        return clientes;
    }

    public boolean existeIdentificador(String identificador, int excluirId) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE identificador = ? AND id_cliente <> ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificador);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo validar el identificador.", e);
        }
    }

    public boolean tienePedidos(int idCliente) {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE id_cliente = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo consultar los pedidos del cliente.", e);
        }
    }

    public boolean cambiarEstado(int id, boolean activo) {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("UPDATE clientes SET activo = ? WHERE id_cliente = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo cambiar el estado del cliente.", e);
        }
    }

    public boolean eliminar(int id) {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM clientes WHERE id_cliente = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No se pudo eliminar el cliente.", e);
        }
    }

    public int contarActivos() {
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM clientes WHERE activo = 1");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatosException("No se pudo contar los clientes.", e);
        }
    }

    public void sumarPuntos(Connection conn, int idCliente, int puntos) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE clientes SET puntos = puntos + ? WHERE id_cliente = ?")) {
            ps.setInt(1, puntos);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    static Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("identificador"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getInt("puntos"),
                rs.getBoolean("activo"),
                rs.getObject("fecha_registro", LocalDateTime.class)
        );
    }
}
