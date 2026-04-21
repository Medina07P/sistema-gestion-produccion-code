package com.sistema.model;

import jakarta.persistence.*;

/**
 * Entidad: LOTE
 * Representa un lote de cultivo donde trabajan los recolectores.
 *
 * Hibernate mapea esta clase a la tabla LOTES en MySQL.
 * Los campos nulos o sus restricciones se definen con @Column.
 */
@Entity
@Table(name = "LOTES")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;          // Ej: "A1", "B2"

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ── Constructores ─────────────────────────────────────────

    public Lote() {}   // Requerido por JPA

    public Lote(String codigo, String descripcion) {
        this.codigo      = codigo;
        this.descripcion = descripcion;
        this.activo      = true;
    }

    // ── Getters y Setters ─────────────────────────────────────

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getCodigo()              { return codigo; }
    public void setCodigo(String codigo)   { this.codigo = codigo; }

    public String getDescripcion()                 { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo()              { return activo; }
    public void setActivo(Boolean activo)   { this.activo = activo; }

    // ── toString para mostrar en JComboBox ────────────────────

    @Override
    public String toString() {
        return codigo + (descripcion != null ? " — " + descripcion : "");
    }
}
