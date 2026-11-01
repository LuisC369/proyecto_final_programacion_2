package com.umg.edu.dao;

import com.umg.edu.config.ConexionDB;
import com.umg.edu.excepcion.DatosException;
import com.umg.edu.modelo.reporte.PuntosClienteFila;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

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
}
