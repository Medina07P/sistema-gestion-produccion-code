package com.sistema.service;

import com.sistema.dao.ILoteDAO;
import com.sistema.dao.IRecolectorDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.model.Recolector;
import com.sistema.util.Validador;

import java.util.List;

/**
 * Servicio de Recolectores.
 *
 * Depende de IRecolectorDAO e ILoteDAO para:
 *   - Validar que la cédula no esté duplicada.
 *   - Resolver el objeto Lote a partir del loteId recibido de la UI.
 */
public class RecolectorService implements IRecolectorService {

    private final IRecolectorDAO recolectorDAO;
    private final ILoteDAO       loteDAO;

    public RecolectorService(IRecolectorDAO recolectorDAO, ILoteDAO loteDAO) {
        this.recolectorDAO = recolectorDAO;
        this.loteDAO       = loteDAO;
    }

    @Override
    public void crear(String nombre, String cedula, Long loteId) {
        // ── Validaciones de entrada ───────────────────────────
        Validador.requerido(nombre, "nombre");
        Validador.longitudMaxima(nombre, 100, "nombre");
        Validador.cedula(cedula);

        // ── Regla de negocio: cédula única ────────────────────
        if (recolectorDAO.buscarPorCedula(cedula.trim()) != null) {
            throw new NegocioException(
                "Ya existe un recolector con la cédula '" + cedula + "'."
            );
        }

        // ── Resolución del lote ───────────────────────────────
        Lote lote = null;
        if (loteId != null) {
            lote = loteDAO.buscarPorId(loteId);
            if (lote == null) {
                throw new NegocioException("El lote seleccionado no existe.");
            }
        }

        recolectorDAO.guardar(new Recolector(nombre.trim(), cedula.trim(), lote));
    }

    @Override
    public void actualizar(Recolector r, String nombre, String cedula, Long loteId) {
        Validador.requerido(nombre, "nombre");
        Validador.cedula(cedula);

        // Verificar cédula única sólo si cambió
        if (!r.getCedula().equals(cedula.trim())) {
            if (recolectorDAO.buscarPorCedula(cedula.trim()) != null) {
                throw new NegocioException(
                    "Ya existe un recolector con la cédula '" + cedula + "'."
                );
            }
        }

        Lote lote = (loteId != null) ? loteDAO.buscarPorId(loteId) : null;

        r.setNombre(nombre.trim());
        r.setCedula(cedula.trim());
        r.setLote(lote);
        recolectorDAO.actualizar(r);
    }

    @Override
    public void desactivar(Long id) {
        Recolector r = recolectorDAO.buscarPorId(id);
        if (r == null) throw new NegocioException("Recolector no encontrado.");
        r.setActivo(false);
        recolectorDAO.actualizar(r);
    }

    @Override
    public List<Recolector> listarTodos() {
        return recolectorDAO.listarTodos();
    }

    @Override
    public List<Recolector> listarActivos() {
        return recolectorDAO.listarActivos();
    }

    @Override
    public Recolector buscarPorId(Long id) {
        return recolectorDAO.buscarPorId(id);
    }
    @Override
    public void activar(Long id) {
        Recolector r = recolectorDAO.buscarPorId(id);
        if (r == null) throw new NegocioException("Recolector no encontrado.");
        r.setActivo(true);
        recolectorDAO.actualizar(r);
    }
}
