package com.sistema.service;

import com.sistema.dao.IPesajeDAO;
import com.sistema.exception.NegocioException;
import com.sistema.model.Pago;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.util.Validador;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio de Cálculo de Pagos.
 *
 * Fórmula central:
 *   totalPago = SUM(kilos del recolector en el período) × precioPorKilo
 *
 * Principios aplicados:
 *   SRP — sólo calcula pagos, no gestiona recolectores ni lotes.
 *   DIP — depende de IPesajeDAO (interfaz), nunca de la implementación.
 */
public class PagoService implements IPagoService {

    private final IPesajeDAO pesajeDAO;

    public PagoService(IPesajeDAO pesajeDAO) {
        this.pesajeDAO = pesajeDAO;
    }

    /**
     * Calcula la liquidación para todos los recolectores en el período dado.
     *
     * Pasos internos:
     *  1. Validar entradas (precio positivo, fechas coherentes).
     *  2. Obtener pesajes del período desde el DAO.
     *  3. Acumular kilos por recolector usando un Map.
     *  4. Construir un Pago (DTO) por cada recolector → totalPago se calcula en Pago.
     *  5. Devolver la lista ordenada por total descendente.
     *
     * @param inicio        Fecha de inicio del período
     * @param fin           Fecha de fin del período
     * @param precioPorKilo Precio en moneda local por kilogramo
     * @return Lista de objetos Pago ordenada de mayor a menor pago
     */
    @Override
    public List<Pago> calcularPagos(LocalDate inicio, LocalDate fin, double precioPorKilo) {
        // ── Validaciones de negocio ───────────────────────────
        Validador.positivo(precioPorKilo, "precio por kilo");
        Validador.rangoFechas(inicio, fin);

        // ── Consulta al DAO ───────────────────────────────────
        List<Pesaje> pesajes = pesajeDAO.buscarPorPeriodo(inicio, fin);

        if (pesajes.isEmpty()) {
            throw new NegocioException(
                "No hay pesajes registrados en el período " + inicio + " → " + fin + "."
            );
        }

        // ── Acumulación: Map<Recolector, kilosTotales> ────────
        // LinkedHashMap preserva el orden de inserción
        Map<Recolector, Double> acumulados = new LinkedHashMap<>();
        for (Pesaje p : pesajes) {
            acumulados.merge(p.getRecolector(), p.getKilos(), Double::sum);
        }

        // ── Construcción de DTOs de Pago ─────────────────────
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

        // Ordenar de mayor a menor pago total
        pagos.sort((a, b) -> Double.compare(b.getTotalPago(), a.getTotalPago()));
        return pagos;
    }
}
