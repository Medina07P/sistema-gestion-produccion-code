package com.sistema.service;

import com.sistema.model.Pago;
import com.sistema.model.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface IPagoService {
    List<Pago> calcularPagos(LocalDate inicio, LocalDate fin, double precioPorKilo);

    void guardarLiquidacion(LocalDate inicio, LocalDate fin,
                            double precioPorKilo, List<Pago> pagos,
                            Usuario usuario);

    void eliminarLiquidacion(Long id);
    void eliminarDetalleLiquidacion(Long detalleId);
}