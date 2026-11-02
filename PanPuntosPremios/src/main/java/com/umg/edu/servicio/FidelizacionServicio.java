package com.umg.edu.servicio;

import com.umg.edu.config.Transaccion;
import com.umg.edu.dao.CanjeDAO;
import com.umg.edu.dao.ClienteDAO;
import com.umg.edu.dao.MovimientoInventarioDAO;
import com.umg.edu.dao.MovimientoPuntosDAO;
import com.umg.edu.dao.ProductoDAO;
import com.umg.edu.dao.RecompensaDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.excepcion.PuntosInsuficientesException;
import com.umg.edu.modelo.Canje;
import com.umg.edu.modelo.Cliente;
import com.umg.edu.modelo.MovimientoPuntos;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.Recompensa;
import com.umg.edu.modelo.TipoMovimiento;
import com.umg.edu.modelo.TipoMovimientoPuntos;
import java.util.List;
import java.util.Map;

public class FidelizacionServicio {

    private final RecompensaDAO recompensaDAO = new RecompensaDAO();
    private final CanjeDAO canjeDAO = new CanjeDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();
    private final MovimientoPuntosDAO puntosDAO = new MovimientoPuntosDAO();

    public List<Recompensa> listarRecompensas() {
        return recompensaDAO.listarTodos();
    }

    public Recompensa obtenerRecompensa(int id) throws NegocioException {
        Recompensa recompensa = recompensaDAO.buscarPorId(id);
        if (recompensa == null) {
            throw new NegocioException("No existe la recompensa solicitada.");
        }
        return recompensa;
    }

    public List<Canje> historialCanjes() {
        return canjeDAO.listarTodos();
    }

    public List<Canje> canjesDe(int idCliente) {
        return canjeDAO.listarPorCliente(idCliente);
    }

    public List<MovimientoPuntos> movimientosDe(int idCliente) {
        return puntosDAO.listarPorCliente(idCliente);
    }

    public void guardarRecompensa(Recompensa recompensa) throws NegocioException {
        String nombre = recompensa.getNombre() == null ? "" : recompensa.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new NegocioException("El nombre de la recompensa es obligatorio.");
        }
        if (recompensa.getPuntosRequeridos() <= 0) {
            throw new NegocioException("Los puntos requeridos deben ser mayores que cero.");
        }
        if (productoDAO.buscarPorId(recompensa.getIdProducto()) == null) {
            throw new NegocioException("Seleccione el producto que se entregará como recompensa.");
        }
        recompensa.setNombre(nombre);
        if (recompensa.getId() == 0) {
            recompensaDAO.insertar(recompensa);
        } else {
            obtenerRecompensa(recompensa.getId());
            recompensaDAO.actualizar(recompensa);
        }
    }

    public void cambiarEstadoRecompensa(int id, boolean activo) throws NegocioException {
        obtenerRecompensa(id);
        recompensaDAO.cambiarEstado(id, activo);
    }

    public Canje canjear(int idCliente, int idRecompensa) throws NegocioException {
        return Transaccion.ejecutar(conn -> {
            Cliente cliente = clienteDAO.buscarPorId(conn, idCliente, true);
            if (cliente == null) {
                throw new NegocioException("El cliente no existe.");
            }
            if (!cliente.isActivo()) {
                throw new NegocioException("El cliente " + cliente.getNombreCompleto() + " está desactivado y no puede canjear puntos.");
            }
            Recompensa recompensa = recompensaDAO.buscarPorId(conn, idRecompensa);
            if (recompensa == null) {
                throw new NegocioException("La recompensa no existe.");
            }
            if (!recompensa.isActivo()) {
                throw new NegocioException("La recompensa " + recompensa.getNombre() + " no está activa.");
            }
            if (!cliente.tieneSaldo(recompensa.getPuntosRequeridos())) {
                throw new PuntosInsuficientesException(cliente.getPuntos(), recompensa.getPuntosRequeridos());
            }

            Producto producto = recompensa.getProducto();
            Map<Integer, Integer> requerimientos = producto.requerimientos(1);
            Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo(conn);
            Map<Integer, Integer> existencias = productoDAO.bloquearExistencias(conn, requerimientos.keySet());
            ValidadorInventario.verificar(requerimientos, existencias, catalogo);

            Canje canje = new Canje(cliente, recompensa);
            int idCanje = canjeDAO.insertar(conn, canje);

            for (Map.Entry<Integer, Integer> requerimiento : requerimientos.entrySet()) {
                productoDAO.ajustarExistencia(conn, requerimiento.getKey(), -requerimiento.getValue());
                movimientoDAO.registrar(conn, requerimiento.getKey(), TipoMovimiento.CANJE,
                        requerimiento.getValue(), "Canje #" + idCanje + ": " + recompensa.getNombre());
            }

            clienteDAO.sumarPuntos(conn, idCliente, -recompensa.getPuntosRequeridos());
            puntosDAO.registrar(conn, idCliente, TipoMovimientoPuntos.CANJE, -recompensa.getPuntosRequeridos(),
                    null, idCanje, "Canje: " + recompensa.getNombre());
            cliente.setPuntos(cliente.getPuntos() - recompensa.getPuntosRequeridos());
            return canje;
        });
    }
}
