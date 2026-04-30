package com.sistema.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad: PESAJE
 * Registro diario de kilogramos recolectados por un recolector.
 *
 * Relaciones:
 *   - @ManyToOne con Recolector  (muchos pesajes → un recolector)
 *   - @ManyToOne con Usuario     (muchos pesajes → un usuario que lo registró)
 *
 * El lote se obtiene navegando: pesaje.getRecolector().getLote()
 */
@Entity
@Table(name = "PESAJES")
public class Pesaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recolector_id", nullable = false)
    private Recolector recolector;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "kilos", nullable = false)
    private Double kilos;

    // ✅ CORREGIDO: @JoinColumn en lugar de @Column
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario registradoPor;

    // ── Constructores ─────────────────────────────────────────

    public Pesaje() {}

    public Pesaje(Recolector recolector, LocalDate fecha, Double kilos,
                  Usuario registradoPor) {
        this.recolector    = recolector;
        this.fecha         = fecha;
        this.kilos         = kilos;
        this.registradoPor = registradoPor;
    }

    // ── Getters y Setters ─────────────────────────────────────

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public Recolector getRecolector()       { return recolector; }
    public void setRecolector(Recolector r) { this.recolector = r; }

    // Acceso conveniente al lote sin FK directa
    public Lote getLote()                   { return recolector != null ? recolector.getLote() : null; }

    public LocalDate getFecha()             { return fecha; }
    public void setFecha(LocalDate fecha)   { this.fecha = fecha; }

    public Double getKilos()               { return kilos; }
    public void setKilos(Double kilos)     { this.kilos = kilos; }

    public Usuario getRegistradoPor()           { return registradoPor; }
    public void setRegistradoPor(Usuario u)     { this.registradoPor = u; }
}