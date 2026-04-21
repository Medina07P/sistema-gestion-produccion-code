package com.sistema.model;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object): PAGO
 *
 * NO es una entidad JPA. No se persiste en la BD.
 * Es el resultado del cálculo de liquidación que PagoService produce
 * y que PagoPanel muestra en la tabla.
 *
 * Principio SRP: sólo transporta datos del cálculo entre capas.
 *
 * Fórmula:
 *   totalPago = totalKilos * precioPorKilo
 */
public class Pago {

    private Recolector recolector;
    private Double     totalKilos;
    private Double     precioPorKilo;
    private Double     totalPago;       // Calculado en el constructor
    private LocalDate  fechaInicio;
    private LocalDate  fechaFin;

    // ── Constructor ───────────────────────────────────────────

    public Pago(Recolector recolector,
                Double     totalKilos,
                Double     precioPorKilo,
                LocalDate  fechaInicio,
                LocalDate  fechaFin) {
        this.recolector    = recolector;
        this.totalKilos    = totalKilos;
        this.precioPorKilo = precioPorKilo;
        this.totalPago     = totalKilos * precioPorKilo;  // Cálculo central
        this.fechaInicio   = fechaInicio;
        this.fechaFin      = fechaFin;
    }

    // ── Getters ───────────────────────────────────────────────

    public Recolector getRecolector()    { return recolector; }
    public Double     getTotalKilos()    { return totalKilos; }
    public Double     getPrecioPorKilo() { return precioPorKilo; }
    public Double     getTotalPago()     { return totalPago; }
    public LocalDate  getFechaInicio()   { return fechaInicio; }
    public LocalDate  getFechaFin()      { return fechaFin; }
}
