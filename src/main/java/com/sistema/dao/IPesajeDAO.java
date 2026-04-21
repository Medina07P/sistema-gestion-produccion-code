package com.sistema.dao;

import com.sistema.model.Pesaje;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz específica del DAO de Pesajes.
 * Agrega consultas por período que son esenciales para
 * el cálculo de pagos y la generación de reportes.
 */
public interface IPesajeDAO extends IGenericDAO<Pesaje, Long> {

    /**
     * Retorna todos los pesajes registrados en el rango de fechas dado.
     * Usado por PagoService para calcular liquidaciones y por los reportes.
     *
     * @param inicio Fecha de inicio (inclusiva)
     * @param fin    Fecha de fin (inclusiva)
     */
    List<Pesaje> buscarPorPeriodo(LocalDate inicio, LocalDate fin);

    /**
     * Retorna filas de reporte con total de kilos agrupado por recolector.
     * Cada Object[] contiene: [nombreRecolector, cedula, totalKilos]
     */
    List<Object[]> totalKilosPorRecolector(LocalDate inicio, LocalDate fin);

    /**
     * Retorna filas de reporte con total de kilos agrupado por lote.
     * Cada Object[] contiene: [codigoLote, descripcionLote, totalKilos]
     */
    List<Object[]> totalKilosPorLote(LocalDate inicio, LocalDate fin);
}
