package com.sistema.service;

import com.sistema.model.Recolector;
import java.util.List;

public interface IRecolectorService {
    void crear(String nombre, String cedula, Long loteId);
    void actualizar(Recolector r, String nombre, String cedula, Long loteId);
    void desactivar(Long id);
    List<Recolector> listarTodos();
    List<Recolector> listarActivos();
    Recolector buscarPorId(Long id);
}
