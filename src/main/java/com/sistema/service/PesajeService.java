package com.sistema.service;

import com.sistema.dao.ILoteDAO;
import com.sistema.dao.IPesajeDAO;
import com.sistema.dao.IRecolectorDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.util.Validador;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Pesajes.
 * Orquesta validaciones y coordina los tres DAOs que necesita.
 */
public class PesajeService implements IPesajeService {

    private final IPesajeDAO     pesajeDAO;
    private final IRecolectorDAO recolectorDAO;
    private final ILoteDAO       loteDAO;

    public PesajeService(IPesajeDAO pesajeDAO,
                         IRecolectorDAO recolectorDAO,
                         ILoteDAO loteDAO) {
        this.pesajeDAO     = pesajeDAO;
        this.recolectorDAO = recolectorDAO;
        this.loteDAO       = loteDAO;
    }

    /**
     * Valida y registra un pesaje.
     *
     * @param recolectorId ID del recolector seleccionado en la UI
     * @param loteId       ID del lote seleccionado en la UI
     * @param fechaStr     Texto de la fecha en formato AAAA-MM-DD
     * @param kilosStr     Texto de los kilos (ej: "50.5")
     */
    @Override
    public void registrar(Long recolectorId, Long loteId, String fechaStr, String kilosStr) {
        // ── Parseo y validación de tipos ──────────────────────
        double kilos = Validador.parsearDouble(kilosStr, "kilos");
        Validador.positivo(kilos, "kilos");

        LocalDate fecha = Validador.parsearFecha(fechaStr, "fecha");

        if (fecha.isAfter(LocalDate.now())) {
            throw new NegocioException("No se puede registrar un pesaje con fecha futura.");
        }

        // ── Resolución de entidades ───────────────────────────
        if (recolectorId == null) throw new NegocioException("Debe seleccionar un recolector.");
        if (loteId == null)       throw new NegocioException("Debe seleccionar un lote.");

        Recolector recolector = recolectorDAO.buscarPorId(recolectorId);
        if (recolector == null || !recolector.getActivo()) {
            throw new NegocioException("El recolector seleccionado no está activo.");
        }

        Lote lote = loteDAO.buscarPorId(loteId);
        if (lote == null || !lote.getActivo()) {
            throw new NegocioException("El lote seleccionado no está activo.");
        }

        pesajeDAO.guardar(new Pesaje(recolector, lote, fecha, kilos));
    }

    @Override
    public void eliminar(Long id) {
        Pesaje p = pesajeDAO.buscarPorId(id);
        if (p == null) throw new NegocioException("Pesaje no encontrado.");
        pesajeDAO.eliminar(id);
    }

    @Override
    public List<Pesaje> listarTodos() {
        return pesajeDAO.listarTodos();
    }
}
