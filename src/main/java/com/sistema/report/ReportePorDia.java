package com.sistema.report;

import com.sistema.dao.IPesajeDAO;
import com.sistema.model.Pesaje;

import java.time.LocalDate;
import java.util.*;

/**
 * Reporte: Total de kilos por Día en un período.
 *
 * Columnas: Fecha | Total Kilos | N° Recolectores
 *
 * Este reporte demuestra el Principio OCP en acción:
 * fue agregado DESPUÉS del diseño inicial sin modificar
 * ReportePanel ni IReporteGenerador. Solo se creó esta clase.
 *
 * Su lógica de agrupación la hace en Java (no en JPQL),
 * mostrando que cada implementación puede tener su propia estrategia.
 */
public class ReportePorDia implements IReporteGenerador {

    private final IPesajeDAO pesajeDAO;

    public ReportePorDia(IPesajeDAO pesajeDAO) {
        this.pesajeDAO = pesajeDAO;
    }

    @Override
    public String getTitulo() {
        return "Kilos por Día";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Fecha", "Total Kilos", "Nº Recolectores"};
    }

    @Override
    public List<Object[]> generarDatos(LocalDate inicio, LocalDate fin) {
        List<Pesaje> pesajes = pesajeDAO.buscarPorPeriodo(inicio, fin);

        // Agrupar por fecha → TreeMap mantiene orden cronológico
        Map<LocalDate, double[]> porDia = new TreeMap<>();
        for (Pesaje p : pesajes) {
            porDia.computeIfAbsent(p.getFecha(), k -> new double[]{0.0, 0.0});
            porDia.get(p.getFecha())[0] += p.getKilos();   // acumular kilos
            porDia.get(p.getFecha())[1]++;                  // contar recolectores
        }

        // Convertir a List<Object[]> para la JTable
        List<Object[]> filas = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> e : porDia.entrySet()) {
            filas.add(new Object[]{
                e.getKey().toString(),
                String.format("%.2f", e.getValue()[0]),
                (int) e.getValue()[1]
            });
        }
        return filas;
    }
}
