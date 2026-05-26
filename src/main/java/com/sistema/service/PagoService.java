package com.sistema.service;

import com.sistema.dao.ILiquidacionDAO;
import com.sistema.dao.IPesajeDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Liquidacion;
import com.sistema.model.LiquidacionDetalle;
import com.sistema.model.Pago;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.model.Usuario;
import com.sistema.util.Validador;

import java.time.LocalDate;
import java.util.*;

public class PagoService implements IPagoService {

    private final IPesajeDAO      pesajeDAO;
    private final ILiquidacionDAO liquidacionDAO;  // ✅ NUEVO

    // ✅ NUEVO: constructor actualizado
    public PagoService(IPesajeDAO pesajeDAO, ILiquidacionDAO liquidacionDAO) {
        this.pesajeDAO      = pesajeDAO;
        this.liquidacionDAO = liquidacionDAO;
    }

    @Override
    public List<Pago> calcularPagos(LocalDate inicio, LocalDate fin, double precioPorKilo) {
        Validador.positivo(precioPorKilo, "precio por kilo");
        Validador.rangoFechas(inicio, fin);

        List<Pesaje> pesajes = pesajeDAO.buscarPorPeriodo(inicio, fin);
        if (pesajes.isEmpty()) {
            throw new NegocioException(
                "No hay pesajes registrados en el período " + inicio + " → " + fin + "."
            );
        }

        Map<Recolector, Double> acumulados = new LinkedHashMap<>();
        for (Pesaje p : pesajes) {
            acumulados.merge(p.getRecolector(), p.getKilos(), Double::sum);
        }

        List<Pago> pagos = new ArrayList<>();
        for (Map.Entry<Recolector, Double> entrada : acumulados.entrySet()) {
            pagos.add(new Pago(
                entrada.getKey(),
                entrada.getValue(),
                precioPorKilo,
                inicio,
                fin
            ));
        }

        pagos.sort((a, b) -> Double.compare(b.getTotalPago(), a.getTotalPago()));
        return pagos;
    }

    @Override
    public void guardarLiquidacion(LocalDate inicio, LocalDate fin,
                                   double precioPorKilo, List<Pago> pagos,
                                   Usuario usuario) {
        Validador.positivo(precioPorKilo, "precio por kilo");
        Validador.rangoFechas(inicio, fin);
        if (pagos == null || pagos.isEmpty()) {
            throw new NegocioException("No hay pagos para guardar. Calcule primero.");
        }
        if (usuario == null) {
            throw new NegocioException("No se identifica al usuario que registra la liquidación.");
        }

        Liquidacion liq = new Liquidacion(inicio, fin, precioPorKilo, usuario);

        List<LiquidacionDetalle> detalles = new ArrayList<>();
        for (Pago p : pagos) {
            detalles.add(new LiquidacionDetalle(
                liq,
                p.getRecolector(),
                p.getTotalKilos(),
                p.getTotalPago()
            ));
        }
        liq.setDetalles(detalles);
        liquidacionDAO.guardar(liq);
    }

    @Override
    public void eliminarLiquidacion(Long id) {
        if (id == null) throw new NegocioException("ID de liquidación no válido.");
        liquidacionDAO.eliminar(id);
    }

    @Override
    public void eliminarDetalleLiquidacion(Long detalleId) {
        if (detalleId == null) throw new NegocioException("ID de detalle no válido.");
        liquidacionDAO.eliminarDetalle(detalleId);
    }
}