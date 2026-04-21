// ─── ILoteService.java ────────────────────────────────────────
package com.sistema.service;

import com.sistema.model.Lote;
import java.util.List;

/**
 * Contrato del servicio de Lotes.
 * La UI sólo conoce esta interfaz, nunca la implementación concreta.
 * Principio DIP aplicado.
 */
public interface ILoteService {
    void crear(String codigo, String descripcion);
    void actualizar(Lote lote, String nuevoCodigo, String nuevaDescripcion);
    void desactivar(Long id);
    List<Lote> listarTodos();
    List<Lote> listarActivos();
    Lote buscarPorId(Long id);
}
