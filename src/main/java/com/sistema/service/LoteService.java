package com.sistema.service;

import com.sistema.dao.ILoteDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.util.Validador;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Lotes: capa de lógica de negocio.
 *
 * Principios aplicados:
 *   SRP — Solo gestiona reglas de negocio de Lotes.
 *   DIP — Depende de ILoteDAO (interfaz), no de LoteDAOImpl.
 *
 * El constructor recibe el DAO por inyección manual (MainFrame).
 */
public class LoteService implements ILoteService {

    private final ILoteDAO loteDAO;

    public LoteService(ILoteDAO loteDAO) {
        this.loteDAO = loteDAO;
    }

    @Override
    public void crear(String codigo, String descripcion) {
        // ── Validaciones ─────────────────────────────────────
        Validador.requerido(codigo, "código");
        Validador.longitudMaxima(codigo, 10, "código");

        // Verificar unicidad del código
        if (loteDAO.buscarPorCodigo(codigo.trim().toUpperCase()) != null) {
            throw new NegocioException("Ya existe un lote con el código '" + codigo + "'.");
        }

        Lote lote = new Lote(codigo.trim().toUpperCase(), descripcion);
        loteDAO.guardar(lote);
    }

    @Override
    public void actualizar(Lote lote, String nuevoCodigo, String nuevaDescripcion) {
        Validador.requerido(nuevoCodigo, "código");
        Validador.longitudMaxima(nuevoCodigo, 10, "código");

        // Si el código cambia, verificar que el nuevo no exista
        if (!lote.getCodigo().equalsIgnoreCase(nuevoCodigo.trim())) {
            if (loteDAO.buscarPorCodigo(nuevoCodigo.trim().toUpperCase()) != null) {
                throw new NegocioException("Ya existe un lote con el código '" + nuevoCodigo + "'.");
            }
        }

        lote.setCodigo(nuevoCodigo.trim().toUpperCase());
        lote.setDescripcion(nuevaDescripcion);
        loteDAO.actualizar(lote);
    }

    @Override
    public void desactivar(Long id) {
        Lote lote = loteDAO.buscarPorId(id);
        if (lote == null) throw new NegocioException("Lote no encontrado.");
        lote.setActivo(false);
        loteDAO.actualizar(lote);
    }

    @Override
    public List<Lote> listarTodos() {
        return loteDAO.listarTodos();
    }

    @Override
    public List<Lote> listarActivos() {
        return loteDAO.listarTodos().stream()
                .filter(Lote::getActivo)
                .collect(Collectors.toList());
    }

    @Override
    public Lote buscarPorId(Long id) {
        return loteDAO.buscarPorId(id);
    }
}
