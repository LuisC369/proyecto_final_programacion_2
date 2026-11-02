package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.MetodoPago;
import com.umg.edu.modelo.reporte.PuntosClienteFila;
import com.umg.edu.modelo.reporte.VentaFila;
import com.umg.edu.modelo.reporte.VentaMenuFila;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO {

    public List<VentaFila> ventasPorPeriodo(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT pe.id_pedido, pa.fecha, pa.metodo, pa.monto, c.nombre, c.apellido "
                + "FROM pagos pa JOIN pedidos pe ON pe.id_pedido = pa.id_pedido "
                + "LEFT JOIN clientes c ON c.id_cliente = pe.id_cliente "
                + "WHERE pa.fecha >= ? AND pa.fecha < ? ORDER BY pa.fecha";
        List<VentaFila> filas = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String cliente = nombre == null ? "Consumidor final" : nombre + " " + rs.getString("apellido");
                    filas.add(new VentaFila(
                            rs.getInt("id_pedido"),
                            rs.getObject("fecha", LocalDateTime.class),
                            cliente,
                            MetodoPago.valueOf(rs.getString("metodo")),
                            rs.getBigDecimal("monto")));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo generar el reporte de ventas.", e);
        }
        return filas;
    }

    public Map<Integer, Integer> cantidadesVendidas(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT d.id_producto, SUM(d.cantidad) AS cantidad "
                + "FROM detalle_pedido d JOIN pagos pa ON pa.id_pedido = d.id_pedido "
                + "WHERE pa.fecha >= ? AND pa.fecha < ? GROUP BY d.id_producto";
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cantidades.put(rs.getInt("id_producto"), rs.getInt("cantidad"));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo generar el reporte de productos vendidos.", e);
        }
        return cantidades;
    }

    public List<PuntosClienteFila> puntosPorCliente() {
        String sql = "SELECT c.identificador, c.nombre, c.apellido, c.activo, c.puntos, "
                + "COALESCE(SUM(CASE WHEN mp.tipo = 'ACUMULACION' THEN mp.puntos END), 0) AS acumulados, "
                + "COALESCE(SUM(CASE WHEN mp.tipo = 'CANJE' THEN -mp.puntos END), 0) AS canjeados "
                + "FROM clientes c LEFT JOIN movimientos_puntos mp ON mp.id_cliente = c.id_cliente "
                + "GROUP BY c.id_cliente, c.identificador, c.nombre, c.apellido, c.activo, c.puntos "
                + "ORDER BY c.puntos DESC, c.nombre";
        List<PuntosClienteFila> filas = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new PuntosClienteFila(
                        rs.getString("identificador"),
                        rs.getString("nombre") + " " + rs.getString("apellido"),
                        rs.getBoolean("activo"),
                        rs.getInt("acumulados"),
                        rs.getInt("canjeados"),
                        rs.getInt("puntos")));
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo generar el reporte de puntos.", e);
        }
        return filas;
    }

    public List<VentaMenuFila> ventasMenus(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT p.codigo, p.nombre, SUM(d.cantidad) AS cantidad, SUM(d.subtotal) AS monto "
                + "FROM detalle_pedido d JOIN productos p ON p.id_producto = d.id_producto "
                + "JOIN pagos pa ON pa.id_pedido = d.id_pedido "
                + "WHERE p.categoria = 'MENU' AND pa.fecha >= ? AND pa.fecha < ? "
                + "GROUP BY p.id_producto, p.codigo, p.nombre ORDER BY cantidad DESC";
        List<VentaMenuFila> filas = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new VentaMenuFila(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("monto")));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo generar el reporte de menús.", e);
        }
        return filas;
    }
}
