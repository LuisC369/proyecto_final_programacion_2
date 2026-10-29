package com.umg.edu.dao;

import java.util.List;

public interface CrudDAO<T> {

    int insertar(T objeto);

    boolean actualizar(T objeto);

    T buscarPorId(int id);

    List<T> listarTodos();
}
