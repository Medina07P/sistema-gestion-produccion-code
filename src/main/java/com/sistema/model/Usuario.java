package com.sistema.model;


import jakarta.persistence.*;

@Entity
@Table(name = "USUARIOS")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Usuario() {}

    public Usuario(String username, String password, String nombre) {
        this.username = username;
        this.password = password;
        this.nombre   = nombre;
        this.activo   = true;
    }

    public Long    getId()       { return id; }
    public String  getUsername() { return username; }
    public String  getPassword() { return password; }
    public String  getNombre()   { return nombre; }
    public Boolean getActivo()   { return activo; }

    public void setId(Long id)        { this.id = id; }
    public void setUsername(String u) { this.username = u; }
    public void setPassword(String p) { this.password = p; }
    public void setNombre(String n)   { this.nombre = n; }
    public void setActivo(Boolean a)  { this.activo = a; }
}