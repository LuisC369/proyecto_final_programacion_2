package com.umg.edu.servicio;

import com.umg.edu.config.Transaccion;
import com.umg.edu.dao.MovimientoInventarioDAO;
import com.umg.edu.dao.ProductoDAO;
import com.umg.edu.excepcion.NegocioException;
import com.umg.edu.modelo.Acompanamiento;
import com.umg.edu.modelo.Bebida;
import com.umg.edu.modelo.Categoria;
import com.umg.edu.modelo.Menu;
import com.umg.edu.modelo.Producto;
import com.umg.edu.modelo.ProductoForm;
import com.umg.edu.modelo.Sandwich;
import com.umg.edu.modelo.TipoMovimiento;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductoServicio {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();

    public List<Producto> listar(Categoria categoria) {
        return productoDAO.listarTodos().stream()
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .toList();
    }

    public List<Producto> listarActivos(Categoria categoria) {
        return listar(categoria).stream().filter(Producto::isActivo).toList();
    }

    public List<Producto> listarBajoInventario() {
        return productoDAO.listarTodos().stream()
                .filter(p -> !p.isMenu() && p.isBajoInventario())
                .toList();
    }

    public Producto obtener(int id) throws NegocioException {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            throw new NegocioException("No existe el producto solicitado.");
        }
        return producto;
    }

    public void guardar(ProductoForm form) throws NegocioException {
        String codigo = form.getCodigo() == null ? "" : form.getCodigo().trim().toUpperCase();
        String nombre = form.getNombre() == null ? "" : form.getNombre().trim();
        Categoria categoria = form.getCategoria();

        if (form.getId() != 0) {
            categoria = obtener(form.getId()).getCategoria();
        }
        if (categoria == null) {
            throw new NegocioException("Seleccione la categoría del producto.");
        }
        if (!codigo.matches("[A-Z0-9-]{2,10}")) {
            throw new NegocioException("El código debe tener de 2 a 10 letras, números o guiones.");
        }
        if (nombre.isEmpty()) {
            throw new NegocioException("El nombre del producto es obligatorio.");
        }
        if (form.getPrecio() == null || form.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("El precio es obligatorio y no puede ser negativo.");
        }
        if (form.getExistencia() < 0) {
            throw new NegocioException("La existencia no puede ser negativa.");
        }
        if (productoDAO.existeCodigo(codigo, form.getId())) {
            throw new NegocioException("Ya existe un producto con el código " + codigo + ".");
        }

        Producto producto = Producto.crear(categoria);
        producto.setId(form.getId());
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setPrecio(form.getPrecio());
        producto.setExistencia(categoria == Categoria.MENU ? 0 : form.getExistencia());
        if (producto instanceof Menu menu) {
            armarMenu(menu, form);
        }

        if (producto.getId() == 0) {
            Transaccion.ejecutar(conn -> {
                int id = productoDAO.insertar(conn, producto);
                if (producto.getExistencia() > 0) {
                    movimientoDAO.registrar(conn, id, TipoMovimiento.ENTRADA, producto.getExistencia(), "Existencia inicial");
                }
                return id;
            });
        } else {
            productoDAO.actualizar(producto);
        }
    }

    private void armarMenu(Menu menu, ProductoForm form) throws NegocioException {
        if (form.getIdSandwich() == null || form.getIdBebida() == null || form.getIdAcompanamiento() == null) {
            throw new NegocioException("Un menú debe incluir un sándwich, una bebida y un acompañamiento.");
        }
        Map<Integer, Producto> catalogo = productoDAO.cargarCatalogo();
        if (catalogo.get(form.getIdSandwich()) instanceof Sandwich sandwich
                && catalogo.get(form.getIdBebida()) instanceof Bebida bebida
                && catalogo.get(form.getIdAcompanamiento()) instanceof Acompanamiento acompanamiento) {
            menu.setSandwich(sandwich);
            menu.setBebida(bebida);
            menu.setAcompanamiento(acompanamiento);
        } else {
            throw new NegocioException("Los componentes del menú no corresponden a un sándwich, una bebida y un acompañamiento.");
        }
    }

    public void cambiarEstado(int id, boolean activo) throws NegocioException {
        obtener(id);
        productoDAO.cambiarEstado(id, activo);
    }
}
