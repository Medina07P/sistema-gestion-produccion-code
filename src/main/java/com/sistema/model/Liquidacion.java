package com.sistema.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LIQUIDACIONES")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "precio_por_kilo", nullable = false)
    private Double precioPorKilo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LiquidacionDetalle> detalles = new ArrayList<>();

    public Liquidacion() {}

    public Liquidacion(LocalDate fechaDesde, LocalDate fechaHasta,
                       Double precioPorKilo, Usuario usuario) {
        this.fechaDesde    = fechaDesde;
        this.fechaHasta    = fechaHasta;
        this.precioPorKilo = precioPorKilo;
        this.fechaRegistro = LocalDateTime.now();
        this.usuario       = usuario;
    }

    public Long getId()                              { return id; }
    public LocalDate getFechaDesde()                 { return fechaDesde; }
    public LocalDate getFechaHasta()                 { return fechaHasta; }
    public Double getPrecioPorKilo()                 { return precioPorKilo; }
    public LocalDateTime getFechaRegistro()          { return fechaRegistro; }
    public Usuario getUsuario()                      { return usuario; }
    public List<LiquidacionDetalle> getDetalles()    { return detalles; }
    public void setDetalles(List<LiquidacionDetalle> d) { this.detalles = d; }
}