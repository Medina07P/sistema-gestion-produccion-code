package com.sistema.service;

import com.sistema.model.Pesaje;
import java.util.List;

public interface IPesajeService {
    void registrar(Long recolectorId, Long loteId, String fecha, String kilos);
    void eliminar(Long id);
    List<Pesaje> listarTodos();
}
