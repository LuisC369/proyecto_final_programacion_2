package com.umg.edu.config;

import com.umg.edu.excepcion.DatosException;
import com.umg.edu.excepcion.NegocioException;
import java.sql.Connection;
import java.sql.SQLException;

public final class Transaccion {

    @FunctionalInterface
    public interface Operacion<T> {

        T ejecutar(Connection conn) throws SQLException, NegocioException;
    }

    private Transaccion() {
    }

    public static <T> T ejecutar(Operacion<T> operacion) throws NegocioException {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | NegocioException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos: " + e.getMessage(), e);
        }
    }
}
