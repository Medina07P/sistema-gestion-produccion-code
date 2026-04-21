package com.sistema.report;

import com.sistema.dao.IPesajeDAO;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte: Total de kilos por Recolector en un período.
 *
 * Columnas: Recolector | Cédula | Total Kilos
 *
 * Implementa IReporteGenerador → polimorfismo aplicado:
 * ReportePanel llama a generarDatos() sin saber de qué tipo es.
 */
public class ReportePorRecolector implements IReporteGenerador {

    private final IPesajeDAO pesajeDAO;

    public ReportePorRecolector(IPesajeDAO pesajeDAO) {
        this.pesajeDAO = pesajeDAO;
    }

    @Override
    public String getTitulo() {
        return "Kilos por Recolector";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Recolector", "Cédula", "Total Kilos"};
    }

    /**
     * Delega la consulta JPQL agregada al DAO.
     * Los datos vienen ya ordenados por total descendente.
     * Retorna: [nombreRecolector, cedula, totalKilos]
     */
    @Override
    public List<Object[]> generarDatos(LocalDate inicio, LocalDate fin) {
        return pesajeDAO.totalKilosPorRecolector(inicio, fin);
    }
}
