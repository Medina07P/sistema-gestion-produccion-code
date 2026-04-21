package com.sistema.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad: PESAJE
 * Registro diario de kilogramos recolectados por un recolector en un lote.
 *
 * Relaciones:
 *   - @ManyToOne con Recolector  (muchos pesajes → un recolector)
 *   - @ManyToOne con Lote        (muchos pesajes → un lote)
 *   - @ManyToOne con Usuario     (muchos pesajes → un usuario que lo registró)
 *
 * Hibernate mapea LocalDate a columna DATE en MySQL
 * usando el conversor automático de java.time.
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "kilos", nullable = false)
    private Double kilos;   // Validado en PesajeService: debe ser > 0

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario registradoPor;   // Usuario autenticado que registró el pesaje

    // ── Constructores ─────────────────────────────────────────

    public Pesaje() {}  // Requerido por JPA

    public Pesaje(Recolector recolector, Lote lote, LocalDate fecha, Double kilos, Usuario registradoPor) {
        this.recolector    = recolector;
        this.lote          = lote;
        this.fecha         = fecha;
        this.kilos         = kilos;
        this.registradoPor = registradoPor;
    }

    // ── Getters y Setters ─────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public Recolector getRecolector()                { return recolector; }
    public void setRecolector(Recolector r)          { this.recolector = r; }

    public Lote getLote()                            { return lote; }
    public void setLote(Lote lote)                   { this.lote = lote; }

    public LocalDate getFecha()                      { return fecha; }
    public void setFecha(LocalDate fecha)            { this.fecha = fecha; }

    public Double getKilos()                         { return kilos; }
    public void setKilos(Double kilos)               { this.kilos = kilos; }

    public Usuario getRegistradoPor()                { return registradoPor; }
    public void setRegistradoPor(Usuario u)          { this.registradoPor = u; }
}