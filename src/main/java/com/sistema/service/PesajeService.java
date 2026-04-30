package com.sistema.service;

import com.sistema.dao.IPesajeDAO;
import com.sistema.dao.IRecolectorDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.model.Usuario;
import com.sistema.util.Validador;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Pesajes.
 * Orquesta validaciones y coordina los DAOs que necesita.
 *
 * CAMBIOS:
 *  - Se eliminó ILoteDAO: el lote se obtiene desde el recolector
 *  - Se agregó precioPorKiloStr como parámetro de registrar()
 */
public class PesajeService implements IPesajeService {

    private final IPesajeDAO     pesajeDAO;
    private final IRecolectorDAO recolectorDAO;

    // ✅ ELIMINADO: loteDAO — ya no se necesita, el lote viene del recolector
    public PesajeService(IPesajeDAO pesajeDAO, IRecolectorDAO recolectorDAO) {
        this.pesajeDAO     = pesajeDAO;
        this.recolectorDAO = recolectorDAO;
    }

    /**
     * Valida y registra un pesaje.
     *
     * @param recolectorId      ID del recolector seleccionado en la UI
     * @param fechaStr          Texto de la fecha en formato AAAA-MM-DD
     * @param kilosStr          Texto de los kilos (ej: "50.5")
     * @param precioPorKiloStr  Texto del precio por kilo (ej: "1200.0")
     * @param usuarioActual     Usuario autenticado que realiza el registro
     */
    @Override
public void registrar(Long recolectorId,
                      String fechaStr, String kilosStr,
                      Usuario usuarioActual) {
    // ── Parseo y validación de tipos ──────────────────────
    double    kilos = Validador.parsearDouble(kilosStr, "kilos");
    LocalDate fecha = Validador.parsearFecha(fechaStr,  "fecha");

    Validador.positivo(kilos, "kilos");

    if (fecha.isAfter(LocalDate.now())) {
        throw new NegocioException("No se puede registrar un pesaje con fecha futura.");
    }

    // ── Resolución de entidades ───────────────────────────
    if (recolectorId == null) throw new NegocioException("Debe seleccionar un recolector.");

    Recolector recolector = recolectorDAO.buscarPorId(recolectorId);
    if (recolector == null || !recolector.getActivo()) {
        throw new NegocioException("El recolector seleccionado no está activo.");
    }

    Lote lote = recolector.getLote();
    if (lote == null || !lote.getActivo()) {
        throw new NegocioException("El recolector no tiene un lote activo asignado.");
    }

    // ✅ CORREGIDO: sin precioPorKilo — el precio se maneja en Pagos
    pesajeDAO.guardar(new Pesaje(recolector, fecha, kilos, usuarioActual));
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