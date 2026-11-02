package com.umg.edu.dao;

import com.umg.edu.modelo.MetodoPago;
import com.umg.edu.modelo.Pago;
import com.umg.edu.modelo.PagoEfectivo;
import com.umg.edu.modelo.PagoTarjeta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;

public class PagoDAO {

    public void insertar(Connection conn, Pago pago) throws SQLException {
        String sql = "INSERT INTO pagos (id_pedido, metodo, monto, monto_recibido, cambio, referencia, ultimos_digitos, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getIdPedido());
            ps.setString(2, pago.getMetodo().name());
            ps.setBigDecimal(3, pago.getMonto());
            ps.setNull(4, Types.DECIMAL);
            ps.setNull(5, Types.DECIMAL);
            ps.setNull(6, Types.VARCHAR);
            ps.setNull(7, Types.CHAR);
            if (pago instanceof PagoEfectivo efectivo) {
                ps.setBigDecimal(4, efectivo.getMontoRecibido());
                ps.setBigDecimal(5, efectivo.getCambio());
            } else if (pago instanceof PagoTarjeta tarjeta) {
                ps.setString(6, tarjeta.getReferencia());
                ps.setString(7, tarjeta.getUltimosDigitos());
            }
            ps.setObject(8, pago.getFecha());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setId(rs.getInt(1));
                }
            }
        }
    }

    public Pago buscarPorPedido(Connection conn, int idPedido) throws SQLException {
        String sql = "SELECT id_pago, id_pedido, metodo, monto, monto_recibido, cambio, referencia, ultimos_digitos, fecha "
                + "FROM pagos WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pago pago;
                if (MetodoPago.valueOf(rs.getString("metodo")) == MetodoPago.EFECTIVO) {
                    PagoEfectivo efectivo = new PagoEfectivo(rs.getBigDecimal("monto_recibido"));
                    efectivo.setCambio(rs.getBigDecimal("cambio"));
                    pago = efectivo;
                } else {
                    PagoTarjeta tarjeta = new PagoTarjeta();
                    tarjeta.setReferencia(rs.getString("referencia"));
                    tarjeta.setUltimosDigitos(rs.getString("ultimos_digitos"));
                    pago = tarjeta;
                }
                pago.setId(rs.getInt("id_pago"));
                pago.setIdPedido(rs.getInt("id_pedido"));
                pago.setMonto(rs.getBigDecimal("monto"));
                pago.setFecha(rs.getObject("fecha", LocalDateTime.class));
                return pago;
            }
        }
    }
}
