package com.sistema.model;

import jakarta.persistence.*;

/**
 * Entidad: RECOLECTOR
 * Persona que recolecta producto en un lote.
 *
 * Relación: Muchos recolectores → Un lote (@ManyToOne).
 * Hibernate usa RECOLECTORES.lote_id como FK.
 */
@Entity
@Table(name = "RECOLECTORES")
public class Recolector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cedula", nullable = false, unique = true, length = 20)
    private String cedula;

    /**
     * Relación @ManyToOne: muchos recolectores pertenecen a un lote.
     * FetchType.EAGER: carga el lote junto con el recolector (OK para este tamaño de datos).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ── Constructores ─────────────────────────────────────────

    public Recolector() {}  // Requerido por JPA

    public Recolector(String nombre, String cedula, Lote lote) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.lote   = lote;
        this.activo = true;
    }

    // ── Getters y Setters ─────────────────────────────────────

    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public String getNombre()              { return nombre; }
    public void setNombre(String nombre)   { this.nombre = nombre; }

    public String getCedula()              { return cedula; }
    public void setCedula(String cedula)   { this.cedula = cedula; }

    public Lote getLote()           { return lote; }
    public void setLote(Lote lote)  { this.lote = lote; }

    public Boolean getActivo()              { return activo; }
    public void setActivo(Boolean activo)   { this.activo = activo; }

    // ── toString para JComboBox ───────────────────────────────

    @Override
    public String toString() {
        return nombre + " (" + cedula + ")";
    }
}
