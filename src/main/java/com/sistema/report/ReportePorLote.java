package com.sistema.report;

import com.sistema.dao.IPesajeDAO;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte: Total de kilos por Lote en un período.
 *
 * Columnas: Lote | Descripción | Total Kilos
 *
 * Implementa IReporteGenerador. Para agregar este reporte
 * al sistema sólo hubo que crear esta clase. (OCP ✓)
 */
public class ReportePorLote implements IReporteGenerador {

    private final IPesajeDAO pesajeDAO;

    public ReportePorLote(IPesajeDAO pesajeDAO) {
        this.pesajeDAO = pesajeDAO;
    }

    @Override
    public String getTitulo() {
        return "Kilos por Lote";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Código Lote", "Descripción", "Total Kilos"};
    }

    @Override
    public List<Object[]> generarDatos(LocalDate inicio, LocalDate fin) {
        return pesajeDAO.totalKilosPorLote(inicio, fin);
    }
}
