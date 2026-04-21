package com.sistema.service;

import com.sistema.model.Pesaje;
import com.sistema.model.Usuario;
import java.util.List;

public interface IPesajeService {
    void registrar(Long recolectorId, Long loteId, String fecha, String kilos, Usuario usuarioActual);
    void eliminar(Long id);
    List<Pesaje> listarTodos();
}
