package com.sistema.model;

import jakarta.persistence.*;

@Entity
@Table(name = "LIQUIDACION_DETALLE")
public class LiquidacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recolector_id", nullable = false)
    private Recolector recolector;

    @Column(name = "total_kilos", nullable = false)
    private Double totalKilos;

    @Column(name = "total_pago", nullable = false)
    private Double totalPago;

    public LiquidacionDetalle() {}

    public LiquidacionDetalle(Liquidacion liquidacion, Recolector recolector,
                               Double totalKilos, Double totalPago) {
        this.liquidacion = liquidacion;
        this.recolector  = recolector;
        this.totalKilos  = totalKilos;
        this.totalPago   = totalPago;
    }

    public Long getId()                  { return id; }
    public Liquidacion getLiquidacion()  { return liquidacion; }
    public Recolector getRecolector()    { return recolector; }
    public Double getTotalKilos()        { return totalKilos; }
    public Double getTotalPago()         { return totalPago; }
}