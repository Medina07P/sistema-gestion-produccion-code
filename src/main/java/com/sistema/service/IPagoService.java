package com.sistema.service;

import com.sistema.model.Pago;
import java.time.LocalDate;
import java.util.List;

public interface IPagoService {
    List<Pago> calcularPagos(LocalDate inicio, LocalDate fin, double precioPorKilo);
}
