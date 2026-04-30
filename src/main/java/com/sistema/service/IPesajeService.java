package com.sistema.service;

import com.sistema.model.Pesaje;
import com.sistema.model.Usuario;
import java.util.List;

public interface IPesajeService {

    // ✅ CORREGIDO: sin precioPorKilo
    void registrar(Long recolectorId, String fecha, String kilos, Usuario usuarioActual);

    void eliminar(Long id);

    List<Pesaje> listarTodos();
}