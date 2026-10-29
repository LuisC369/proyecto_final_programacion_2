package com.umg.edu.servicio;

import com.umg.edu.dao.ClienteDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Cliente;
import java.util.List;

public class ClienteServicio {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public List<Cliente> buscar(String texto) {
        return clienteDAO.buscar(texto);
    }

    public List<Cliente> listarActivos() {
        return clienteDAO.listarActivos();
    }

    public List<Cliente> topPorPuntos(int limite) {
        return clienteDAO.topPorPuntos(limite);
    }

    public int contarActivos() {
        return clienteDAO.contarActivos();
    }

    public Cliente obtener(int id) throws NegocioException {
        Cliente cliente = clienteDAO.buscarPorId(id);
        if (cliente == null) {
            throw new NegocioException("No existe el cliente solicitado.");
        }
        return cliente;
    }

    public Cliente buscarPorIdentificador(String identificador) {
        return identificador == null ? null : clienteDAO.buscarPorIdentificador(identificador.trim());
    }

    public void guardar(Cliente cliente) throws NegocioException {
        cliente.setIdentificador(limpiar(cliente.getIdentificador()));
        cliente.setNombre(limpiar(cliente.getNombre()));
        cliente.setApellido(limpiar(cliente.getApellido()));
        cliente.setTelefono(limpiar(cliente.getTelefono()));
        cliente.setEmail(limpiar(cliente.getEmail()));

        if (cliente.getIdentificador().isEmpty() || cliente.getNombre().isEmpty() || cliente.getApellido().isEmpty()) {
            throw new NegocioException("El identificador, el nombre y el apellido son obligatorios.");
        }
        if (!cliente.getIdentificador().matches("[A-Za-z0-9-]{3,20}")) {
            throw new NegocioException("El identificador debe tener de 3 a 20 letras, números o guiones.");
        }
        if (!cliente.getTelefono().isEmpty() && !cliente.getTelefono().matches("[0-9 +-]{8,15}")) {
            throw new NegocioException("El teléfono no tiene un formato válido.");
        }
        if (!cliente.getEmail().isEmpty() && !cliente.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new NegocioException("El correo electrónico no tiene un formato válido.");
        }
        if (clienteDAO.existeIdentificador(cliente.getIdentificador(), cliente.getId())) {
            throw new NegocioException("Ya existe un cliente con el identificador " + cliente.getIdentificador() + ".");
        }

        if (cliente.getId() == 0) {
            cliente.setId(clienteDAO.insertar(cliente));
        } else {
            obtener(cliente.getId());
            clienteDAO.actualizar(cliente);
        }
    }

    public void cambiarEstado(int id, boolean activo) throws NegocioException {
        obtener(id);
        clienteDAO.cambiarEstado(id, activo);
    }

    public void eliminar(int id) throws NegocioException {
        Cliente cliente = obtener(id);
        if (clienteDAO.tienePedidos(id)) {
            throw new NegocioException(cliente.getNombreCompleto()
                    + " tiene pedidos registrados: no se puede eliminar, solo desactivar.");
        }
        clienteDAO.eliminar(id);
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
