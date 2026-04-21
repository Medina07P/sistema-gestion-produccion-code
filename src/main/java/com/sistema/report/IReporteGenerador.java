package com.sistema.report;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de Generador de Reportes.
 *
 * ── POLIMORFISMO (OCP) ────────────────────────────────────────
 * ReportePanel recibe una List<IReporteGenerador> y puede mostrar
 * cualquier tipo de reporte sin conocer su implementación concreta.
 *
 * Implementaciones:
 *   ReportePorRecolector  → kilos y pago por persona
 *   ReportePorLote        → kilos por lote
 *   ReportePorDia         → kilos por día en el período
 *
 * Para agregar un nuevo reporte: crear una clase que implemente
 * esta interfaz. ReportePanel NO necesita modificarse. (OCP ✓)
 */
public interface IReporteGenerador {

    /** Nombre del reporte, aparece en el JComboBox de selección. */
    String getTitulo();

    /** Encabezados de columna para la JTable. */
    String[] getColumnas();

    /**
     * Ejecuta la consulta y retorna filas de datos.
     * Cada Object[] es una fila: sus elementos mapean 1-a-1 con getColumnas().
     *
     * @param inicio Fecha de inicio (inclusiva)
     * @param fin    Fecha de fin (inclusiva)
     */
    List<Object[]> generarDatos(LocalDate inicio, LocalDate fin);
}
