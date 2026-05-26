package com.sistema.report;

import com.sistema.dao.ILiquidacionDAO;
import com.sistema.model.Liquidacion;
import com.sistema.model.LiquidacionDetalle;
import com.sistema.service.IPagoService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporte: Historial de Liquidaciones guardadas.
 *
 * Columnas: ID | Período | Precio x Kilo | Recolector | Cédula | Total Kilos | Total Pago | Registrado por | Fecha Registro
 *
 * La columna ID permite identificar la liquidación para eliminarla.
 * No usa el rango de fechas del filtro — muestra todo el historial.
 */
public class ReporteLiquidaciones implements IReporteGenerador {

    private final ILiquidacionDAO liquidacionDAO;
    private final IPagoService    pagoService;

    public ReporteLiquidaciones(ILiquidacionDAO liquidacionDAO, IPagoService pagoService) {
        this.liquidacionDAO = liquidacionDAO;
        this.pagoService    = pagoService;
    }

    @Override
    public String getTitulo() {
        return "Historial de Liquidaciones";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{
            "ID", "Período", "Precio x Kilo", "Recolector", "Cédula",
            "Total Kilos", "Total Pago", "Registrado por", "Fecha Registro"
        };
    }

    @Override
    public List<Object[]> generarDatos(LocalDate inicio, LocalDate fin) {
        List<Liquidacion> liquidaciones = liquidacionDAO.listarTodas();
        List<Object[]> filas = new ArrayList<>();

        for (Liquidacion liq : liquidaciones) {
            String periodo       = liq.getFechaDesde() + " → " + liq.getFechaHasta();
            String precioPorKilo = String.format("$ %.2f", liq.getPrecioPorKilo());
            String registradoPor = liq.getUsuario() != null ? liq.getUsuario().getNombre() : "—";
            String fechaRegistro = liq.getFechaRegistro().toLocalDate().toString();

            for (LiquidacionDetalle detalle : liq.getDetalles()) {
                filas.add(new Object[]{
                    detalle.getId(),
                    periodo,
                    precioPorKilo,
                    detalle.getRecolector().getNombre(),
                    detalle.getRecolector().getCedula(),
                    String.format("%.2f kg", detalle.getTotalKilos()),
                    String.format("$ %.2f",  detalle.getTotalPago()),
                    registradoPor,
                    fechaRegistro
                });
            }
        }

        return filas;
    }

    @Override
    public boolean soportaEliminar() {
        return true;
    }

    @Override
    public void eliminar(Object idFila) {
        pagoService.eliminarDetalleLiquidacion(((Number) idFila).longValue());
    }
}
